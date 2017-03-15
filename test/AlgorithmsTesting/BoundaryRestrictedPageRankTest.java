/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package AlgorithmsTesting;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Random;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import personalizedpagerank.Algorithms.BoundaryRestrictedPageRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Algorithms.WrappedStoringPageRank;

public class BoundaryRestrictedPageRankTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //0
        try 
        {
            new BoundaryRestrictedPageRank(g, 0, 0.85, 0.0001, 0.001);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //negative damping factor
        try 
        {
            new BoundaryRestrictedPageRank(g, 100, -0.1d, 0.0001, 0.001);     
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
      
        //damping factor over 1
        try 
        {
            new BoundaryRestrictedPageRank(g, 100, 1.1d, 0.0001, 0.001);     
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
    }
    
    public void testGetters()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        
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
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        
        assertEquals(res.getMaps().size(), 0, 0);
    }
    
    public void testReturning0()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        assertEquals(res.getRank(1, 2), 0d, 0d);
        assertEquals(res.getRank(2, 1), 0d, 0d);
        assertEquals(res.getMap(1).get(2), 0d, 0d);
        assertEquals(res.getMap(2).get(1), 0d, 0d);
    }
    
    public void testSingleNodeGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        //1 node no edges
        g.addVertex(1);
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        assertEquals((Double) res.getRank(1, 1), 1d, 0);
        
        //1 node 1 edge to himself
        g.addEdge(1, 1);
        res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        assertEquals(res.getMaps().size(), 1, 0);
        assertEquals(res.getMap(1).size(), 1, 0);
        assertEquals((Double) res.getRank(1, 1), 1, 0);
    }
    
    public void testTwoNodesGraph()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(1);
        g.addVertex(2);
        
        //no edges first
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 
                0.85, 0.001, 0.001);
        
        assertEquals(res.getRank(1, 2), 0, 0);
        assertEquals(res.getRank(2, 1), 0, 0);
        
        //with edges
        g.addEdge(1, 2);
        g.addEdge(2, 1);
        
        res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
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
        
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);

        //for each node check that the PPR of a node after is always lower
        for(int i = 0; i <= 5; i++)
            for(int u = i; u < 5; u++)
                assertTrue(res.getRank(i, u) > res.getRank(i, u + 1));
    }

    public void testStarGraph()
    {
        //0 is the center which every node points/is connected to
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);     
        for(int i = 0; i < 6; i++)
            g.addVertex(i);
          
        for(int i = 1; i < 6; i++)
            g.addEdge(i, 0);
       
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.8, 0.0001, 0.001);

        assertEquals(res.getRank(0, 0), 1d, 0.01);
        for(int i = 1; i < 6; i++)
        {
            assertEquals(res.getRank(0, i), 0d, 0d);
            assertEquals(res.getRank(i, 0), 0.444, 0.001);
        }

        //connect the center to itself
        g.addEdge(0, 0);
        res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0.0001, 0.001);
        
        assertEquals(res.getRank(0, 0), 1, 0.01);
        for(int i = 1; i < 6; i++)
        {
            assertEquals(res.getRank(0, i), 0d, 0d);
            assertEquals(res.getRank(i, 0), 0.849, 0.001);
        }
    }
    
    public void testNodesGreaterThanK1()
    {
        //make a graph thats a line
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);     
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 99; i++)
            g.addEdge(i, i + 1);
        g.addEdge(99, 0);
        PersonalizedPageRankAlgorithm res = new BoundaryRestrictedPageRank(g, 100, 0.85, 0d, 0d);
        
        //for each node check that the PPR of a node after that is always lower,
        for(int i = 0; i < 100; i++)
            for(int u = i; u < (i + 100 - 1); u++)
            {
                assertTrue(res.getRank(i, u%100) != 0d);
                assertTrue(res.getRank(i, (u +1)%100) != 0d);
                assertTrue(res.getRank(i, u%100) > res.getRank(i, (u +1)%100));
            }
    }
    
    //test that with no tolerance or threshold it orders in the same way as pagerank
    public void testRandomGraph()
    {
        for(int t = 0; t < 500; t++)
        {
            int nodes = 100;
            int edges = 50;
            Random random = new Random();
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            for(int i = 0; i < nodes; i++)
                g.addVertex(i);
            for(int i = 0; i < edges; i++)
                g.addEdge(random.nextInt(nodes), random.nextInt(nodes));

            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 0.85d, 0.0001, nodes);
            PersonalizedPageRankAlgorithm b = new BoundaryRestrictedPageRank(g, 100, 0.85d, 0d, 0d);
            for(int node : g.vertexSet())
            {
                //get maps and sort entries
                Int2DoubleOpenHashMap map1 = pr.getMap(node);
                Int2DoubleOpenHashMap map2 = b.getMap(node);

                Int2DoubleMap.Entry[] m1 = map1.entrySet().toArray(new Int2DoubleMap.Entry[0]);
                Int2DoubleMap.Entry[] m2 = map2.entrySet().toArray(new Int2DoubleMap.Entry[0]);

                //sort entries by values, descending
                Arrays.sort(m1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                        -> {
                    return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                            : e1.getDoubleValue() == e2.getDoubleValue() ?
                            (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
                });
                Arrays.sort(m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                        -> {
                    return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                            : e1.getDoubleValue() == e2.getDoubleValue() ?
                            (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
                });
                for(int i = 0; i < m2.length && m1[i].getDoubleValue() > 0d; i++)
                    assertEquals(m1[i].getIntKey(), m2[i].getIntKey());
            }
        }
    }
    
}
