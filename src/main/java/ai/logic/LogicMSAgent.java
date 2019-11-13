package ai.logic;

import ai.utility.FieldCellFactoryFunction;
import ai.utility.StatefulMSAgent;
import ai.utility.Utility;
import api.MSField;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;
import java.util.stream.Stream;

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
            println("Field state at error:");
            println(field);
            throw e;
        }
    }

    private boolean solveUnsafe() {
        IVecInt clause = new VecInt();
        clause.clear();
        firstMove();

        while(!gameOver()){
            println("Solving iteration");
            println("Field:");
            println(field);
            solver.clearLearntClauses();
            getAllCells().filter(it -> !it.covered).forEach(this::analyzeCell);
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

            println("Found notBombs:" + notBombs.size());
            println("Found bombs:" + bombs.size());
            if(bombs.size() > 0){
                println("Bombs at:");
                bombs.forEach(it -> {
                    println("" + it.x + ";" + it.y);
//                    pushClause(coordinatesToNumber(it));
                });
            }
            notBombs.forEach(it -> {
                println(String.format("Uncovering: (%d;%d)", it.x, it.y));
                LogicFieldCell uncovered = uncover(it.x, it.y);
                if(uncovered.bomb){
                    println(field);
                    println("BOOM!");
                }
            });
            if(notBombs.size() == 0){
                // random guess here.
                println("Uncovering random cell!");
                uncoverRandomCell();
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
        IVecInt clause = new VecInt();
        List<LogicFieldCell> neighbours = getNeighbours(cell.x, cell.y).collect(toList());
        if(cell.bombsAround == 0){
            neighbours.forEach(notBomb -> {
                try{
                pushClause(-coordinatesToNumber(notBomb));
                }catch (RuntimeException e){
                    println("Error occurred while adding clauses for cell with 0 bombs around");
                    println(String.format("The analyzed cell was: (%d;%d) = %d %s",
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
                println("Error occurred while adding clauses for cell with some bombs around");
                println(String.format("The analyzed cell was: (%d;%d) = %d", cell.x, cell.y, cell.bombsAround));
                throw e;
            }

        }else if(cell.bombsAround == neighbours.size()){
            // all the neighbouring cells are bombs
            try {
                neighbours.forEach(it -> pushClause(
                        coordinatesToNumber(cell)
                ));
            }catch (RuntimeException e){
                println("Error occurred while adding clauses for cell with only bombs around");
                println(String.format("The analyzed cell was: (%d;%d) = %d", cell.x, cell.y, cell.bombsAround));
                throw e;
            }
        }
    }

    private void uncoverRandomCell(){
        List<LogicFieldCell> allCells = getAllCells().filter(it -> it.covered).collect(toList());
        LogicFieldCell cell = allCells.get(rng.nextInt(allCells.size()));
        cell = this.uncover(cell.x, cell.y);
        if(cell.bomb){
            println("BOOM!");
            println(String.format("Random click uncovered a bomb at (%d;%d)", cell.x, cell.y));
        }
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
                println("Contradiction: " + clause);
                if(clause.size() == 1){
                    int clauseCell = Math.abs(clause.last());
                    int x = clauseCell % nColumns;
                    int y = (clauseCell - x) / nColumns;
                    println(String.format("That is a cell at: (%d;%d)", x, y));
                }
                throw new RuntimeException("Tried to add invalid clause!", e);
            }
        }else{
            println("Can't add an empty clause");
        }
    }


    private int coordinatesToNumber(LogicFieldCell cell){
        return cell.y * nRows + cell.x + 1;
    }

    private void firstMove(){
        println("Initial field:");
        println(field);
        uncover(0,0);
        println("After initial move (0;0):");
        println(field);
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
