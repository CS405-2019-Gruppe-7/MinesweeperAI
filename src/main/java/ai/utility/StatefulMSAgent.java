package ai.utility;

import api.MSAgent;
import api.MSField;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Abstract class with some utility functionality
 * to help build agents
 * @param <CellT> - the type of cell to store in a field.
 *               Must be a subclass of FieldCell, which contains the basic
 *               field information
 */
public abstract class StatefulMSAgent<CellT extends FieldCell> extends MSAgent {

    protected boolean display = false;

    /**
     * Current field state
     */
    private ArrayList<ArrayList<CellT>> currentField;

    protected final int nRows;
    protected final int nColumns;

    public StatefulMSAgent(MSField field, FieldCellFactoryFunction<CellT> cellFactoryFunction) {
        super(field);
        nRows = field.getNumOfRows();
        nColumns = field.getNumOfCols();
        this.currentField = new ArrayList<>();
        for(int y = 0; y < field.getNumOfRows(); y++){
            ArrayList<CellT> row = new ArrayList<>();
            for(int x = 0; x < field.getNumOfCols(); x++){
                CellT cell = cellFactoryFunction.create();
                cell.y = y;
                cell.x = x;
                row.add(cell);
            }
            this.currentField.add(row);
        }
    }

    public CellT uncover(int x, int y){
        int value = field.uncover(x, y);
        CellT result = this.get(x, y);
        if(!result.covered){
            throw new RuntimeException("Can not uncover a field that is already uncovered!");
        }
        result.covered = false;
        if(value == -1){
            result.bomb = true;
        }else{
            result.bombsAround = value;
        }
        return result;
    }

    protected CellT get(int x, int y){
        try{
            CellT cell = this.currentField.get(y).get(x);
            assert cell.x == x && cell.y == y : "The requested cell has a wrong position!";
            return cell;
        }catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    protected void set(int x, int y, CellT cell){
        this.currentField.get(y).set(x, cell);
    }

    protected Stream<CellT> getNeighbours(int x, int y){
        return Stream.of(
                get(x, y+1),
                get(x+1, y+1),
                get(x+1, y),
                get(x+1, y-1),
                get(x, y-1),
                get(x-1, y-1),
                get(x-1, y),
                get(x-1, y+1)
        ).filter(Objects::nonNull);
    }

    protected Stream<CellT> getPossibleMoves(){
        return getAllCells().filter(it -> it.covered);
    }

    protected Stream<CellT> getAllCells(){
        Stream.Builder<CellT> sb = Stream.builder();
        for(int y = 0; y < field.getNumOfRows(); y++){
            for(int x = 0; x < field.getNumOfCols(); x++){
                sb.add(get(x,y));
            }
        }
        return sb.build();
    }

    /**
     * A slow implementation that checks if all fields are uncovered
     * @return is the game over
     */
    protected boolean isGameOver(){
        boolean bombUncovered = getAllCells().anyMatch(it -> it.bomb);
        boolean allCellsUncovered = getAllCells().noneMatch(it -> it.covered && !it.bombFlag);
        return allCellsUncovered || bombUncovered;
    }

    @Override
    public abstract boolean solve();

    @Override
    public void activateDisplay() {
        display = true;
    }

    @Override
    public void deactivateDisplay() {
        display = false;
    }
}
