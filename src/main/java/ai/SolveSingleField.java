package ai;

import ai.logic.LogicMSAgent;
import api.MSAgent;
import api.MSField;

public class SolveSingleField {

    /**
     * Array containing the names of all fields. If you want to iterate over all of
     * them, this might help.
     */
    private static final String[] fields = { "baby1-3x3-0.txt", "baby2-3x3-1.txt", "baby3-5x5-1.txt", "baby4-5x5-3.txt",
            "baby5-5x5-5.txt", "baby6-7x7-1.txt", "baby7-7x7-3.txt", "baby8-7x7-5.txt", "baby9-7x7-10.txt",
            "anfaenger1-9x9-10.txt", "anfaenger2-9x9-10.txt", "anfaenger3-9x9-10.txt", "anfaenger4-9x9-10.txt",
            "anfaenger5-9x9-10.txt", "fortgeschrittene1-16x16-40.txt", "fortgeschrittene2-16x16-40.txt",
            "fortgeschrittene3-16x16-40.txt", "fortgeschrittene4-16x16-40.txt", "fortgeschrittene5-16x16-40.txt",
            "profi1-30x16-99.txt", "profi2-30x16-99.txt", "profi3-30x16-99.txt", "profi4-30x16-99.txt",
            "profi5-30x16-99.txt" };

    public static void main(String[] args){
        //		 solve and print
		MSField field = new MSField("fields/" + fields[18]);
		MSAgent agent = new LogicMSAgent(field);
		agent.activateDisplay();
		agent.solve();
    }
}
