package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.NodeScores;

public class PersonalizedPageRank 
{
    private PersonalizedPageRank(){}
    
    /**
     * Execute an instance of personalized pagerank starting from the
     * origin.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of PageRank values between
     *        iterations change less than this value
     * @param origin the node for which to run personalized pagerank
     * @return Retrieves a map containing the personalized pagerank scores for a node.
     * Given a node "origin" the map contains pagerank scores of nodes  in the graph
     * as if pagerank was run having the origin node as the only starting node and 
     * the only node in the teleport set.
     */
    public static NodeScores getScores(final DirectedGraph<Integer, DefaultEdge> g, double dampingFactor, int maxIterations, double tolerance, int origin)
    {
        if (maxIterations <= 0) 
        {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) 
        {
            throw new IllegalArgumentException("Damping factor not valid");
        }

        if (tolerance <= 0.0) 
        {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }
        
        if(!g.containsVertex(origin))
        {
            throw new IllegalArgumentException("origin vertex must be part of the grap");
        }

        return run(g, dampingFactor, maxIterations, tolerance, origin);
    }
    
    private static NodeScores run(DirectedGraph<Integer, DefaultEdge> g, double dampingFactor, int maxIterations, double tolerance, int origin)
    {
        NodeScores scores = new NodeScores();
        //init every non origin score to 0 and origin to 1
        for (int node: g.vertexSet()) 
            scores.put(node, 0d);
        scores.put(origin, 1d);
        
        //run PageRank
        NodeScores nextScores = new NodeScores();
        double maxChange = tolerance;
        
        //successors for each node, to avoid calling Graphs.successorListOf which is slow
        Int2ObjectOpenHashMap<int[]> successors = personalizedpagerank.Utility.Graphs.getSuccessors(g);
        
        while (maxIterations > 0 && maxChange >= tolerance) 
        {
            maxChange = 0d;
            nextScores.clear();
            nextScores.addTo(origin, 1 - dampingFactor);

            for (Int2DoubleOpenHashMap.Entry entry: scores.int2DoubleEntrySet()) 
            {
                int node = entry.getIntKey();
                double value = entry.getDoubleValue();
                double factor = dampingFactor/g.outDegreeOf(node);
                
                //add pagerank to every successor of the node
                for (int successor : successors.get(node))
                    nextScores.addTo(successor, value * factor);
            }
            
            //check the highest change in scores
            for(Int2DoubleOpenHashMap.Entry entry: nextScores.int2DoubleEntrySet())
            {
                maxChange = Math.max(maxChange, Math.abs(entry.getDoubleValue() -
                        scores.get(entry.getIntKey())));
            }
            
            //swap scores
            NodeScores tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            maxIterations--;
        }
        return scores;
    }
}
