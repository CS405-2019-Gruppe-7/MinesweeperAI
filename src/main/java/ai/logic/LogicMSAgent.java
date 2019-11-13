package ai.logic;

import ai.utility.FieldCellFactoryFunction;
import ai.utility.StatefulMSAgent;
import api.MSField;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LogicMSAgent extends StatefulMSAgent<LogicFieldCell> {

    ISolver solver;

    public LogicMSAgent(MSField field) {
        super(field, LogicFieldCell::new);
        solver = SolverFactory.newDefault();
    }

    @Override
    public boolean solve() {
        IVecInt clause = new VecInt();
        clause.clear();
        firstMove();

        while(!field.solved()){
            System.out.println("Solving iteration");
            System.out.println("Field:");
            System.out.println(field);
//            solver.clearLearntClauses();
            getAllCells().filter(it -> !it.covered).forEach(this::analyzeCell);
            System.out.println("analyzed");
            List<LogicFieldCell> notBombs = getAllCells()
                    .filter(it -> !it.covered)
                    .flatMap(it -> getNeighbours(it.x, it.y))
                    .filter(it -> it.covered)
                    .filter(it -> {
                    int number = coordinatesToNumber(it);
                    clause.clear();
                    clause.push(-number);
                    try {
                        return solver.isSatisfiable(clause);
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .distinct()
                .collect(toList());

            List<LogicFieldCell> bombs = getAllCells()
                    .filter(it -> !it.covered)
                    .flatMap(it -> getNeighbours(it.x, it.y))
                    .filter(it -> it.covered)
                    .filter(it -> {
                        int number = coordinatesToNumber(it);
                        clause.clear();
                        clause.push(-number);
                        try {
                            return !solver.isSatisfiable(clause);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    .distinct()
                    .collect(toList());

            System.out.println("Found notBombs:" + notBombs.size());
            System.out.println("Found bombs:" + bombs.size());
            System.out.println("Bombs at:");
            bombs.forEach(it -> {
                System.out.println("" + it.x + ";" + it.y);
                pushClause(coordinatesToNumber(it));
            });
            notBombs.forEach(it -> {
                System.out.println(String.format("Uncovering: (%d;%d)", it.x, it.y));
                LogicFieldCell uncovered = uncover(it.x, it.y);
                if(uncovered.bomb){
                    System.out.println(field);
                    System.out.println("BOOM!");
                }
            });
        }
        boolean bombUncovered = getAllCells().anyMatch(it -> it.bomb && !it.covered);
        if(bombUncovered){
            System.out.println("Bomb uncovered!");
        }else{
            System.out.println("Field solved");
        }
        System.out.println(field);
        return !bombUncovered;
    }

    public void analyzeCell(LogicFieldCell cell){
        IVecInt clause = new VecInt();
        if(cell.bombsAround == 0){
            getNeighbours(cell.x, cell.y).forEach(notBomb -> {
                pushClause(-coordinatesToNumber(notBomb));
            });
        }else if (cell.bombsAround==1) {
            bomb1(getNeighbours(cell.x, cell.y).collect(toList()));
            // for 1 bomb (¬A ∨ ¬B) ∧ (¬A ∨ ¬C) ∧ (A ∨ B ∨ C) ∧ (¬B ∨ ¬C)
        } else if (cell.bombsAround==2) {
            bomb2(getNeighbours(cell.x, cell.y).collect(toList()));
        }
    }

    public void bomb1(List<LogicFieldCell> cells) {
            Stream<LogicFieldCell> notBombs = cells.stream().filter(it -> it.x != i.x && it.y != i.y);
            cells.forEach(i -> {

            notBombs.forEach(notBomb -> {
                pushClause(-coordinatesToNumber(i), -coordinatesToNumber(notBomb));
            });
        });
        pushClause(cells.stream().mapToInt(this::coordinatesToNumber).toArray());
    }

//    public List<IVecInt> getAllDisjunctions(ArrayList<Integer> literals, int len){
//        ArrayList<IVecInt> result  = new ArrayList<>();
//        for(int i = 0; i < len; i++){
//            IVecInt clause = new VecInt();
//            clause.push()
//        }
//    }

    public void bomb2(List<LogicFieldCell> cells) {
        Iterator<LogicFieldCell> it = cells.iterator();
        while (it.hasNext()) {
            pushClause(coordinatesToNumber(it.next()));
            it.remove();
            bomb1(cells);
        }
    }

    private void pushClause(int ... literals){
        IVecInt clause = new VecInt();
        for(int l: literals){
            clause.push(l);
        }
        if(!clause.isEmpty()) {
            System.out.println(clause);
            try {
                solver.addClause(clause);
            } catch (ContradictionException e) {
                System.out.println("Contradiction: " + clause);
                e.printStackTrace();
                System.exit(1);
            }
        }else{
            System.out.println("Can't add an empty clause");
        }
    }


    private int coordinatesToNumber(LogicFieldCell cell){
        return cell.y * nRows + cell.x + 1;
    }

    private void firstMove(){
        if(display){
            System.out.println("Initial field:");
            System.out.println(field);
        }
        uncover(0,0);
        if(display){
            System.out.println("After initial move (0;0):");
            System.out.println(field);
        }
    }
}
