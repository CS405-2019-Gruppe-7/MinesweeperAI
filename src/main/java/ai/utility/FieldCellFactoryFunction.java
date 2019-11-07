package ai.utility;

public interface FieldCellFactoryFunction<CellT extends FieldCell> {
    CellT create();
}
