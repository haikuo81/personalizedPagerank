package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Parameters;

/**
 * Wrapper of the PageRank class from jgrapht library to store results from
 * multiple runs of the algorithm from different origin nodes.
 */
public class WrappedPageRank implements PersonalizedPageRankAlgorithm
{
    private final DirectedGraph<Integer, DefaultEdge> g;
    private final Parameters parameters;
    private Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> scores;
    private Set<Integer> pickedNodes;

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
    public WrappedPageRank(final DirectedGraph<Integer, DefaultEdge> g, final int iterations, 
            final double dampingFactor, final double tolerance, int samples)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap(g.vertexSet().size());
        pickedNodes = new HashSet();
        
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
            pickedNodes.add(nodes.get(i));
        for(Integer pick: pickedNodes)
            {
                VertexScoringAlgorithm pr = new PageRank(g, parameters.getDamping(), 
                        parameters.getIterations(), parameters.getTolerance(), pick);
                Map<Integer, Double> pprScores = pr.getScores();
                Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
                map.defaultReturnValue(-1);
                //"translate" this map into a Int2DoubleOpenHashMap to satisty the interface
                for(Integer score: pprScores.keySet())
                    map.put(score, pprScores.get(score));
                scores.put(pick, map);
            }
    }
    
    //GETTERS
    ////////////////////

    public Set<Integer> getNodes() {
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
    public Int2DoubleOpenHashMap getMap(final int origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return scores.get(origin);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> getMaps()
    {
        return scores;
    }
        
    /**
     * @inheritDoc
     */
    public double getRank(final int origin,final int target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return (scores.get(origin).get(target) != -1)? scores.get(origin).get(target) : 0d;
    }
    
}
