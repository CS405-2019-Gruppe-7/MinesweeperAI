package ai.logic;

import ai.utility.StatefulMSAgent;
import ai.utility.Utility;
import api.MSField;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class LogicMSAgent extends StatefulMSAgent<LogicFieldCell> {

    private ISolver solver;
    private Random rng = new Random();

    public LogicMSAgent(MSField field) {
        super(field, LogicFieldCell::new);
        solver = SolverFactory.newDefault();
    }

    @Override
    public boolean solve(){
        try{
            return this.solveUnsafe();
        }catch (Exception e){
            System.out.println("Field state at error:");
            System.out.println(field);
            throw e;
        }
    }

    private boolean solveUnsafe() {
        IVecInt clause = new VecInt();
        clause.clear();
        Queue<LogicFieldCell> fieldsToUncover = new LinkedList<>();
        fieldsToUncover.add(get(0,0));

        while(!gameOver()){
            println("=========================");
            println("Fields to uncover queue:");

            if(fieldsToUncover.size() == 0){

                getAllCells()
                    .filter(it -> !it.covered)
                    .flatMap(it -> getNeighbours(it.x, it.y))
                    .filter(it -> it.covered)
                    .filter(this::bombFilter)
                    .distinct()
                    .forEach(it -> it.bombFlag = true);

                List<LogicFieldCell> notBombs = getAllCells()
                        .filter(it -> !it.covered)
                        .flatMap(it -> getNeighbours(it.x, it.y))
                        .filter(it -> it.covered)
                        .filter(this::notBombFilter)
                        .distinct()
                        .collect(toList());
                if(notBombs.size() > 0){
                    fieldsToUncover.addAll(notBombs);
                }else{
                    // random guess here.
                    println("Adding random cell to the queue!");
                    fieldsToUncover.add(getRandomCoveredCell());
                }
            }

            StringBuilder sb = new StringBuilder();
            for(LogicFieldCell cell: fieldsToUncover){
                sb.append(String.format("(%d;%d), ", cell.x, cell.y));
            }
            println(sb.toString());
            println("Field:");
            println(field);
            LogicFieldCell fieldToUncover = fieldsToUncover.poll();

            println(String.format("Uncovering: (%d;%d)", fieldToUncover.x, fieldToUncover.y));

            LogicFieldCell uncovered = uncover(fieldToUncover.x, fieldToUncover.y);
            if(uncovered.bomb){
                println(field);
                println("BOOM!");
                return false;
            }
            analyzeCell(fieldToUncover);
            // finds all not bomb neighbours of the uncovered one
            fieldsToUncover.addAll(
                    getNeighbours(uncovered.x, uncovered.y)
                            .filter(it -> !fieldsToUncover.contains(it))
                            .filter(it -> it.covered)
                            .filter(this::notBombFilter)
                            .collect(toList())
            );

            if(display) {
                getAllCells().forEach(it -> it.bombFlag = false);
                List<LogicFieldCell> notBombs = getAllCells()
                        .filter(it -> !it.covered)
                        .flatMap(it -> getNeighbours(it.x, it.y))
                        .filter(it -> it.covered)
                        .filter(this::notBombFilter)
                        .distinct()
                        .collect(toList());

                List<LogicFieldCell> bombs = getAllCells()
                        .filter(it -> !it.covered)
                        .flatMap(it -> getNeighbours(it.x, it.y))
                        .filter(it -> it.covered)
                        .filter(this::bombFilter)
                        .distinct()
                        .collect(toList());
                bombs.forEach(it -> it.bombFlag = true);

                println("Found notBombs:" + notBombs.size());
                println("Found bombs:" + bombs.size());
                if (bombs.size() > 0) {
                    println("Bombs at:");
                    bombs.forEach(it -> {
                        println("" + it.x + ";" + it.y);
//                    pushClause(coordinatesToNumber(it));
                    });
                    println("Bomb flags:");
                    this.printInternalField();
                }
            }
        }
        boolean bombUncovered = getAllCells().anyMatch(it -> it.bomb && !it.covered);
        if(bombUncovered){
            println("Game over - a bomb was uncovered!");
        }else{
            println("Game over - the field was solved");
        }
        println(field);
        return !bombUncovered;
    }

    public void analyzeCell(LogicFieldCell cell) {
        List<LogicFieldCell> neighbours = getNeighbours(cell.x, cell.y).collect(toList());
        if(cell.bombsAround == 0){
            neighbours.forEach(notBomb -> {
                try{
                pushClause(-coordinatesToNumber(notBomb));
                }catch (RuntimeException e){
                    System.out.println("Error occurred while adding clauses for cell with 0 bombs around");
                    System.out.println(String.format("The analyzed cell was: (%d;%d) = %d %s",
                            cell.x,
                            cell.y,
                            cell.bombsAround,
                            cell.bomb? "BOMB" : ""
                    ));
                    throw e;
                }
            });
        }else if (cell.bombsAround < neighbours.size()){

            List<Integer> positiveLiterals = neighbours.stream()
                    .map(this::coordinatesToNumber)
                    .collect(toList());
            List<Integer> negativeLiterals = neighbours.stream()
                    .map(it -> -coordinatesToNumber(it))
                    .collect(toList());
            int atLeastClauseSize = neighbours.size() - cell.bombsAround + 1;
            int atMostClauseSize = cell.bombsAround + 1;
            List<IVecInt> atLeastClauses = Utility.getAllDisjunctions(positiveLiterals, atLeastClauseSize);
            List<IVecInt> atMostClauses = Utility.getAllDisjunctions(negativeLiterals,atMostClauseSize);

            try {
                atLeastClauses.forEach(this::pushClause);
                atMostClauses.forEach(this::pushClause);
            }catch (RuntimeException e){
                System.out.println("Error occurred while adding clauses for cell with some bombs around");
                System.out.println(String.format("The analyzed cell was: (%d;%d) = %d", cell.x, cell.y, cell.bombsAround));
                throw e;
            }

        }else if(cell.bombsAround == neighbours.size()){
            // all the neighbouring cells are bombs
            try {
                neighbours.forEach(it -> {
                    pushClause(coordinatesToNumber(cell));
                    it.bombFlag = true;
                });
            }catch (RuntimeException e){
                System.out.println("Error occurred while adding clauses for cell with only bombs around");
                System.out.println(String.format("The analyzed cell was: (%d;%d) = %d", cell.x, cell.y, cell.bombsAround));
                throw e;
            }
        }
    }

    private LogicFieldCell getRandomCoveredCell(){
        List<LogicFieldCell> allCells = getAllCells().filter(it -> it.covered && !it.bombFlag).collect(toList());
        LogicFieldCell cell = allCells.get(rng.nextInt(allCells.size()));
        return cell;
    }

    private boolean gameOver(){
        boolean bombUncovered = getAllCells().anyMatch(it -> it.bomb);
        return field.solved() || bombUncovered;
    }

    private boolean bombFilter(LogicFieldCell it){
        int number = coordinatesToNumber(it);
        IVecInt clause = new VecInt();
        clause.push(-number);
        try {
            return !solver.isSatisfiable(clause);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean notBombFilter(LogicFieldCell it){
        int number = coordinatesToNumber(it);
        IVecInt clause = new VecInt();
        clause.push(number);
        try {
            return !solver.isSatisfiable(clause);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void pushClause(int ... literals) {
        IVecInt clause = new VecInt();
        for(int l: literals){
            clause.push(l);
        }
        pushClause(clause);
    }

    private void pushClause(IVecInt clause) {
        if(!clause.isEmpty()) {
            println("Adding clause:" + clause.toString());
            try {
                solver.addClause(clause);
            } catch (ContradictionException e) {
                System.out.println("Contradiction: " + clause);
                if(clause.size() == 1){
                    int clauseCell = Math.abs(clause.last());
                    int x = clauseCell % nColumns;
                    int y = (clauseCell - x) / nColumns;
                    System.out.println(String.format("That is a cell at: (%d;%d)", x, y));
                }
                throw new RuntimeException("Tried to add invalid clause!", e);
            }
        }else{
            System.out.println("Can't add an empty clause");
        }
    }


    private int coordinatesToNumber(LogicFieldCell cell){
        return cell.y * nRows + cell.x + 1;
    }

    private void println(String s){
        if(display){
            System.out.println(s);
        }
    }

    private void println(Object o){
        println(o.toString());
    }
}
