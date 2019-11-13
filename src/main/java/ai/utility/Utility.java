package ai.utility;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static List<IVecInt> getAllDisjunctions(List<Integer> literals, int len){
        ArrayList<IVecInt> result  = new ArrayList<>();
        int i = 0;
        while(i < 1 << (literals.size())){
            if(bitsSet(i) == len){
                IVecInt clause = new VecInt();
                int index = 0;
                int tmp = i;
                while(tmp != 0){
                    if((tmp & 1) == 1){
                        clause.push(literals.get(index));
                    }
                    tmp = tmp >> 1;
                    index++;
                }
                result.add(clause);
            }
            i++;
        }
        return result;
    }

    public static int bitsSet(int number){
        int count = 0;
        while(number != 0){
            count += number & 1;
            number = number >> 1;
        }
        return count;
    }
}
