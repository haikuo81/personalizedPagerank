package algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utility.NodeScores;
import utility.Parameters;

/**
 * Wrapper of the PageRank class from jgrapht library to store results from
 * multiple runs of the algorithm from different origin nodes.
 */
public class WrappedStoringPageRank extends PersonalizedPageRankAlgorithm
{
    private final Set<Integer> pickedNodes;
    private final Parameters parameters;
    //CONSTRUCTORS
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g The input graph.
     * @param smallTop How many max entries for each vertex to keep in the final results.
     * @param iterations The number of iterations to perform.
     * @param dampingFactor The damping factor.
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * @param samples Number of nodes for which to run the algorithm.
     */
    public WrappedStoringPageRank(final DirectedGraph<Integer, DefaultEdge> g, final int smallTop,
            final int iterations, final double dampingFactor, final double tolerance, int samples)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        pickedNodes = new HashSet<>(samples);
        
        if(samples < 0) 
            throw new IllegalArgumentException("Number of samples can't be negative");
        
        if(samples > g.vertexSet().size()) 
            throw new IllegalArgumentException("Number of samples can't be greater than total nodes in the graph");
        
        parameters = new Parameters(g.vertexSet().size(), g.edgeSet().size(), 
                iterations, dampingFactor, tolerance);
        
        //pick nodes
        ArrayList<Integer> nodes = new ArrayList<>(g.vertexSet());
        Collections.shuffle(nodes);
        for(int i = 0; i < samples; i++)
            {
                pickedNodes.add(nodes.get(i));
                /*
                VertexScoringAlgorithm<Integer, Double> pr = new PageRank<>(g, parameters.getDamping(), 
                        parameters.getIterations(), parameters.getTolerance(), nodes.get(i));
                Map<Integer, Double> pprScores = pr.getScores();
                //"translate" this map into a NodeScores to satisty the interface
                NodeScores map = new NodeScores(pprScores);
                */
                NodeScores map = PersonalizedPageRank.getScores(g, dampingFactor, iterations, tolerance, nodes.get(i));
                map.keepTop(smallTop);
                map.trim();
                scores.put(nodes.get(i).intValue(), map);
            }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Parameters getParameters() 
    {
        return parameters;
    }
    
    /**
     * Returns the nodes for which the personalized pagerank has been calculated.
     * @return 
     */
    public Set<Integer> getNodes() 
    {
        return pickedNodes;
    }
}
