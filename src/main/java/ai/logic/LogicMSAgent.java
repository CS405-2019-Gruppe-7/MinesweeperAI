package ai.logic;

import ai.utility.FieldCellFactoryFunction;
import ai.utility.StatefulMSAgent;
import api.MSField;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

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
                    clause.push(number);
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
                clause.clear();
                clause.push(-coordinatesToNumber(notBomb));
                System.out.println(clause);
                try {
                    solver.addClause(clause);
                } catch (ContradictionException e) {

                    e.printStackTrace();
                }
            });
        }else {
            // for 1 bomb (¬A ∨ ¬B) ∧ (¬A ∨ ¬C) ∧ (A ∨ B ∨ C) ∧ (¬B ∨ ¬C)
            getNeighbours(cell.x, cell.y).forEach(i -> {
                Stream<LogicFieldCell> notBombs = getNeighbours(cell.x, cell.y).filter(it -> it.x != i.x && it.y != i.y);

                notBombs.forEach(notBomb -> {
                    clause.clear();
                    clause.push(-coordinatesToNumber(i));
                    clause.push(-coordinatesToNumber(notBomb));
                    try {
                        solver.addClause(clause);
                    } catch (ContradictionException e) {
                        e.printStackTrace();
                    }
                });
                clause.clear();
                clause.push(coordinatesToNumber(i));
            });
            try {
                if (!clause.isEmpty())
                    solver.addClause(clause);
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
        }
//        try {
////            System.out.println(clause);
//            solver.addClause(clause);
////            System.out.println("Added clauses!");
//        } catch (ContradictionException e) {
//            e.printStackTrace();
//        }
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
