package UtilityTesting;

import junit.framework.TestCase;
import personalizedpagerank.Utility.Pearson;

public class PearsonTest extends TestCase
{
    public void test0Length()
    {
        double[] x = new double[0];
        double[] y = new double[5];
        
        try 
        {
            Pearson.correlation(x, y);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        x = new double[5];
        y = new double[0];
        try 
        {
            Pearson.correlation(x, y);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
    }
    
    public void testDifferentLengths()
    {
        double[] x = new double[4];
        double[] y = new double[5];
        
        try 
        {
            Pearson.correlation(x, y);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
    }
            
    public void testCorrelated()
    {
        double[] x = {0d,3d,2d,5d,7d,99d};
        assertEquals(Pearson.correlation(x, x), 1d, 0.000001);
    }
    
    public void testAntiCorrelated()
    {
        double[] x = {1d,2d,3d,4d,5d,99d};
        double[] y = {99d,5d,4d,3d,2d,1d};
        assertEquals(Pearson.correlation(x, y), -1d, 0.000001);
    }
    
    public void testUndefinedCorrelation1()
    {
        double[] x = {1d,2d,3d,4d,5d,99d};
        double[] y = {1d,1d,1d,1d,1d,1d};
        assertEquals(Pearson.correlation(x, y), -1d, 0.000001);
    }
    
    public void testZeroCorrelation()
    {
        double[] x = {1d,2d,1d,2d};
        double[] y = {1d,1d,2d,2d};
        assertEquals(Pearson.correlation(x, y), 0, 0.000001);
    }
    
    public void testHalfCorrelation()
    {
        double[] x = {0d,0d,4d,2d};
        double[] y = {0d,0d,2d,4d};
        assertEquals(Pearson.correlation(x, y), 0.5, 0.000001);
    }
    
    public void testNonLinearCorrelation()
    {
        double[] x = {0d,5d,6d,7d};
        double[] y = {0d,250d,1000d,1001d};
        assertEquals(Pearson.correlation(x, y), 0.643598796, 0.000001);
    }
    
    //https://en.wikipedia.org/wiki/Anscombe%27s_quartet
    public void testAnscombesQuarter()
    {
        double[] x1 =  {10d, 8d, 13d, 9d, 11d, 14d, 6d, 4d, 12d, 7d, 5d};
        double[] y1 = {8.04, 6.95, 7.58, 8.81, 8.33, 9.96, 7.24, 4.26, 10.84, 4.82, 5.68};
        assertEquals(Pearson.correlation(x1, y1), 0.816, 0.001);
        
        double[] y2 = {9.14, 8.14, 8.74, 8.77, 9.26, 8.10, 6.13, 3.10, 9.13, 7.26, 4.74};
        assertEquals(Pearson.correlation(x1, y2), 0.816, 0.001);
        
        double[] y3 = {7.46, 6.77, 12.74, 7.11, 7.81, 8.84, 6.08, 5.39, 8.15, 6.42, 5.73};
        assertEquals(Pearson.correlation(x1, y3), 0.816, 0.001);
        
        double [] x2 = {8d, 8d, 8d, 8d, 8d, 8d, 8d, 19d, 8d, 8d, 8d};
        double[] y4 = {6.58, 5.76, 7.71, 8.84, 8.47, 7.04, 5.25, 12.50, 5.56, 7.91, 6.89};
        assertEquals(Pearson.correlation(x2, y4), 0.816, 0.001);
    }
}
