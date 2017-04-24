package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

//class for mapping budgets to nodes 
public final class Budgets 
{
    private Budgets(){}
    
    /**
     * Given a min amount to spend for each node from the "nodes" set and an average amount distribute the 
     * budget for each node, each node will receive at least min as a budget if
     * it has an outdegree greater than 0.
     * If the outdegree of a node is 0 it will receive a budget of 1.
     * If the indegree of  a node is 0 it will receive a budget of "min".
     * The average of the budget for each node will be lower or equal than the 
     * parameter average (lower because some budget may be lost because of rounding)
     * The budget is calculated based on edges, the more outgoing edges
     * a node has the more budget it will receive.
     * @param g Graph containing the nodes from "nodes".
     * @param nodes Set of nodes for which budget is assigned.
     * @param min Min amount of budget to allocate for each node.
     * @param average Average amount of budget for each node.
     * @return A map mapping each node to a budget (an integer value).
     */
    public static Int2IntOpenHashMap degreeBasedBudget(DirectedGraph<Integer, DefaultEdge> g, Set<Integer> nodes, int min, int average)
    {
        if(min < 0)
            throw new IllegalArgumentException("Min budget must not be negative");
        if(min > average)
            throw new IllegalArgumentException("Min greater than average");
        Int2IntOpenHashMap budgets = new Int2IntOpenHashMap(g.vertexSet().size());
        
        //this is the total budget that can be distributed freely, after accounting
        //for the min budget
        double spendible = (average - min) * g.vertexSet().size();
        
        //total value for each input node, will be used to decide the share of 
        //"spendible" each node will receive
        double total = 0;
        for(int node: nodes)
        {
            //if a node outdegree is 0 it will receive 1 as its budget
            //if a node indegree is 0 it will receive "min" as its budget
            //if a node indegree and outdegree are != 0 it will receive a share
            //of spendible + "min" a its budget
            int degree;
            if(g.outDegreeOf(node) == 0)
            {
                degree = 0;
                spendible += min - 1;
            }
            else if(g.inDegreeOf(node) == 0)
                degree = 0;
            else
                degree = g.outDegreeOf(node) + g.inDegreeOf(node);
            budgets.put(node, degree);
            total += degree;
        }
        
        //if no edges or each node either has no indegree or no outdegree just
        //share equally the spendible
        if(total == 0)
        {
            for(int node: nodes)
                budgets.put(node, average);
        }
        else
        {
            for(int node: nodes)
                budgets.put(node, (int) (((g.outDegreeOf(node) == 0)? 1 : min) + spendible * budgets.get(node)/total));
        }
        return budgets;    
    }
}
