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
        firstMove();
        return false;
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
