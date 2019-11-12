package ai.logic;

import ai.utility.FieldCellFactoryFunction;
import ai.utility.StatefulMSAgent;
import api.MSField;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;

public class LogicMSAgent extends StatefulMSAgent<LogicFieldCell> {

    public LogicMSAgent(MSField field) {
        super(field, LogicFieldCell::new);
        ISolver solver = SolverFactory.newDefault();
    }

    @Override
    public boolean solve() {
        return false;
    }
}
