package ai.probability;

import ai.utility.FieldCell;

import java.util.ArrayList;
import java.util.Optional;

public class ProbabilityFieldCell extends FieldCell {
    private ArrayList<Influence> influences = new ArrayList<>();
    public boolean notABomb = false;
    public double getBombProbability(){
        if(notABomb){
            return 0;
        }
        if(influences.size() == 0){
            return 0.5d;
        }
        return influences.stream().mapToDouble(Influence::getProbability).sum();
    }
    public void setInfluence(int fromX, int fromY, double probability){
        Optional<Influence> currentInfluence = influences.stream().filter(it->
                it.getFromX() == fromX && it.getFromY() == fromY
                ).findFirst();
        Influence inf;
        if(currentInfluence.isPresent()){
             inf = currentInfluence.get();
        }else{
            inf = new Influence(probability, fromX, fromY);
            influences.add(inf);
        }
        inf.setProbability(probability);
    }

    public void zeroProbability(){
        notABomb = true;
    }
}
