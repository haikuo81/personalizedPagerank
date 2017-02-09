package personalizedpagerank.algorithms;

import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
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
                     * of a fraction of it.
                     */
                    for(V key: successorMap.keySet())
                    {
                        Double contribution = factor * successorMap.get(key);
                        //if there was a previously stored value put the sum of that value and the contribution
                        //stored will be used to store the new value
                        Double stored = (stored = currentMap.get(key)) != null? (contribution + stored) : contribution;
                        currentMap.put(key, stored);
                        //using contribution to store scores.get(v).get(key) (the old value) to call it only once
                        contribution = (contribution = scores.get(v).get(key)) != null? contribution : 0;
                        //update maxDiff
                        maxDiff = Math.max(maxDiff, Math.abs(contribution - stored));
                    }
                }
                //keep the top L values only
                if(currentMap.size() > topL)
                    keepTopL3(currentMap, topL);
            }
            // swap scores
            Map<V, Map<V,Double>> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            iterations--;            
        }
        
    }
    
     /**
     * Keeps the topL entries of the map, descending order based on values.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL1(Map<V, Double> input, final int topL)
    {
        //trasform the map in a list of entries
        ArrayList<Map.Entry<V, Double>> list = new ArrayList<>(input.entrySet());

        //order the entries with the comparator, descending order
        Collections.sort(list, (Map.Entry<V, Double> e1, Map.Entry<V, Double> e2) ->
        {
            return e2.getValue().compareTo(e1.getValue());
        });
        input.clear();
        for(int i = 0; i < topL; i++)
            input.put(list.get(i).getKey(), list.get(i).getValue());
    }
    
    /**
     * Keeps the topL entries of the map, after finding the value of the Lth 
     * element if they were ordered by values (descending). First pass is inclusive
     * of entries with values = lth, if the size of the result exceeds L 
     * then a second pass is done to remove entries with values = lth.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL2(Map<V, Double> input, final int topL)
    {
        Map.Entry<V, Double>[] values = input.entrySet().toArray(new Map.Entry[0]);
        partialSort(values, topL);
        Double lth = values[topL].getValue();
        input.entrySet().removeIf(e-> e.getValue() < lth );
        if(input.size() > topL)
            input.entrySet().removeIf(e-> e.getValue().equals(lth) && input.size() > topL );
    }
    
    /**
     * Keeps the topL entries of the map, based on a partial order on the Lth element.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL3(Map<V, Double> input, final int topL)
    {
        Map.Entry<V, Double>[] values = input.entrySet().toArray(new Map.Entry[0]);
        partialSort(values, topL);
        input.clear();
        for(int i = 0; i < topL; i++)
            input.put(values[i].getKey(), values[i].getValue());
    }
    
    /**
     * Partially sorts the array using selection sort.
     * Values greater than the nth value (the value that would be on the nth
     * position if the array was sorted by entry.value in descending order) will
     * be on the left, and the lower values on the right.
     * @param input Input array of entries.
     * @param n 
     */
    private void partialSort(Map.Entry<V, Double>[] input, int n) 
    {
        if (n >= input.length)
            throw new IllegalArgumentException("N must be lower than the length of the input");
        int from = 0, to = input.length - 1;

        while (from < to) 
        {
            int leftIndex = from, rightIndex = to;
            Map.Entry<V, Double> mid = input[(leftIndex + rightIndex) / 2];
            
            while (leftIndex < rightIndex) 
            {
                /*
                if the value is greater than the pivot move it on the right 
                side by swapping it with the value at rightIndex, else move on
                */
                if (input[leftIndex].getValue() <= mid.getValue()) 
                { 
                    Map.Entry<V, Double> tmp = input[rightIndex];
                    input[rightIndex] = input[leftIndex];
                    input[leftIndex] = tmp;
                    rightIndex--;
                } 
                else
                    leftIndex++;
            }
            if (input[leftIndex].getValue() < mid.getValue())
                leftIndex--;
            //change to or from depending if what we are looking for is on the left or right part
            if (n <= leftIndex) 
                to = leftIndex;
            else 
                from = leftIndex + 1;
        }
    }
}
