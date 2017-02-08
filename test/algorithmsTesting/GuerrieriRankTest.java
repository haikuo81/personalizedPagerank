
package algorithmsTesting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import personalizedpagerank.algorithms.*;

import junit.framework.*;
import org.jgrapht.DirectedGraph;
import personalizedpagerank.PersonalizedPageRankAlgorithm;


public class GuerrieriRankTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //top L with L = 0
        try 
        {
            new GuerrieriRank<>(g, 0);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //0 iterations
        try 
        {
            new GuerrieriRank<>(g, 10, 0);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //negative dampingfactor
        try 
        {
            new GuerrieriRank<>(g, 10, 10, -0.1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
      
        //dampingfactor over 1
        try 
        {
            new GuerrieriRank<>(g, 10, 10, 1.1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
    }
    
    public void testGetters()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g);
        
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
        
        Map<Integer, Double> map1 = res.getMap(1);
        Map<Integer, Map<Integer, Double>> map2 = res.getMaps();
        
        assertEquals(map1.size(), 1, 0);
        assertEquals(map2.size(), 1, 0);
        assertEquals(map2.get(1).size(), 1, 0);
        //cant use assertSame because getmap and getMaps use Collections.unmodifiableMap
        assertEquals(map1, map2.get(1));
        
    }
    
    public void testEmptyGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g);
        
        assertEquals(res.getMaps().size(), 0, 0);
    }
    
    public void testSingleNodeGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        //1 node no edges
        g.addVertex(1);
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g, 10, 10, 0.5);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        //expected is not 1 because as of now nodes without edges don't always teleport
        assertEquals((Double) res.getRank(1, 1), 0.5, 0);
        
        //1 node 1 edge to himself
        g.addEdge(1, 1);
        res = new GuerrieriRank(g);
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
        PersonalizedPageRankAlgorithm<Integer, Double> res = new GuerrieriRank(g, 10, 100);
        
        assertEquals(res.getRank(1, 2), 0, 0);
        assertEquals(res.getRank(2, 1), 0, 0);
        
        //with edges
        g.addEdge(1, 2);
        g.addEdge(2, 1);
        
        res = new GuerrieriRank(g, 10, 10000);
        assertEquals(res.getMaps().size(), 2, 0);
        assertEquals(res.getMap(1).size(), 2, 0);
        assertEquals(res.getMap(2).size(), 2, 0);
        
        assertTrue(res.getRank(1, 1) >= res.getRank(1, 2)); 
        assertTrue(res.getRank(2, 2) >= res.getRank(2,1));
    }
    
    
}
