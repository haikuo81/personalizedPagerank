package UtilityTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import personalizedpagerank.Utility.Kendall;

public class KendallTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        double[] x = new double[0];
        double[] y = new double[1];
        
        try 
        {
            Kendall.correlation(x, y, false);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}

        try 
        {
            Kendall.correlation(x, y, true);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
            Kendall.correlation(y, x, true);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
            Kendall.correlation(y, x, false);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
         
        try 
        {
            x = new double[0];
            y = new double[0];
            Kendall.correlation(y, x, false);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
            x = new double[0];
            y = new double[0];
            Kendall.correlation(y, x, true);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
    }
    
    public void test1Element()
    {
        double[] x = new double[1];
        double[] y = new double[1];
        
        assertEquals(1d, Kendall.correlation(x, y, false));
    }
    //tests with no ties
    
    public void testNoTiesFullyConcordant()
    {
        for(int n = 100; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n ; i++)
            {
                x[i] = i;
                y[i] = i;
            }
            assertEquals(1.0, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testNoTiesFullyDiscordant()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n ; i++)
            {
                x[i] = n - i;
                y[i] = i;
            }

            assertEquals(-1.0, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testNoTies0Correlation()
    {
            double[] x = {0, 1, 2, 3};
            double[] y = {0, 1, 0.8, 0.7};

            assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testNoTiesPositiveCorrelation()
    {
            double[] x = {0, 1, 2, 3};
            double[] y = {0, 1, 0.8, 0.9};

            assertEquals(2d/6d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testNoTiesNegativeCorrelation()
    {
            double[] x = {0, 1, 2, 3};
            double[] y = {0, 1, 0.8, -0.5};

            assertEquals(-2d/6d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testShuffledNoTies()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n ; i++)
            {
                x[i] = i;
                y[i] = i;
            }
            
            //shuffle x and y
            ArrayList<Double> arr1 = new ArrayList(Arrays.stream(x).boxed().collect(Collectors.toList()));
            ArrayList<Double> arr2 = new ArrayList(Arrays.stream(y).boxed().collect(Collectors.toList()));
            
            Collections.shuffle(arr1);
            Collections.shuffle(arr2);
            x = arr1.stream().mapToDouble(i -> i).toArray();
            y = arr2.stream().mapToDouble(i -> i).toArray();
            

            assertEquals(lazyMethod(x, y), Kendall.correlation(x, y, false), 0.0001);
        }
        
    }
    
    //tests with ties
    public void testSameX()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n ; i++)
                y[i] = i;

            assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSameY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n ; i++)
                x[i] = i;

            assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSameXY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            assertEquals(1d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameX()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n/2; i++)
            {
                x[i] = -1d;
                y[i] = i;
            }
            for(int i = n/2; i < n; i++)
            {
                x[i] = i;
                y[i] = i;
            }

            assertEquals(expectedResult(n, (n/2d *(n/2d - 1d)/2), 0, 0), Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n/2; i++)
            {
                y[i] = -1d;
                x[i] = i;
            }
            for(int i = n/2; i < n; i++)
            {
                x[i] = i;
                y[i] = i;
            }

            assertEquals(expectedResult(n, 0, (n/2d *(n/2d - 1d)/2), 0), Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameXY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            double[] x = new double[n];
            double[] y = new double[n];

            for(int i = 0; i < n/2; i++)
            {
                y[i] = -1d;
                x[i] = -1d;
            }
            for(int i = n/2; i < n; i++)
            {
                x[i] = i;
                y[i] = i;
            }

            assertEquals(expectedResult(n, (n/2d *(n/2d - 1d)/2),
                    (n/2d *(n/2d - 1d)/2), (n/2d *(n/2d - 1d)/2)),
                    Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public double expectedResult(double n, double sameX, double sameY, double sameXY)
    {
        double totalPairs = (n * (n - 1d))/2d;
        double num = totalPairs - sameX - sameY + sameXY;
        double den = Math.sqrt((totalPairs - sameX) * (totalPairs - sameY));
        return num/den;
    }
    
    //O(n^2) method that doesn't include ties
    public double lazyMethod(double[] x, double[] y)
    {
        int n = x.length;
        double totalPairs = (n * (n - 1d))/2d;
        double res = 0;
        for(int i = 0; i < x.length; i++)
            for(int u = i+1; u < x.length; u++)
                res += Math.signum(x[i] - x[u]) * Math.signum(y[i]- y[u]);
        return res/totalPairs;
    }
}
