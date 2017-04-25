package benchmarkingTesting;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;
import benchmarking.Jaccard;

public class JaccardTest extends TestCase
{
    final Jaccard<Integer> jaccard = new Jaccard<>();
    final Random random = new Random();
    
    public void testEmpty()
    {
        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        assertEquals(jaccard.similarity(s1, s1), 1, 0);
        assertEquals(jaccard.similarity(s1, s2), 1, 0);
    }
    
    public void testOneElement()
    {
        Set<Integer> s1 = new HashSet<>();
        s1.add(1);
        Set<Integer> s2 = new HashSet<>();
        assertEquals(jaccard.similarity(s1, s1), 1d, 0d);
        assertEquals(jaccard.similarity(s1, s2), 0d, 0d);
    }
    
    public void testIdenticalPopulatedSets()
    {
        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        for(int i = 0; i < 50; i++)
            s1.add(random.nextInt());
        s2.addAll(s1);
        assertEquals(jaccard.similarity(s1, s2), 1d, 1d);
    }
    
    public void testTotallyDifferentPopulatedSets()
    {
        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        for(int i = 0; i < 100; i++)
        {
            s1.add(i);
            s2.add(i + 100);
        }
        assertEquals(jaccard.similarity(s1, s2), 0d, 0d);
    }
    
    public void testHalfJaccard()
    {
        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        //s1 has 0-4, s2 has 0-9
        for(int i = 0; i < 5; i++)
        {
            s1.add(i);
            s2.add(i + 5);
        }
        s2.addAll(s1);
        assertEquals(jaccard.similarity(s1, s2), 0.5d, 0.0001);
    }
    
    public void testPercentJaccard()
    {
        Set<Integer> s1 = new HashSet<>();
        Set<Integer> s2 = new HashSet<>();
        //init a set with ints from 0 to 99
        for(int i = 0; i < 100; i++)
        {
            s1.add(i);
        }
        //grow s2 10% at a time
        for(int round = 1; round <= 10; round++)
        {
            for(int i = (round - 1) * 10 ; i < round * 10; i++)
                s2.add(i);
            assertEquals(jaccard.similarity(s1,s2), round/10d, 0.0001);
        }
    }
}
