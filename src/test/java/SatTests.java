import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SatTests {

    @Test
    public void testSat() throws ContradictionException, TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        IVecInt clause = new VecInt();
        clause.push(-1);
        clause.push(-2);
        solver.addClause(clause);
        clause.clear();
        clause.push(-1);
        clause.push(-3);
        solver.addClause(clause);
        clause.clear();
        clause.push(-2);
        clause.push(-3);
        solver.addClause(clause);

        clause.clear();
        clause.push(1);
        clause.push(2);
        clause.push(3);
        solver.addClause(clause);

        clause.clear();
        clause.push(1);
        solver.addClause(clause);

        clause.clear();
        clause.push(2);
        assertFalse(solver.isSatisfiable(clause));

        clause.clear();
        clause.push(3);
        assertFalse(solver.isSatisfiable(clause));

        clause.clear();
        clause.push(1);
        assertTrue(solver.isSatisfiable(clause));
    }

    @Test
    public void testSatContradiction() throws ContradictionException, TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        IVecInt clause = new VecInt();

        clause.clear();
        clause.push(1);
        solver.addClause(clause);

        clause.clear();
        clause.push(-1);
        solver.addClause(clause);
    }
}
