
package AlgorithmsTesting;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import org.jgrapht.graph.*;
import personalizedpagerank.Algorithms.*;

import junit.framework.*;
import org.jgrapht.DirectedGraph;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;


public class GuerrieriRankTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //smallTop = 0
        try 
        {
            new GuerrieriRank(g, 0, 10, 100, 0.8, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //smallTop > largeTop
        try 
        {
            new GuerrieriRank(g, 10, 5, 100, 0.8, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //0 iterations
        try 
        {
            new GuerrieriRank(g, 10, 5, 0, 0.8, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //negative damping factor
        try 
        {
            new GuerrieriRank(g, 10, 5, 0, -0.1, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
      
        //damping factor over 1
        try 
        {
            new GuerrieriRank(g, 10, 5, 0, 1.1, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
    }
    
    public void testGetters()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 30, 100, 0.85, 0.0001);
        
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
        //cant use assertSame because getmap and getMaps use Collections.unmodifiableMap
        assertEquals(map1, map2.get(1));
        
    }
    
    public void testEmptyGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 30, 100, 0.85, 0.0001);
        
        assertEquals(res.getMaps().size(), 0, 0);
    }
    
    public void testL()
    {
        //adds 30 nodes and <= 30 random edges, then checks if the returned top 
        //for each node is always <= topL
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        PersonalizedPageRankAlgorithm res;
        for(int i = 0; i < 30; i++)
            g.addVertex(i);
        for(int i = 0; i < 30; i++)
            g.addEdge((int)(Math.random() * 30), (int)(Math.random() * 30));
        for(int i = 1; i < 30; i++)
        {
            res = new GuerrieriRank(g, i, i, 100, 0.85, 0.0001);
            for(int u = 0; u < 30; u++)
                assertTrue(res.getMap(u).size() <= i);
        }
    }
    public void testSingleNodeGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        //1 node no edges
        g.addVertex(1);
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 10, 10, 0.5, 0.0001);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        //expected is not 1 because as of now nodes without edges don't always teleport
        assertEquals((Double) res.getRank(1, 1), 0.5, 0);
        
        //1 node 1 edge to himself
        g.addEdge(1, 1);
        res = new GuerrieriRank(g, 10, 30, 100, 0.85, 0.0001);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        assertEquals((Double) res.getRank(1, 1), 1, 0);
    }
    
    public void testTwoNodesGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(1);
        g.addVertex(2);
        
        //no edges first
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 
                100, 100, 0.85, 0.0001);
        
        assertEquals(res.getRank(1, 2), 0, 0);
        assertEquals(res.getRank(2, 1), 0, 0);
        
        //with edges
        g.addEdge(1, 2);
        g.addEdge(2, 1);
        
        res = new GuerrieriRank(g, 10, 10000, 100, 0.85, 0.0001);
        assertEquals(res.getMaps().size(), 2, 0);
        assertEquals(res.getMap(1).size(), 2, 0);
        assertEquals(res.getMap(2).size(), 2, 0);
        
        assertTrue(res.getRank(1, 1) >= res.getRank(1, 2)); 
        assertTrue(res.getRank(2, 2) >= res.getRank(2, 1));
    }
    
    public void testLineGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        for(int i = 0; i < 6; i++)
            g.addVertex(i);
        
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 0);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 100, 100, 0.85, 0.0001);

        //for each node check that the PPR of a node after is always lower
        for(int i = 0; i <= 5; i++)
            for(int u = i; u < 5; u++)
                assertTrue(res.getRank(i, u) > res.getRank(i, u + 1));
    }

    public void testStarGraph()
    {
        //0 is the center which every node points/is connected to
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);     
        for(int i = 0; i < 6; i++)
            g.addVertex(i);
          
        for(int i = 1; i < 6; i++)
            g.addEdge(i, 0);
       
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 10, 10, 0.8, 0.0001);

        assertEquals(res.getRank(0, 0), 0.2, 0.01);
        for(int i = 1; i < 6; i++)
        {
            assertEquals(res.getRank(0, i), 0d, 0d);
            //0.2 because 0 has no edges, 0.2 * 0.8 because dampingfactor is taken into consideration
            //when doing contribution from one node to another
            assertEquals(res.getRank(i, 0), 0.2 * 0.8, 0.01);
        }

        //connect the center to itself
        g.addEdge(0, 0);
        res = new GuerrieriRank(g, 10, 10, 10, 0.8, 0.0001);
        
        assertEquals(res.getRank(0, 0), 1, 0.01);
        for(int i = 1; i < 6; i++)
        {
            assertEquals(res.getRank(0, i), 0d, 0d);
            assertEquals(res.getRank(i, 0), 0.8, 0.01);
        }
    }
}
