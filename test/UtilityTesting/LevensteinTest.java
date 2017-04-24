package UtilityTesting;

import java.util.Comparator;
import java.util.Random;
import junit.framework.TestCase;
import benchmarking.Levenstein;

public class LevensteinTest extends TestCase
{
    final Levenstein<Integer> lev = new Levenstein<>();
    final Random random = new Random();
    
    //tests for method with no comparator
    /////////////////////
    
    public void testBothEmpty()
    {
        Integer[] i1 = {};
        Integer[] i2 = {};
        
        assertEquals(lev.distance(i1, i2), 0, 0);
    }
    
    public void testFirstEmpty()
    {
        Integer[] i1 = {};
        Integer[] i2 = {0,1,2,3};
        
        assertEquals(lev.distance(i1, i2), i2.length, 0);
    }
    
    public void testSecondEmpty()
    {
        Integer[] i1 = {0,1,2,3};
        Integer[] i2 = {};
        
        assertEquals(lev.distance(i1, i2), i1.length, 0);
    }
    
    public void testSame()
    {
        Integer[] i1 = {0,1,2,3,4,5,6,7,8};
        Integer[] i2 = i1;
        
        assertEquals(lev.distance(i1, i2), 0, 0);
    }
    
    public void testSubstitution()
    {
        Integer[] i1 = {0,0,2,3,4,5,6,0,0};
        Integer[] i2 = {0,1,2,3,4,5,6,7,8};
        
        assertEquals(lev.distance(i1 ,i2), 3, 0);
    }
    
    public void testTotallyDifferent()
    {
        Integer[] i1 = {0,0,2,3,4,5,6,0,0};
        Integer[] i2 = {8,8,8,8,8,8,8,8,8};
        
        assertEquals(lev.distance(i1 ,i2), i2.length, 0);
    }
    
    /**
     * Levenstein difference has a lower bound given by the difference between the
     * length of the arguments; also it has a higher bound given by the highest
     * length between the two arguments.
     */
    public void testLowerHigherBound()
    {
        Integer[] i1;
        Integer[] i2;
        int distance;
        for(int times = 0; times < 100; times++)
        {
            i1 = new Integer[random.nextInt(500)];
            i2 = new Integer[random.nextInt(500)];
            for(int i = 0; i < i1.length; i++)
                i1[i] = random.nextInt(50);
            for(int i = 0; i < i2.length; i++)
                i2[i] = random.nextInt(50);
            distance = lev.distance(i1, i2);            
            assertTrue(distance >= Math.abs(i1.length - i2.length));
            assertTrue(distance <= Math.max(i1.length, i2.length));
        }
    }
    
    //tests for method with comparator
    /////////////////////
    
    final Comparator<Integer> comp = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) 
            {
                return (o1.equals(o2))? 0 : -1;
            }
        };
    
    public void testBothEmptyWithComparator()
    {
        Integer[] i1 = {};
        Integer[] i2 = {};
        Integer a;
        
        assertEquals(lev.distance(i1, i2, comp), 0, 0);
    }
    
    public void testFirstEmptyWithComparator()
    {
        Integer[] i1 = {};
        Integer[] i2 = {0,1,2,3};
        
        assertEquals(lev.distance(i1, i2, comp), i2.length, 0);
    }
    
    public void testSecondEmptyWithComparator()
    {
        Integer[] i1 = {0,1,2,3};
        Integer[] i2 = {};
        
        assertEquals(lev.distance(i1, i2, comp), i1.length, 0);
    }
    
    public void testSameWithComparator()
    {
        Integer[] i1 = {0,1,2,3,4,5,6,7,8};
        Integer[] i2 = i1;
        
        assertEquals(lev.distance(i1, i2, comp), 0, 0);
    }
    
    public void testSubstitutionWithComparator()
    {
        Integer[] i1 = {0,0,2,3,4,5,6,0,0};
        Integer[] i2 = {0,1,2,3,4,5,6,7,8};
        
        assertEquals(lev.distance(i1 ,i2, comp), 3, 0);
    }
    
    public void testTotallyDifferentWithComparator()
    {
        Integer[] i1 = {0,0,2,3,4,5,6,0,0};
        Integer[] i2 = {8,8,8,8,8,8,8,8,8};
        
        assertEquals(lev.distance(i1 ,i2, comp), i2.length, 0);
    }
    
    /**
     * Levenstein difference has a lower bound given by the difference between the
     * length of the arguments; also it has a higher bound given by the highest
     * length between the two arguments.
     */
    public void testLowerHigherBoundWithComparator()
    {
        Integer[] i1;
        Integer[] i2;
        int distance;
        for(int times = 0; times < 100; times++)
        {
            i1 = new Integer[random.nextInt(500)];
            i2 = new Integer[random.nextInt(500)];
            for(int i = 0; i < i1.length; i++)
                i1[i] = random.nextInt(50);
            for(int i = 0; i < i2.length; i++)
                i2[i] = random.nextInt(50);
            distance = lev.distance(i1, i2, comp);            
            assertTrue(distance >= Math.abs(i1.length - i2.length));
            assertTrue(distance <= Math.max(i1.length, i2.length));
        }
    }
}
