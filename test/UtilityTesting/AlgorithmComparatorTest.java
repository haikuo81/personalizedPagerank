package UtilityTesting;

import java.util.Random;
import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.GuerrieriRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Utility.ComparisonData;
import personalizedpagerank.Utility.AlgorithmComparator;



public class AlgorithmComparatorTest extends TestCase
{
    Random random = new Random();
    
    public void testEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        assertTrue(AlgorithmComparator.compare(p, p, g.vertexSet(), new int[0]).length == 0);
    }
    
    public void testNonEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        int[] ks = {1,2,3,4,5,6};
        ComparisonData[] data = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        assertTrue(data.length == ks.length);
        for(int i = 0; i < ks.length; i++)
            assertEquals(data[i].getMaxEntries(), ks[i]);
        
    }
    
    public void testCompareSameAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        int[] ks = {3};
        
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        
        ComparisonData data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), 3);
        ComparisonData data2 = AlgorithmComparator.compare(p, p, g.vertexSet(), 3);
        ComparisonData[] data3 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        
        assertTrue(data1.getParam1().equals(data1.getParam2()));
        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
        
        assertTrue(data1.getParam1().equals(data3[0].getParam2()));
        assertTrue(data1.equals(data3[0]));
        assertTrue(data3[0].equals(data1));
        
        assertEquals(data1.getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1.getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1.getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1.getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1.getLevenstein().getMin(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getAverage(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getMax(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getStd(), 0d, 0.001);
    }
    
    public void testCompareSameAlgorithmBigGraph()
    {
        
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        for(int i = 0; i < 500; i++)
            g.addVertex(i);
        for(int i = 0; i < 500; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        
        ComparisonData data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), 3);
        ComparisonData data2 = AlgorithmComparator.compare(p, p, g.vertexSet(), 3);
        
        assertTrue(data1.getParam1().equals(data1.getParam2()));
        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
        
        assertEquals(data1.getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1.getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1.getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1.getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1.getLevenstein().getMin(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getAverage(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getMax(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getStd(), 0d, 0.001);
    }
    
    public void testCompareDifferentAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p2 = new GuerrieriRank(g, 2, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p3 = new GuerrieriRank(g, 3, 4, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p4 = new GuerrieriRank(g, 3, 3, 101, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p5 = new GuerrieriRank(g, 3, 3, 100, 0.86, 0.0001);
        PersonalizedPageRankAlgorithm p6 = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0005);
        
        ComparisonData data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), 3);
        ComparisonData data4 = AlgorithmComparator.compare(p4, p4, g.vertexSet(), 3);
        ComparisonData data5 = AlgorithmComparator.compare(p5, p5, g.vertexSet(), 3);
        ComparisonData data6 = AlgorithmComparator.compare(p6, p6, g.vertexSet(), 3);
        
        
        assertFalse(data1.equals(data4));
        assertFalse(data4.equals(data1));
        
        assertFalse(data1.equals(data5));
        assertFalse(data5.equals(data1));
        
        assertFalse(data1.equals(data6));
        assertFalse(data6.equals(data1));
        
        assertEquals(data1.getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1.getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1.getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1.getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1.getLevenstein().getMin(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getAverage(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getMax(), 0d, 0.001);
        assertEquals(data1.getLevenstein().getStd(), 0d, 0.001);
    }
}
