package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.NodeScores;
import personalizedpagerank.Utility.Parameters;

/**
 * Wrapper of the PageRank class from jgrapht library to store results from
 * multiple runs of the algorithm from different origin nodes.
 */
public class WrappedStoringPageRank implements PersonalizedPageRankAlgorithm
{
    private final DirectedGraph<Integer, DefaultEdge> g;
    private final Parameters parameters;
    private final Int2ObjectOpenHashMap<NodeScores> scores;
    private final Set<Integer> pickedNodes;

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
                VertexScoringAlgorithm<Integer, Double> pr = new PageRank<>(g, parameters.getDamping(), 
                        parameters.getIterations(), parameters.getTolerance(), nodes.get(i));
                Map<Integer, Double> pprScores = pr.getScores();
                //"translate" this map into a NodeScores to satisty the interface
                NodeScores map = new NodeScores(pprScores);
                map.keepTop(smallTop);
                scores.put(nodes.get(i).intValue(), map);
            }
    }
    
    //GETTERS
    ////////////////////

    /**
     * @inheritDoc
     */
    @Override
    public DirectedGraph<Integer, DefaultEdge> getGraph() 
    {
        return g;
    }
    
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
    public NodeScores getMap(final int origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return scores.get(origin);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Int2ObjectOpenHashMap<NodeScores> getMaps()
    {
        return scores;
    }
        
    /**
     * @inheritDoc
     */
    @Override
    public double getRank(final int origin,final int target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return scores.get(origin).get(target);
    }
    
}
