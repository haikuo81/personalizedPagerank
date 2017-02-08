package personalizedpagerank.algorithms;

import static java.lang.Integer.min;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.*;
import personalizedpagerank.PersonalizedPageRankAlgorithm;
/**
 *
 * @author jacopo
 */
public class GuerrieriRank<V, E> implements PersonalizedPageRankAlgorithm<V, Double>
{
    //Default number of max scores to keep for each node if it's not specified in the constructor, if
    //scores[V].size() > max scores the lowest ones gets removed, keeping only the first max scores.
    public static final int DEFAULT_TOP = 10;

    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final int DEFAULT_ITERATIONS = 100;
    
    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final double DEFAULT_DAMPING_FACTOR = 0.85d;
    
    //Default number of tolerance, if the highest difference of scores between 2 iterations is lower
    //than this the algorithm will stop.
    public static final double DEFAULT_TOLERANCE = 0.0001;
    
    private final DirectedGraph<V, E> g;
    private Map<V, Map<V, Double>> scores;
    
    
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     */
    public GuerrieriRank(final DirectedGraph<V, E> g)
    {
        this.g = g;
        this.scores = new HashMap<>();
        run(DEFAULT_TOP, DEFAULT_ITERATIONS, DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param top How many max entries to keep for each vertex (the rest gets removed).
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int top)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if(top <= 0) 
            throw new IllegalArgumentException("Top k entries to keep must be positive");
                
        run(top, DEFAULT_ITERATIONS, DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param top How many max entries to keep for each vertex (the rest gets removed).
     * @param iterations the number of iterations to perform
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int top, final int iterations)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if(top <= 0) 
            throw new IllegalArgumentException("Top k entries to keep must be positive");
                
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
                
        run(top, iterations, DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);
    }
    
    
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param top How many max entries to keep for each vertex (the rest gets removed).
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int top, final int iterations, final double dampingFactor)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if(top <= 0) 
            throw new IllegalArgumentException("Top k entries to keep must be positive");
        
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
            
        run(top, iterations, dampingFactor, DEFAULT_TOLERANCE);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param top How many max entries to keep for each vertex (the rest gets removed).
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * Negative values are allowed to specify that tolerance must be ignored.
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int top, final int iterations, final double dampingFactor, final double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if(top <= 0) 
            throw new IllegalArgumentException("Top k entries to keep must be positive");
        
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
        
        run(top, iterations, dampingFactor, tolerance);
    }

    
    @Override
    public Map<V, Double> getMap(V origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return Collections.unmodifiableMap(scores.get(origin));
    }
    
    @Override
    public Map<V, Map<V, Double>> getMaps()
    {
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public Double getRank(V origin, V target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return (scores.get(origin).get(target) != null)? scores.get(origin).get(target) : 0d;
    }
    
    private void run(final int topL, int iterations, final double dampingFactor, final double tolerance)
    {
        double maxDiff = tolerance;
        //init scores
        Map<V, Map<V, Double>> nextScores = new HashMap<>();
        for(V v: g.vertexSet())
        {
            HashMap<V, Double> tmp = new HashMap<>();
            tmp.put(v, 1d);
            scores.put(v, tmp);
            nextScores.put(v, new HashMap<>());
        }
        
        
        while(iterations > 0 && maxDiff >= tolerance)
        {
            for(V v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                double factor = dampingFactor / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                Map<V, Double> currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.put(v, 1 - dampingFactor);
                
                //for each successor of v
                for(E e: g.outgoingEdgesOf(v))
                {
                    V successor = Graphs.getOppositeVertex(g, e, v);
                    Map<V, Double> successorMap = scores.get(successor);
                    
                    /**
                     * for each value of personalized pagerank (max L values) saved 
                     * in the map  of a successor increment the personalized pagerank of v
                     * of a fraction of it
                     */
                    for(V key: successorMap.keySet())
                    {
                        Double contribution = factor * successorMap.get(key);
                        //if there was a previously stored value put the sum of that value and the contribution
                        //stored will be used to store the new value
                        Double stored = currentMap.get(key);
                        stored = stored == null? contribution : (contribution + stored);
                        currentMap.put(key, stored);
                        
                        //using contribution to store scores.get(v).get(key) (the old value) to call it only once
                        contribution = scores.get(v).get(key);
                        contribution = contribution == null? 0 : contribution;
                        //update maxDiff
                        maxDiff = Math.max(maxDiff, contribution - stored);
                        
                        /* oldversion
                        //update maxDiff
                        if(scores.get(v).get(key) != null)
                            maxDiff = Math.max(maxDiff, Math.abs(scores.get(v).get(key) - stored));
                        else
                            maxDiff = Math.max(maxDiff, stored);
                        */
                    }
                }
                //keep the top L values only (!values = to the 49th place are pruned aswell)
                if(currentMap.size() > topL)
                    nextScores.put(v, pruneLowestLValues(currentMap,topL));
                
            }
            
            // swap scores
            Map<V, Map<V,Double>> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            iterations--;            
        }
        
    }
    
     /**
     * Returns a map where every pair (key, value) that isn't part of the 
     * topL when ordered by value descending is removed.
     * @param unsortMap Starting map.
     * @param topL How many elements to keep from the top.
     * @return The pruned map.
     */
    private Map<V, Double> pruneLowestLValues(final Map<V, Double> unsortMap, final int topL)
    {
        //trasform the map in a list of entries
        List<Map.Entry<V, Double>> list = new LinkedList<>(unsortMap.entrySet());

        //order the entries with the comparator, descending order
        Collections.sort(list, (Map.Entry<V, Double> e1, Map.Entry<V, Double> e2) ->
        {
            return e2.getValue().compareTo(e1.getValue());
        });

        //remove elements that aren't part of the top L
        list.subList(min(topL,list.size()), list.size()).clear();
        
        //insert ordered entries (and keep order) thanks to linked hash map
        Map<V, Double> prunedMap = new HashMap<>();
        for (Map.Entry<V, Double> entry : list)
        {
            prunedMap.put(entry.getKey(), entry.getValue());
        }
        
        return prunedMap;
    }
}
