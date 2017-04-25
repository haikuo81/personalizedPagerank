package benchmarkingTesting;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Map;
import java.util.Random;
import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import algorithms.GuerrieriRank;
import algorithms.PageRank;
import algorithms.PersonalizedPageRankAlgorithm;
import algorithms.WrappedStoringPageRank;
import benchmarking.ComparisonData;
import benchmarking.AlgorithmComparator;
import benchmarking.NodesComparisonData;



public class AlgorithmComparatorTest extends TestCase
{
    Random random = new Random();
    
    //AlgorithmComparator.compareOrigins tests
    ///////////////////////////////////
    public void testCompareEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        assertTrue(AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), new int[0]).length == 0);
    }
    
    public void testCompareNonEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        int[] ks = {1,2,3,4,5,6};
        ComparisonData[] data = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        assertTrue(data.length == ks.length);
        for(int i = 0; i < ks.length; i++)
            assertEquals(data[i].getMaxEntries(), ks[i]);
        
    }
    
    public void testCompareSameAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        int[] ks = {3};
        
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        
        ComparisonData[] data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        ComparisonData[] data2 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        
        assertTrue(data1[0].getParam1().equals(data1[0].getParam2()));
        assertTrue(data1[0].equals(data2[0]));
        assertTrue(data2[0].equals(data1[0]));
        
        
        assertEquals(data1[0].getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1[0].getKendall().getMin(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getMax(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getStd(), 0d, 0.001);
    }
    
    public void testCompareSameAlgorithmBigGraph()
    {
        
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        int[] ks = {3};
        for(int i = 0; i < 500; i++)
            g.addVertex(i);
        for(int i = 0; i < 500; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        
        ComparisonData[] data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        ComparisonData[] data2 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        
        assertTrue(data1[0].getParam1().equals(data1[0].getParam2()));
        assertTrue(data1[0].equals(data2[0]));
        assertTrue(data2[0].equals(data1[0]));
        
        assertEquals(data1[0].getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1[0].getKendall().getMin(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getMax(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getStd(), 0d, 0.001);
    }
    
    public void testCompareDifferentAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        int[] ks = {3};
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p2 = new GuerrieriRank(g, 2, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p3 = new GuerrieriRank(g, 3, 4, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p4 = new GuerrieriRank(g, 3, 3, 101, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p5 = new GuerrieriRank(g, 3, 3, 100, 0.86, 0.0001);
        PersonalizedPageRankAlgorithm p6 = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0005);
        
        ComparisonData[] data1 = AlgorithmComparator.compare(p, p, g.vertexSet(), ks);
        ComparisonData[] data4 = AlgorithmComparator.compare(p4, p4, g.vertexSet(), ks);
        ComparisonData[] data5 = AlgorithmComparator.compare(p5, p5, g.vertexSet(), ks);
        ComparisonData[] data6 = AlgorithmComparator.compare(p6, p6, g.vertexSet(), ks);
        
        
        assertFalse(data1[0].equals(data4[0]));
        assertFalse(data4[0].equals(data1[0]));
        
        assertFalse(data1[0].equals(data5[0]));
        assertFalse(data5[0].equals(data1[0]));
        
        assertFalse(data1[0].equals(data6[0]));
        assertFalse(data6[0].equals(data1[0]));
        
        assertEquals(data1[0].getJaccard().getMin(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getMax(), 1d, 0.001);
        assertEquals(data1[0].getJaccard().getStd(), 0d, 0.001);
        
        assertEquals(data1[0].getKendall().getMin(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getAverage(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getMax(), 1d, 0.001);
        assertEquals(data1[0].getKendall().getStd(), 0d, 0.001);
    }
    
    //AlgorithmComparator.compareOriginsOriginsTests
    ///////////////////////////////////
    
    public void testCompareOriginsEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        assertTrue(AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), new int[0]).length == 0);
    }
    
    public void testCompareOriginsNonEmptyKs()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        int[] ks = {1,2,3,4,5,6};
        NodesComparisonData[] data = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        assertTrue(data.length == ks.length);
        for(int i = 0; i < ks.length; i++)
            assertEquals(data[i].getMaxEntries(), ks[i]);
    }
    
    public void testCompareOriginsSameAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        int[] ks = {3};
        
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        
        NodesComparisonData[] data1 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        NodesComparisonData[] data2 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        NodesComparisonData[] data3 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        
        assertTrue(data1[0].getParam1().equals(data1[0].getParam2()));
        assertTrue(data1[0].equals(data2[0]));
        assertTrue(data2[0].equals(data1[0]));
        
        assertTrue(data1[0].getParam1().equals(data3[0].getParam2()));
        assertTrue(data1[0].equals(data3[0]));
        assertTrue(data3[0].equals(data1[0]));
        
       for(int i = 0; i < data1[0].getLength(); i++)
        {
            assertEquals(data1[0].getIndegree(i), 0, 0);
            assertEquals(data1[0].getJaccard(i), 1d, 0);
            assertEquals(data1[0].getKendall(i), 1d, 0);
            assertEquals(data1[0].getNeighbourIn(i), 0, 0);
            assertEquals(data1[0].getNeighbourJaccard(i), 0d, 0);
            assertEquals(data1[0].getNeighbourKendall(i), 0d, 0);
            assertEquals(data1[0].getNeighbourOut(i), 0, 0);
            assertEquals(data1[0].getNeighbourPagerank(i), 0, 0);
            assertEquals(data1[0].getNeighbourPagerankError(i), 0, 0);
            assertEquals(data1[0].getOutdegree(i), 0, 0);
            assertEquals(data1[0].getPagerank(i), 0.3333, 0.0001);
            assertEquals(data1[0].getPagerankError(i), 0d, 0);
            assertEquals(data1[0].getExcluded(i), 0, 0);
            assertEquals(data1[0].getIncluded(i), 0, 0);
            assertEquals(data1[0].getNeighbourExcluded(i), 0d, 0);
            assertEquals(data1[0].getNeighbourIncluded(i), 0d, 0);
        }
    }
   
    public void testCompareOriginsSameAlgorithmBigGraph()
    {
        
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        for(int i = 0; i < 500; i++)
            g.addVertex(i);
        for(int i = 0; i < 500; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        Map<Integer, Double> pagerank = (new PageRank<>(g, p.getParameters().getDamping(), 
                p.getParameters().getIterations(), 
                p.getParameters().getTolerance()).getScores());
        
        int[] ks = {3};
        NodesComparisonData[] data1 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        NodesComparisonData[] data2 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        
        assertTrue(data1[0].getParam1().equals(data1[0].getParam2()));
        assertTrue(data1[0].equals(data2[0]));
        assertTrue(data2[0].equals(data1[0]));
        
        for(int i = 0; i < data1[0].getLength(); i++)
        {
            int node = data1[0].getId(i);
            assertEquals(data1[0].getIndegree(i), g.inDegreeOf(node), 0);
            assertEquals(data1[0].getJaccard(i), 1d, 0);
            assertEquals(data1[0].getKendall(i), 1d, 0);
            assertEquals(data1[0].getOutdegree(i), g.outDegreeOf(node), 0);
            assertEquals(data1[0].getPagerank(i), pagerank.get(node), 0.0001);
            assertEquals(data1[0].getPagerankError(i), 0d, 0);
            assertEquals(data1[0].getExcluded(i), 0, 0);
            assertEquals(data1[0].getIncluded(i), 0, 0);
            assertEquals(data1[0].getExcluded(i), 0, 0);
            assertEquals(data1[0].getIncluded(i), 0, 0);
            assertEquals(data1[0].getNeighbourExcluded(i), 0d, 0);
            assertEquals(data1[0].getNeighbourIncluded(i), 0d, 0);
        }
        
        //check stats for nodes that requires neighbour information
        //maps to store jaccard/levenstein/spearman values to avoid calculating them more than once
        Int2DoubleOpenHashMap jMap = new Int2DoubleOpenHashMap(data1[0].getLength());
        Int2DoubleOpenHashMap lMap = new Int2DoubleOpenHashMap(data1[0].getLength());
        Int2DoubleOpenHashMap sMap = new Int2DoubleOpenHashMap(data1[0].getLength());
        jMap.defaultReturnValue(-1);
        lMap.defaultReturnValue(-1);
        sMap.defaultReturnValue(-1);
        
        for(int i = 0; i < data1[0].getLength(); i++)
        {
            int node = data1[0].getId(i);
            boolean skipNeighbourhood = false;
            double in = 0;
            double out = 0;
            double pr = 0;
            int neighbourHood = 0;
            
            //father nodes
            for(DefaultEdge edge: g.incomingEdgesOf(node))
            {
                neighbourHood++;
                int neighbour = Graphs.getOppositeVertex(g, edge, node);
                in += g.inDegreeOf(neighbour);
                out += g.outDegreeOf(neighbour);
                pr += pagerank.get(neighbour);

                //only need to check one of the maps to check if the neighbour
                //is not part of the nodes for which personalized pagerank scores
                //have been calculated
                skipNeighbourhood = skipNeighbourhood || jMap.get(neighbour) == -1;
            }
            
            //children nodes
            for(DefaultEdge edge: g.outgoingEdgesOf(node))
            {
                neighbourHood++;
                int neighbour = Graphs.getOppositeVertex(g, edge, node);
                in += g.inDegreeOf(neighbour);
                out += g.outDegreeOf(neighbour);
                pr += pagerank.get(neighbour);
                
                //only need to check one of the maps to check if the neighbour
                //is not part of the nodes for which personalized pagerank scores
                //have been calculated
                skipNeighbourhood = skipNeighbourhood || jMap.get(neighbour) == -1;
            }
            
            if(neighbourHood > 0)
            {
                in /= neighbourHood;
                out /= neighbourHood;
                pr /= neighbourHood;
            }
            
            assertEquals(data1[0].getNeighbourIn(i), in, 0);
            //could be either 1 or 0 depedending if a node has neighbours or not
            assertTrue(data1[0].getNeighbourJaccard(i) == 0d || data1[0].getNeighbourJaccard(i) == 1d);
            assertTrue(data1[0].getNeighbourKendall(i) == 0d || data1[0].getNeighbourKendall(i) == 1d);
            assertEquals(data1[0].getNeighbourOut(i), out, 0);
            assertEquals(data1[0].getNeighbourPagerank(i), pr, 0d);
            assertEquals(data1[0].getNeighbourPagerankError(i), 0d, 00d);
            assertEquals(data1[0].getExcluded(i), 0, 0);
            assertEquals(data1[0].getIncluded(i), 0, 0);
            assertEquals(data1[0].getNeighbourExcluded(i), 0d, 0);
            assertEquals(data1[0].getNeighbourIncluded(i), 0d, 0);
        }
    }
    
    public void testCompareOriginsDifferentAlgorithm()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        
        PersonalizedPageRankAlgorithm p = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p2 = new GuerrieriRank(g, 2, 3, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p3 = new GuerrieriRank(g, 3, 4, 100, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p4 = new GuerrieriRank(g, 3, 3, 101, 0.85, 0.0001);
        PersonalizedPageRankAlgorithm p5 = new GuerrieriRank(g, 3, 3, 100, 0.86, 0.0001);
        PersonalizedPageRankAlgorithm p6 = new GuerrieriRank(g, 3, 3, 100, 0.85, 0.0005);
        
        int[] ks = {3};
        NodesComparisonData[] data1 = AlgorithmComparator.compareOrigins(p, p, g.vertexSet(), ks);
        NodesComparisonData[] data4 = AlgorithmComparator.compareOrigins(p4, p4, g.vertexSet(), ks);
        NodesComparisonData[] data5 = AlgorithmComparator.compareOrigins(p5, p5, g.vertexSet(), ks);
        NodesComparisonData[] data6 = AlgorithmComparator.compareOrigins(p6, p6, g.vertexSet(), ks);
        
        
        assertFalse(data1[0].equals(data4[0]));
        assertFalse(data4.equals(data1[0]));
        
        assertFalse(data1[0].equals(data5[0]));
        assertFalse(data5[0].equals(data1[0]));
        
        assertFalse(data1[0].equals(data6[0]));
        assertFalse(data6[0].equals(data1[0]));
    }
    
    public void testSameKSameResultCompare()
    {
        for(int k = 1; k < 100; k++)
        {
            int nodes = 100;
            int edges = 3000;
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            for (int i = 0; i < nodes; i++) 
                g.addVertex(i);
            for (int i = 0; i < edges; i++) 
                g.addEdge(random.nextInt(nodes), random.nextInt(nodes));

            int[] ks = {k, k};
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 100, 0.85d, 0.0001, nodes);
            PersonalizedPageRankAlgorithm grank = new GuerrieriRank(g, 100, 100, 50, 0.85, 0.0001);
            ComparisonData[] data = AlgorithmComparator.compare(pr, grank, pr.getNodes(), ks);
            assertTrue(data[0].equals(data[1]));
        }
    }
    
    public void testSameKSameResultCompareOrigins()
    {
        for (int k = 1; k < 100; k++) 
        {
            int nodes = 100;
            int edges = 3000;
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            for (int i = 0; i < nodes; i++) 
                g.addVertex(i);
            for (int i = 0; i < edges; i++) 
                g.addEdge(random.nextInt(nodes), random.nextInt(nodes));

            int[] ks = {k,k};
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 100, 0.85d, 0.0001, nodes);
            PersonalizedPageRankAlgorithm grank = new GuerrieriRank(g, 100, 100, 50, 0.85, 0.0001);
            NodesComparisonData[] data = AlgorithmComparator.compareOrigins(pr, grank, pr.getNodes(), ks);
            assertTrue(data[0].equals(data[1]));
        }
    }
}
