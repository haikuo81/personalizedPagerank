/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public final class Graphs 
{
    private Graphs(){}
    
    /**
     * Returns a map mapping each node to an array of integers, which are it's
     * successors. Usually needed to avoid calling org.jgrapht.Graphs.successorListOf
     * many times during the execution of an algorithm since it's kinda slow.
     * @return map of successors for each node
     */
    public static Int2ObjectOpenHashMap<int[]> getSuccessors(DirectedGraph<Integer, DefaultEdge> g)
    {
        Int2ObjectOpenHashMap<int[]> res = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        for(int node: g.vertexSet())
        {
            List<Integer> s = org.jgrapht.Graphs.successorListOf(g, node);
            int[] nodeSuccessors = new int[s.size()];
            int index = 0;
            for(int successor: s)
            {
                nodeSuccessors[index] = successor;
                index++;
            }
            res.put(node, nodeSuccessors);
        }
        return res;
    }

}
