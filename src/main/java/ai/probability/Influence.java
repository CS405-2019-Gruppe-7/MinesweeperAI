package ai.probability;

public class Influence {
    private double probability;
    private int fromX;
    private int fromY;

    public Influence(double probability, int fromX, int fromY) {
        this.probability = probability;
        this.fromX = fromX;
        this.fromY = fromY;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability){
        this.probability = probability;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }
}
