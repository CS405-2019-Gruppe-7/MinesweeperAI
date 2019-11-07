package ai.utility;

public class FieldCell {
    public boolean covered = true;
    public boolean bombFlag = false;
    public boolean bomb = false;
    public int bombsAround = 0;

    public int x;
    public int y;
}
