package UtilityTesting;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import personalizedpagerank.Utility.Result;

/**
 *
 * @author jacopo
 */
public class ResultTest extends TestCase
{
    public void testConstructorAndGetters()
    {
        double[] in = {0d,1d,2d,3d};
        Result res = new Result(in);
        assertEquals(res.getMin(), 0, 0);
        assertEquals(res.getAverage(), 1, 0);
        assertEquals(res.getMax(), 2, 0);
        assertEquals(res.getStd(), 3d, 0);
    }
    
    public void testEqual()
    {
        double[] in1 = {0d,1d,2d,3d};
        double[] in2 = {0d,1d,2d,3d};
        double[] in3 = {-1,1d,2d,3d};
        double[] in4 = {0d,-1d,2d,3d};
        double[] in5 = {0d,1d,-2d,3d};
        double[] in6 = {0d,1d,2d,-3d};
        Result r1 = new Result(in1);
        Result r2 = new Result(in2);
        Result r3 = new Result(in3);
        Result r4 = new Result(in4);
        Result r5 = new Result(in5);
        Result r6 = new Result(in6);
        
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        assertFalse(r1.equals(r3));
        assertFalse(r3.equals(r1));
        
        assertFalse(r1.equals(r4));
        assertFalse(r4.equals(r1));
        
        assertFalse(r1.equals(r5));
        assertFalse(r5.equals(r1));
        
        assertFalse(r1.equals(r6));
        assertFalse(r6.equals(r1));
    }
}
