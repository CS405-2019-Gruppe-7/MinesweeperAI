package ai;

import ai.logic.LogicMSAgent;
import ai.probability.ProbabilityMSAgent;
import ai.utility.Requirements;
import api.MSAgent;
import api.MSField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An example of how to use a MSAgent to solve the game. You can do whatever you
 * want with this class.
 */
public class UsageExample {

	/**
	 * Array containing the names of all fields. If you want to iterate over all of
	 * them, this might help.
	 */
	public static String[] fields = { "baby1-3x3-0.txt", "baby2-3x3-1.txt", "baby3-5x5-1.txt", "baby4-5x5-3.txt",
			"baby5-5x5-5.txt", "baby6-7x7-1.txt", "baby7-7x7-3.txt", "baby8-7x7-5.txt", "baby9-7x7-10.txt",
			"anfaenger1-9x9-10.txt", "anfaenger2-9x9-10.txt", "anfaenger3-9x9-10.txt", "anfaenger4-9x9-10.txt",
			"anfaenger5-9x9-10.txt", "fortgeschrittene1-16x16-40.txt", "fortgeschrittene2-16x16-40.txt",
			"fortgeschrittene3-16x16-40.txt", "fortgeschrittene4-16x16-40.txt", "fortgeschrittene5-16x16-40.txt",
			"profi1-30x16-99.txt", "profi2-30x16-99.txt", "profi3-30x16-99.txt", "profi4-30x16-99.txt",
			"profi5-30x16-99.txt" };

	/**
	 * ######################
	 * Used to test a sample list of fields (null/empty = ignored)
	 * ######################
	 */
	public static String[] testFields = {};

	public static void main(String[] args) {

		MSField field;
		MSAgent agent;
		/* solve multiple times and count
		agent.deactivateDisplay();
		*/

		if (testFields != null && testFields.length != 0) {
			fields = testFields;
		}

		Requirements req = new Requirements();
		for (int i = 0; i < fields.length; i++) {
			int iterations = fields[i].contains("profi") ? 100 : 1000;
			int solved = 0;
			long average = 0;

			for (int j = 0; j < iterations; j++) {
				field = new MSField("fields/" + fields[i]);
				agent = new LogicMSAgent(field);
				long time1 = System.currentTimeMillis();

				if (agent.solve()) {
					solved++;
					average += System.currentTimeMillis()-time1;
				}

			}
			if(solved != 0){
				average /= solved;
			}else{
				// there is no meaningful average here
				average = -1;
			}
			String time = String.format("%d,%03d sec",
					TimeUnit.MILLISECONDS.toSeconds(average),
					average - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(average)));

			double prob = ((double)solved/iterations)*100;
			HashMap<String, String> requirements = req.requirements.get(fields[i]);
			System.out.println(fields[i] +
					" | " +solved + "/" + iterations +
					" | Quote: " + prob + "% ("+ requirements.get("probability") +")" +
					" | " + time + " ("+requirements.get("time")+")");
		}

	}

}
