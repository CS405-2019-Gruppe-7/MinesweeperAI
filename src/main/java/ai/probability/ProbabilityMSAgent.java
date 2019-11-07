package ai.probability;

import ai.utility.FieldCell;
import ai.utility.StatefulMSAgent;
import api.MSAgent;
import api.MSField;

import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class ProbabilityMSAgent extends StatefulMSAgent<ProbabilityFieldCell> {

    Random rng = new Random();

    public ProbabilityMSAgent(MSField field) {
        super(field, ProbabilityFieldCell::new);
    }

    @Override
    public boolean solve() {
        if(display){
            System.out.println("Initial field:");
            System.out.println(field);
        }
        uncover(0,0);
        if(display){
            System.out.println("After initial move (0;0):");
            System.out.println(field);
            System.out.println("Probabilities:");
            printProbabilityField();
        }

        while(!field.solved()){
            ProbabilityFieldCell bestMove = getBestMove();
            if(display){
                System.out.println("=====================");
                System.out.println(String.format("The best selected move is: (%d; %d)", bestMove.x, bestMove.y));
            }
            // the game logic:
            ProbabilityFieldCell cell = uncover(bestMove.x, bestMove.y);

            if(cell.bomb){
                if(display){
                    System.out.println("BOOM!");
                }
                return false;
            }
            if(display){
                System.out.println(String.format("After move (%d; %d):", bestMove.x, bestMove.y));
                System.out.println(field);
                System.out.println("Probabilities:");
                printProbabilityField();
            }
        }
        if(display){
            System.out.println("Solved!");
        }
        return true;
    }

    private ProbabilityFieldCell getBestMove(){
        TreeMap<Double, List<ProbabilityFieldCell>> options = getAllCells()
                .filter(it -> it.covered)
                .collect(groupingBy(ProbabilityFieldCell::getBombProbability,
                TreeMap::new,
                toList()));
        List<ProbabilityFieldCell> bestMoves = options.firstEntry().getValue();
        return bestMoves.get(rng.nextInt(bestMoves.size()));
    }

    @Override
    public ProbabilityFieldCell uncover(int x, int y){
        ProbabilityFieldCell cell = super.uncover(x, y);
        if(!cell.bomb){
            int bombsMarked = (int)getNeighbours(x,y).filter(it -> it.bombFlag).count();
            int neighbourCount = (int)getNeighbours(x,y).filter(it -> !it.notABomb).count();
            getNeighbours(x, y).forEach(c -> {
                if(cell.bombsAround == 0){
                    c.zeroProbability();
                }else{
                    c.setInfluence(x, y, (double)(cell.bombsAround - bombsMarked)/(double)neighbourCount);
                }
            });
        }
        return cell;
    }

    public void printProbabilityField(){
        for(int y = 0; y < this.nRows; y++){
            for(int x = 0; x < this.nColumns; x++){
                double probability = this.get(x, y).getBombProbability();
                System.out.print(String.format("%.2f ", probability));
            }
            System.out.println();
        }
    }

}
