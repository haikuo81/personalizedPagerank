package utilityTesting;

import junit.framework.TestCase;
import algorithms.PersonalizedPageRankAlgorithm.Parameters;

public class ParametersTest extends TestCase
{
    public void testContructorsAndGetters()
    {
        Parameters p = new Parameters(0, 1, 2, 3d, 4d);
        assertEquals(p.getVertices(), 0, 0);
        assertEquals(p.getEdges(), 1, 0);
        assertEquals(p.getIterations(), 2, 0);
        assertEquals(p.getDamping(), 3d, 0);
        assertEquals(p.getTolerance(), 4d, 0);
    }
    
    public void testEqual()
    {
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p3 = new Parameters(-1, 1, 2, 3d, 4d);
        Parameters p4 = new Parameters(0, -1, 2, 3d, 4d);
        Parameters p5 = new Parameters(0, 1, -2, 3d, 4d);
        Parameters p6 = new Parameters(0, 1, 2, -3d, 4d);
        Parameters p7 = new Parameters(0, 1, 2, 3d, -4d);
        
        
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        
        assertFalse(p1.equals(p3));
        assertFalse(p3.equals(p1));
        
        assertFalse(p1.equals(p4));
        assertFalse(p4.equals(p1));
        
        assertFalse(p1.equals(p5));
        assertFalse(p5.equals(p1));
        
        assertFalse(p1.equals(p6));
        assertFalse(p6.equals(p1));
        
        assertFalse(p1.equals(p7));
        assertFalse(p7.equals(p1));
    }
}
