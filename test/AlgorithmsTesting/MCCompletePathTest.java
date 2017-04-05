package AlgorithmsTesting;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.graph.*;
import personalizedpagerank.Algorithms.MCCompletePathPageRank;

import junit.framework.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.jgrapht.DirectedGraph;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;


public class MCCompletePathTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //smallTop = 0
        try 
        {
            new MCCompletePathPageRank(g, 0, 1000, 0.85);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //0 iterations
        try 
        {
            new MCCompletePathPageRank(g, 10, 0, 0.85);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //negative damping factor
        try 
        {
            new MCCompletePathPageRank(g, 10, 100, -0.01);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
      
        //damping factor equal 1
        try 
        {
            new MCCompletePathPageRank(g, 10, 100, 1d);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //dampint factor over 1
        try 
        {
            new MCCompletePathPageRank(g, 10, 100, 1.1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
    }
    
    public void testGetters()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 10, 1000, 0.85);
        
        //node doesn't exist
        try 
        {
            res.getMap(4);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //origin doesn't exist
        try 
        {
            res.getRank(4, 1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //target doesn't exist
        try 
        {
            res.getRank(1, 4);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        Int2DoubleOpenHashMap map1 = res.getMap(1);
        Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> map2 = res.getMaps();
        
        assertEquals(map1.size(), 1, 0);
        assertEquals(map2.size(), 1, 0);
        assertEquals(map2.get(1).size(), 1, 0);
        assertEquals(map1, map2.get(1));
        
    }
    
    public void testEmptyGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 10, 1000, 0.85);
        
        assertEquals(res.getMaps().size(), 0, 0);
    }
    
    public void testReturning0()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 3, 1000, 0.85);
        assertEquals(res.getRank(1, 2), 0d, 0d);
        assertEquals(res.getRank(2, 1), 0d, 0d);
        assertEquals(res.getMap(1).get(2), 0d, 0d);
        assertEquals(res.getMap(2).get(1), 0d, 0d);
    }
    
    public void testNoEdgesGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        for(int i = 0 ; i < 1000; i++)
            g.addVertex(i);
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 1000, 0.85);
        for(int i = 0 ; i < 1000; i++)
        {
            assertEquals(res.getMap(i).size(), 1);        
            assertEquals(res.getMap(i).get(i), 1000d);
        }
    }
    
    public void testConnectedComponentsPairs()
    {
        //a graph where pairs of nodes are connected
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        for(int i = 0 ; i < 1000; i++)
            g.addVertex(i);
        for(int i = 0; i < 999; i += 2)
        {
            g.addEdge(i, i+1);
            g.addEdge(i+1, i);
        }
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 1000, 0.85);
        for(int i = 0 ; i < 999; i += 2)
        {
            assertEquals(res.getMap(i).size(), 2);        
            assertTrue(res.getMap(i).get(i) > 0d);
            assertTrue(res.getMap(i).get(i+1) > 0d);
            assertTrue(res.getMap(i).get(i) >= res.getMap(i).get(i+1));
        }
    }
    
    public void testEdgeToHimself()
    {
        //a graph where pairs of nodes are connected
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        for(int i = 0 ; i < 1000; i++)
        {
            g.addVertex(i);
            g.addEdge(i, i);
        }
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 1000, 0.85);
        for(int i = 0 ; i < 999; i += 2)
        {
            assertEquals(res.getMap(i).size(), 1);        
            assertTrue(res.getMap(i).get(i) >= 1000d);
        }
    }
    
    public void testL()
    {
        //adds 30 nodes and <= 30 random edges, then checks if the returned top 
        //for each node is always <= topL
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        PersonalizedPageRankAlgorithm res;
        for(int i = 0; i < 30; i++)
            g.addVertex(i);
        for(int i = 0; i < 30; i++)
            g.addEdge((int)(Math.random() * 30), (int)(Math.random() * 30));
        for(int i = 1; i < 30; i++)
        {
            res = new MCCompletePathPageRank(g, i, 1000, 0.85);
            for(int u = 0; u < 30; u++)
                assertTrue(res.getMap(u).size() <= i);
        }
    }
    
    public void testSingleNodeGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        //1 node no edges
        g.addVertex(1);
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 100, 0.85);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        assertEquals((Double) res.getRank(1, 1), 100, 0);
    }
    
    public void testTwoNodesGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(1);
        g.addVertex(2);
        
        //no edges first
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 100, 0.85);
        
        assertEquals(res.getRank(1, 2), 0, 0);
        assertEquals(res.getRank(2, 1), 0, 0);
        
        //with edges
        g.addEdge(1, 2);
        g.addEdge(2, 1);
        
        res = new MCCompletePathPageRank(g, 100, 100, 0.85);
        assertEquals(res.getMaps().size(), 2, 0);
        assertEquals(res.getMap(1).size(), 2, 0);
        assertEquals(res.getMap(2).size(), 2, 0);
        
        assertTrue(res.getRank(1, 1) >= res.getRank(1, 2)); 
        assertTrue(res.getRank(2, 2) >= res.getRank(2, 1));
    }
    
    public void testLineGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        for(int i = 0; i < 6; i++)
            g.addVertex(i);
        
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 0);
        
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, 100, 1000, 0.85);

        //for each node check that the score of a node after is lower or equal
        for(int i = 0; i <= 5; i++)
            for(int u = i; u < 5; u++)
                assertTrue(res.getRank(i, u) >= res.getRank(i, u + 1));
    }

    public void testNodesGreaterThanK1()
    {
        //make a graph thats a line
        final int K = 10;
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);     
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 99; i++)
            g.addEdge(i, i + 1);
        g.addEdge(99, 0);
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, K, 1000, 0.85);
        
        for(int i = 0; i < 100; i++)
            assertTrue( res.getMap(i).size() == K);
        
        //for each node check that the score of a node after that is always lower or equal,
        //and the first K-1 nodes in the line are the ones in the top K
        for(int i = 0; i < 100; i++)
            for(int u = i; u < (i + K - 1); u++)
            {
                assertTrue(res.getRank(i, u%100) != 0d);
                assertTrue(res.getRank(i, (u +1)%100) != 0d);
                assertTrue(res.getRank(i, u%100) >= res.getRank(i, (u +1)%100));
            }
    }
    
    public void testNodesGreaterThanK4()
    {
        //test with smallTop being greater than the number of nodes ( g.verteSex().size() * 2 )
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);     
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 99; i++)
            g.addEdge(i, i + 1);
        g.addEdge(99, 0);
        PersonalizedPageRankAlgorithm res = new MCCompletePathPageRank(g, g.vertexSet().size() * 2, 1000, 0.85);
        g.addVertex(1000);
        
        //for each node check that the score of a node after that is always lower or equal,
        //and the first K-1 nodes in the line are the ones in the top K
        for(int i = 0; i < 100; i++)
            for(int u = i; u < (i + 10 - 1); u++)
            {
                assertTrue(res.getRank(i, u%100) != 0d);
                assertTrue(res.getRank(i, (u +1)%100) != 0d);
                assertTrue(res.getRank(i, u%100) > res.getRank(i, (u +1)%100));
                assertEquals(res.getRank(i, 1000), 0d, 0d);
                assertEquals(res.getMap(i).get(1000), 0d, 0d);
            }
    }
}
