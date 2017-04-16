/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package UtilityTesting;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Random;
import junit.framework.TestCase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import personalizedpagerank.Utility.Budgets;

public class BudgetsTest extends TestCase
{
    Random random = new Random();
    
    public void testEmptyGraph()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 0, 0);
        assertEquals(map.size(), 0);
    }
    
    public void test1ResultForEachNodeDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 0, 0);
        assertEquals(map.size(), 100);
        for(int i = 0; i < 100; i++)
            assertEquals(map.get(i), 0);
    }
    
    public void testMinAverageEqualDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 99; i++)
        {
            g.addEdge(i, i+1);
            g.addEdge(i+1, i);
        }
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 10);
        assertEquals(map.size(), 100);
        for(int i = 0; i < 100; i++)
            assertEquals(map.get(i), 10);
    }
    
    public void testMinAverageDifferentMinRespectedDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        assertEquals(map.size(), 100);
        for(int i = 0; i < 100; i++)
        {
            if(g.outDegreeOf(i) == 0)
                assertEquals(map.get(i) , 1);
            else
                assertTrue(map.get(i) >= 10);
        }
    }
    
    public void testMinAverageDifferentAverageRespectedDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        assertEquals(map.size(), 100);
        double average = 0;
        for(int i = 0; i < 100; i++)
            average += map.get(i);
        average /= g.vertexSet().size();
        assertTrue(average <= 20);
    }
    
    public void testGreaterDegreeGreaterBudgetDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        for(int node1: g.vertexSet())
            for(int node2: g.vertexSet())
            {
                int d1 = g.outDegreeOf(node1) + g.inDegreeOf(node1);
                int d2 = g.outDegreeOf(node2) + g.inDegreeOf(node2);
                
                if(g.outDegreeOf(node1) == 0)
                {
                    assertEquals(map.get(node1) , 1);
                    assertTrue(map.get(node1) <= map.get(node2));
                }
                else if(g.outDegreeOf(node2) == 0)
                {
                    assertEquals(map.get(node2) , 1);
                    assertTrue(map.get(node2) <= map.get(node1));
                }
                else if(g.inDegreeOf(node1) == 0)
                {
                    assertEquals(map.get(node1) , 10);
                    assertTrue(map.get(node1) <= map.get(node2));
                }
                else if(g.inDegreeOf(node2) == 0)
                {
                    assertEquals(map.get(node2) , 10);
                    assertTrue(map.get(node2) <= map.get(node1));
                }
                else if(d1 >= d2)
                    assertTrue(map.get(node1) >= map.get(node2));
                else//>= due to roundings that might make them equal
                    assertTrue(map.get(node2) >= map.get(node1));
            }
    }
    
    public void testMinAverageEqualUndirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 99; i++)
        {
            g.addEdge(i, i+1);
            g.addEdge(i+1, i);
        }
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 10);
        assertEquals(map.size(), 100);
        for(int i = 0; i < 100; i++)
            assertEquals(map.get(i), 10);
    }
    
    public void testMinAverageDifferentMinRespectedUndirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
        {
            int node1 = random.nextInt(100);
            int node2 = random.nextInt(100);
            g.addEdge(node1, node2);
        }
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        assertEquals(map.size(), 100);
        for(int i = 0; i < 100; i++)
        {
            if(g.outDegreeOf(i) == 0)
                assertEquals(map.get(i) , 1);
            else
                assertTrue(map.get(i) >= 10);
        }
    }
    
    public void testMinAverageDifferentAverageRespectedUndirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
        {
            int node1 = random.nextInt(100);
            int node2 = random.nextInt(100);
            g.addEdge(node1, node2);
        }
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        assertEquals(map.size(), 100);
        double average = 0;
        for(int i = 0; i < 100; i++)
            average += map.get(i);
        average /= g.vertexSet().size();
        assertTrue(average <= 20);
    }
    
    public void testGreaterDegreeGreaterBudgetUndirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 100; i++)
        {
            int node1 = random.nextInt(100);
            int node2 = random.nextInt(100);
            g.addEdge(node1, node2);
        }
        Int2IntOpenHashMap map = Budgets.degreeBasedBudget(g, g.vertexSet(), 10, 20);
        for(int node1: g.vertexSet())
            for(int node2: g.vertexSet())
            {
                int d1 = g.outDegreeOf(node1) + g.inDegreeOf(node1);
                int d2 = g.outDegreeOf(node2) + g.inDegreeOf(node2);
                
                if(g.outDegreeOf(node1) == 0)
                {
                    assertEquals(map.get(node1) , 1);
                    assertTrue(map.get(node1) <= map.get(node2));
                }
                else if(g.outDegreeOf(node2) == 0)
                {
                    assertEquals(map.get(node2) , 1);
                    assertTrue(map.get(node2) <= map.get(node1));
                }
                else if(g.inDegreeOf(node1) == 0)
                {
                    assertEquals(map.get(node1) , 10);
                    assertTrue(map.get(node1) <= map.get(node2));
                }
                else if(g.inDegreeOf(node2) == 0)
                {
                    assertEquals(map.get(node2) , 10);
                    assertTrue(map.get(node2) <= map.get(node1));
                }
                else if(d1 >= d2)
                    assertTrue(map.get(node1) >= map.get(node2));
                else//>= due to roundings that might make them equal
                    assertTrue(map.get(node2) >= map.get(node1));
            }
    }
}
