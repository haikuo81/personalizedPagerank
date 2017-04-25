package utilityTesting;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Random;
import junit.framework.TestCase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import utility.Graphs;

public class GraphsTest extends TestCase 
{
    Random random = new Random();
    public void testEmptyGraph()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Int2ObjectOpenHashMap<int[]> res = Graphs.getSuccessors(g);
        assertEquals(res.size(), 0);
    }
    
    public void testNoEdges()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        Int2ObjectOpenHashMap<int[]> res = Graphs.getSuccessors(g);
        assertEquals(res.size(), 100);
        for(int i = 0; i < res.size(); i++)
            assertEquals(res.get(i).length, 0);
    }
    
    public void testWithEdges()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 500; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        Int2ObjectOpenHashMap<int[]> res = Graphs.getSuccessors(g);
        for(int i = 0; i < res.size(); i++)
            assertEquals(res.get(i).length, g.outDegreeOf(i));
    }
    
    public void testRightSuccessors()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int i = 0; i < 500; i++)
            g.addEdge(random.nextInt(100), random.nextInt(100));
        Int2ObjectOpenHashMap<int[]> res = Graphs.getSuccessors(g);
        for(int i = 0; i < res.size(); i++)
        {
            int[] successors = res.get(i);
            assertEquals(successors.length, org.jgrapht.Graphs.successorListOf(g, i).size());
            for(int successor: successors)
                assertTrue(org.jgrapht.Graphs.successorListOf(g, i).contains(successor));
        }
    }
    
    public void testCompleteGraph()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        for(int i = 0; i < 100; i++)
            g.addVertex(i);
        for(int node1: g.vertexSet())
            for(int node2: g.vertexSet())
                g.addEdge(node1, node2);
        Int2ObjectOpenHashMap<int[]> res = Graphs.getSuccessors(g);
        int sum = 99 * 100 /2;
        for(int i = 0; i < res.size(); i++)
        {
            int[] successors = res.get(i);
            assertEquals(successors.length, 100);
            int tmpSum = 0;
            for(int u = 0; u < successors.length; u++)
                tmpSum += successors[u];
            assertEquals(tmpSum, sum);
        }
    }

}
