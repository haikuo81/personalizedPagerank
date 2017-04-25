package benchmarkingTesting;

import java.util.ArrayList;
import java.util.Collections;
import junit.framework.TestCase;
import benchmarking.Kendall;

public class KendallTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        
        y.add(1d);
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
            y.clear();
            Kendall.correlation(y, x, false);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
            Kendall.correlation(y, x, true);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
    }
    
    public void test1Element()
    {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(1d);
        y.add(1d);
        
        assertEquals(1d, Kendall.correlation(x, y, false));
    }
    //tests with no ties
    
    public void testNoTiesFullyConcordant()
    {
        for(int n = 100; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n ; i++)
            {
                x.add(i);
                y.add(i);
            }
            assertEquals(1.0, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testNoTiesFullyDiscordant()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n ; i++)
            {
                x.add(n-i);
                y.add(i);
            }

            assertEquals(-1.0, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testNoTies0Correlation()
    {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(0d);
        x.add(1d);
        x.add(2d);
        x.add(3d);
        y.add(0d);
        y.add(1d);
        y.add(0.8);
        y.add(0.7);

        assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testNoTiesPositiveCorrelation()
    {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(0d);
        x.add(1d);
        x.add(2d);
        x.add(3d);
        y.add(0d);
        y.add(1d);
        y.add(0.8);
        y.add(0.9);

        assertEquals(2d/6d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testNoTiesNegativeCorrelation()
    {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(0d);
        x.add(1d);
        x.add(2d);
        x.add(3d);
        y.add(0d);
        y.add(1d);
        y.add(0.8);
        y.add(-0.5);

        assertEquals(-2d/6d, Kendall.correlation(x, y, false), 0.0001);
    }
    
    public void testShuffledNoTies()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();
            
            for(double i = 0; i < n ; i++)
            {
                x.add(i);
                y.add(i);
            }
            
            Collections.shuffle(x);
            Collections.shuffle(y);

            assertEquals(lazyMethod(x, y), Kendall.correlation(x, y, false), 0.0001);
        }
        
    }
    
    //tests with ties
    public void testSameX()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n ; i++)
            {
                x.add(0d);
                y.add(i);
            }
            assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSameY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n ; i++)
            {
                x.add(i);
                y.add(0d);
            }
            assertEquals(0d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSameXY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();
            for(double i = 0; i < n; i++)
            {
                x.add(0d);
                y.add(0d);
            }

            assertEquals(1d, Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameX()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n/2; i++)
            {
                x.add(-1d);
                y.add(i);
            }
            
            for(double i = n/2; i < n; i++)
            {
                x.add(i);
                y.add(i);
            }

            assertEquals(expectedResult(n, (n/2d *(n/2d - 1d)/2), 0, 0), Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(double i = 0; i < n/2; i++)
            {
                y.add(-1d);
                x.add(i);
            }
            for(double i = n/2; i < n; i++)
            {
                x.add(i);
                y.add(i);
            }

            assertEquals(expectedResult(n, 0, (n/2d *(n/2d - 1d)/2), 0), Kendall.correlation(x, y, false), 0.0001);
        }
    }
    
    public void testSomeSameXY()
    {
        for(int n = 10; n < 100000; n *= 10)
        {
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> y = new ArrayList<>();

            for(int i = 0; i < n/2; i++)
            {
                y.add(-1d);
                x.add(-1d);
            }
            for(double i = n/2; i < n; i++)
            {
                x.add(i);
                y.add(i);
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
    public double lazyMethod(ArrayList<Double> x, ArrayList<Double> y)
    {
        int n = x.size();
        double totalPairs = (n * (n - 1d))/2d;
        double res = 0;
        for(int i = 0; i < x.size(); i++)
            for(int u = i+1; u < x.size(); u++)
                res += Math.signum(x.get(i) - x.get(u)) * Math.signum(y.get(i)- y.get(u));
        return res/totalPairs;
    }
}
