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
    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final int DEFAULT_ITERATIONS = 100;
    
    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final double DEFAULT_DAMPING_FACTOR = 0.85d;

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
        run(DEFAULT_DAMPING_FACTOR, DEFAULT_ITERATIONS);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param iterations the number of iterations to perform
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int iterations)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if (iterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }
        
        run(DEFAULT_DAMPING_FACTOR, iterations);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final double dampingFactor)
    {
        this.g = g;
        this.scores = new HashMap<>();

        run(dampingFactor, DEFAULT_ITERATIONS);
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param iterations the maximum number of iterations to perform
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final double dampingFactor, final int iterations)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if (iterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor not valid");
        }
        
        run(dampingFactor, iterations);
    }
    
    @Override
    public Map<V, Double> getMap(V origin)
    {
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
        return scores.get(origin).get(target);
    }
    
    private void run(double dampingFactor, int iterations)
    {
        int L = 10;
        //init scores
        for(V v: g.vertexSet())
        {
            HashMap<V, Double> tmp = new HashMap<>();
            tmp.put(v, 1d);
            scores.put(v, tmp);
        }
        Map<V, Map<V, Double>> nextScores = new HashMap<>();
        
        while(iterations > 0)
        {
            for(V v: g.vertexSet())
            {
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                HashMap<V, Double> tmp = new HashMap<>();
                tmp.put(v, 1 - dampingFactor);
                nextScores.put(v, tmp);
                
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
                        Double contribution = 
                                dampingFactor * scores.get(successor).get(key) / g.outDegreeOf(v);
                        Double oldRes;
                        //if there was a previously stored value put the sum of that value and the contribution
                        if( (oldRes = nextScores.get(v).putIfAbsent(key, contribution)) != null)
                            nextScores.get(v).put(key, oldRes + contribution);
                    }
                }
                //keep the top L values only (!values = to the 49th place are pruned aswell)
                if(nextScores.get(v).size() > L)
                    nextScores.put(v, pruneLowestLValues(nextScores.get(v),L));
            }
            
            // swap scores
            Map<V, Map<V,Double>> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            nextScores.clear();
            
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
