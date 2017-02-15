package personalizedpagerank.Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import personalizedpagerank.Parameters;
import personalizedpagerank.PersonalizedPageRankAlgorithm;

/**
 * Wrapper of the PageRank class from jgrapht library to store results from
 * multiple runs of the algorithm from different origin nodes.
 */
public class WrappedPageRank<V, E> implements PersonalizedPageRankAlgorithm<V, Double>
{
    private final DirectedGraph<V, E> g;
    private final Parameters parameters;
    private Map<V, Map<V, Double>> scores;
    private Set<V> pickedNodes;

    //CONSTRUCTORS
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g the input graph
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * @param samples Number of nodes for which to run the algorithm.
     */
    public WrappedPageRank(final DirectedGraph<V, E> g, final int iterations, final double dampingFactor, final double tolerance, int samples)
    {
        this.g = g;
        this.scores = new HashMap<>();
        pickedNodes = new HashSet();
        
        if(samples < 0) 
            throw new IllegalArgumentException("Number of samples can't be negative");
        
        if(samples > g.vertexSet().size()) 
            throw new IllegalArgumentException("Number of samples can't be greater than total nodes in the graph");
        
        parameters = new Parameters(g.vertexSet().size(), g.edgeSet().size(), 
                iterations, dampingFactor, tolerance);
        
        //pick nodes
        ArrayList<V> nodes = new ArrayList<>(g.vertexSet());
        Collections.shuffle(nodes);
        for(int i = 0; i < samples; i++)
            pickedNodes.add(nodes.get(i));
        for(V pick: pickedNodes)
            {
                VertexScoringAlgorithm res2 = new PageRank(g, 0.85, 100, 0.0001, pick);
                scores.put(pick, res2.getScores());
            }
    }
    
    //GETTERS
    ////////////////////

    public Set<V> getNodes() {
        return pickedNodes;
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
     * @inheritDoc
     */
    @Override
    public Map<V, Double> getMap(V origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return Collections.unmodifiableMap(scores.get(origin));
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Map<V, Map<V, Double>> getMaps()
    {
        return Collections.unmodifiableMap(scores);
    }
        
    /**
     * @inheritDoc
     */
    @Override
    public Double getRank(V origin, V target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return (scores.get(origin).get(target) != null)? scores.get(origin).get(target) : 0d;
    }
    
}
