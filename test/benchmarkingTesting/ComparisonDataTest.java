package benchmarkingTesting;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import benchmarking.ComparisonData;
import algorithms.PersonalizedPageRankAlgorithm.Parameters;
import benchmarking.Result;

public class ComparisonDataTest extends TestCase
{
    public void testConstructorAndGetters()
    {
        
        
        double[] in = {0d,1d,2d,3d};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        
        try 
        {
            new ComparisonData(0, r1, r2, p1, p2);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
            new ComparisonData(-1, r1, r2, p1, p2);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        ComparisonData data = new ComparisonData(10, r1, r2, p1, p2);
        assertEquals(data.getMaxEntries(), 10);
        assertSame(r1, data.getJaccard());
        assertSame(r2, data.getKendall());
        assertSame(p1, data.getParam1());
        assertSame(p2, data.getParam2());
    }
    
    public void testDataNotModified()
    {
        double[] in = {0d,1d,2d,3d};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        ComparisonData data = new ComparisonData(10, r1, r2, p1, p2);
        
        assertEquals(data.getParam1().getVertices(), 0, 0);
        assertEquals(data.getParam1().getEdges(), 1, 0);
        assertEquals(data.getParam1().getIterations(), 2, 0);
        assertEquals(data.getParam1().getDamping(), 3d, 0);
        assertEquals(data.getParam1().getTolerance(), 4d, 0);
        
        assertEquals(data.getParam2().getVertices(), 0, 0);
        assertEquals(data.getParam2().getEdges(), 1, 0);
        assertEquals(data.getParam2().getIterations(), 2, 0);
        assertEquals(data.getParam2().getDamping(), 3d, 0);
        assertEquals(data.getParam2().getTolerance(), 4d, 0);
        
        assertEquals(data.getJaccard().getMin(), 0, 0);
        assertEquals(data.getJaccard().getAverage(), 1, 0);
        assertEquals(data.getJaccard().getMax(), 2, 0);
        assertEquals(data.getJaccard().getStd(), 3d, 0);
        
        assertEquals(data.getKendall().getMin(), 0, 0);
        assertEquals(data.getKendall().getAverage(), 1, 0);
        assertEquals(data.getKendall().getMax(), 2, 0);
        assertEquals(data.getKendall().getStd(), 3d, 0);
        
    }
    
    public void testEquals()
    {
        double[] in = {0d,1d,2d,3d};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        ComparisonData d1 = new ComparisonData(10, r1, r2, p1, p2);
        ComparisonData d2 = new ComparisonData(10, r1, r2, p1, p2);
        
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        
        in[0] = 1d;
        r1 = new Result(in);
        d2 = new ComparisonData(10, r1, r2, p1, p2);
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
        
        in[0] = 0d;
        p1 = new Parameters(0, -1, 2, 3d, 4d);
        d2 = new ComparisonData(10, r1, r2, p1, p2);
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
        
        p1 = new Parameters(0, 1, 2, 3d, 4d);
        p2 = new Parameters(0, -1, 2, 3d, 4d);
        d2 = new ComparisonData(10, r1, r2, p1, p2);
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
    }
}
