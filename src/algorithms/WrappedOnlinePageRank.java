package algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import utility.NodeScores;

/**
 * Wrapper of the PageRank class from jgrapht library to run personalized pagerank.
 * GetMap, getMaps and getRank methods involve calculating the personalized
 * pagerank of the origin node; no results are stored.
 * 
 */
public class WrappedOnlinePageRank extends PersonalizedPageRankAlgorithm
{
    private final Parameters parameters;
    
    //CONSTRUCTORS
    ////////////////////
    
    /**
     * Parameters and graph are saved and used in every future request for personalized
     * pagerank scores.
     * @param g The input graph.
     * @param iterations The number of iterations to perform.
     * @param dampingFactor The damping factor.
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     */
    public WrappedOnlinePageRank(final DirectedGraph<Integer, DefaultEdge> g, final int iterations, 
            final double dampingFactor, final double tolerance)
    {
        this.g = g;
        
        parameters = new Parameters(g.vertexSet().size(), g.edgeSet().size(), 
                iterations, dampingFactor, tolerance);
    }
    
    /**
     * Calculates personalized pagerank scores for a node and returns a map
     * mapping to each node of the graph its value.
     * @param node 
     */
    private NodeScores calculateNode(int node)
    {
        VertexScoringAlgorithm<Integer, Double> pr = new PageRank<>(g, parameters.getDamping(), 
                        parameters.getIterations(), parameters.getTolerance(), node);
        Map<Integer, Double> pprScores = pr.getScores();
        NodeScores map = new NodeScores(pprScores);
        return map;
    }
    
    //GETTERS
    ////////////////////

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
        return calculateNode(origin);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Int2ObjectOpenHashMap<NodeScores> getMaps()
    {
        return new Int2ObjectOpenHashMap<>();
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
        return calculateNode(origin).get(target);
    }
}
