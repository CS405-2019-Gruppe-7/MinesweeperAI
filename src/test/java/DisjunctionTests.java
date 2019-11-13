import ai.utility.Utility;
import org.junit.Test;
import org.sat4j.specs.IVecInt;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DisjunctionTests {

    @Test
    public void testDisjunctionGeneration(){
        ArrayList<Integer> literals = new ArrayList<>();
        literals.add(1);
        literals.add(2);
        literals.add(3);
        List<IVecInt> disjunctions = Utility.getAllDisjunctions(literals, 1);
        assertEquals(3, disjunctions.size());

        disjunctions = Utility.getAllDisjunctions(literals, 2);
        assertEquals(3, disjunctions.size());

        disjunctions = Utility.getAllDisjunctions(literals, 3);
        assertEquals(1, disjunctions.size());
    }

    @Test
    public void testDisjunctionGeneration2(){
        ArrayList<Integer> literals = new ArrayList<>();
        literals.add(1);
        literals.add(2);
        literals.add(3);
        literals.add(4);

        List<IVecInt> disjunctions = Utility.getAllDisjunctions(literals, 2);
        assertEquals(6, disjunctions.size());

        disjunctions = Utility.getAllDisjunctions(literals, 3);
        assertEquals(4, disjunctions.size());

        disjunctions = Utility.getAllDisjunctions(literals, 4);
        assertEquals(1, disjunctions.size());
    }

    @Test
    public void testBitsSet(){
        assertEquals(2, Utility.bitsSet(3));
        assertEquals(3, Utility.bitsSet(37));
    }

}
