package AlgorithmsTesting;

import java.util.Map;
import junit.framework.TestCase;
import static junit.framework.TestCase.fail;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import personalizedpagerank.Algorithms.WrappedPageRank;
import personalizedpagerank.PersonalizedPageRankAlgorithm;

public class WrappedPageRankTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //negative damping
        try
        {
            new WrappedPageRank<>(g, 100, -1d, 0, 10);
            fail("this line shouldn't be reached");
        }
        catch(Exception e){}
        
        //damping over 1
        try 
        {
            new WrappedPageRank<>(g, 100, 1.1, 0.0001, 10);
            fail("this line shouldn't be reached");
        } catch (IllegalArgumentException e) {}

        //no iterations
        try 
        {
            new WrappedPageRank<>(g, 0, 0.85, 0.0001, 10);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}

        //negative picked nodes
        try 
        {
            new WrappedPageRank<>(g, 10, 0.5, 0.0, -1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //more picked nodes than nodes in the graph
        try 
        {
            new WrappedPageRank<>(g, 10, 0.5, 0.0, -1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
    }
    
    public void testGetters()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new WrappedPageRank(g, 10, 0.5, 0.0001, 1);
        
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
        
        PersonalizedPageRankAlgorithm res = new WrappedPageRank(g, 10, 0.5, 0.0001, 0);
        
        assertEquals(res.getMaps().size(), 0, 0);
    }
    
    public void testSingleNodeGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        //1 node no edges
        g.addVertex(1);
        PersonalizedPageRankAlgorithm res = new WrappedPageRank(g, 10, 0.5, 0.0001, 1);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        //expected is 1 because as of now nodes without edges always teleport
        assertEquals((Double) res.getRank(1, 1), 1, 0);
        
        //1 node 1 edge to himself
        g.addEdge(1, 1);
        res = new WrappedPageRank(g, 10, 0.5, 0.0001, 1);
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
        PersonalizedPageRankAlgorithm<Integer, Double> res = new WrappedPageRank(g, 10, 0.85, 0.0001, 2);
        
        assertEquals(res.getRank(1, 2), 0, 0);
        assertEquals(res.getRank(2, 1), 0, 0);
        
        //with edges
        g.addEdge(1, 2);
        g.addEdge(2, 1);
        
        res = new WrappedPageRank(g, 10, 0.85, 0.0001, 2);
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
        
        PersonalizedPageRankAlgorithm<Integer, Double> res = new WrappedPageRank(g, 10, 0.85, 0.0001, 6);

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
       
        PersonalizedPageRankAlgorithm<Integer, Double> res = new WrappedPageRank(g, 10, 0.8, 0.0001, 6);

        //expected 1 because as of now nodes with no edges always teleport in Algorithms.PageRank
        assertEquals(res.getRank(0, 0), 1, 0.01);
        for(int i = 1; i < 6; i++)
            assertEquals(res.getRank(0, i), 0d, 0d);

        //connect the center to itself
        g.addEdge(0, 0);
        res = new WrappedPageRank(g, 10, 0.8, 0.0001, 6);
        
        assertEquals(res.getRank(0, 0), 1, 0.01);
        for(int i = 1; i < 6; i++)
            assertEquals(res.getRank(0, i), 0d, 0d);
    }
}
