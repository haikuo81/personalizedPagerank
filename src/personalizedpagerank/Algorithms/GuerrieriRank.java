package personalizedpagerank.Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import personalizedpagerank.Parameters;
import personalizedpagerank.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Utility.PartialSorter;

/**
 * Runs an instance of GuerrieriRank, which runs an approximation of pagerank
 * for each node in the graph, obtaining personalized pagerank scores for each node.
 * For I iterations (or until convergence) for each edge pagerank score is passed
 * from a child node to an ancestor. For each node only the top "largeTop" scores of 
 * personalized pagerank (as if that node was the origin and only node of the
 * teleport set) are kept, while the rest is pruned.
 * After calculations another round of pruning is executed, for each node 
 * only the "smallTop" scores are kept.
 * Note that the returned results may not be ordered.
 * The complexity is O(I *|Edges| * L).
 * @param <V> Object representing the nodes of the graph for which scores will be computed.
 * @param <E> Object representing the edges of the graph for which scores will be computed.
 */
public class GuerrieriRank<V, E> implements PersonalizedPageRankAlgorithm<V, Double>
{
    //Default number of scores to return for each node, after doing calculations with
    //the LARGE_TOP only the small top will be kept as a valid result.
    public static final int DEFAULT_SMALL_TOP = 10;
    
    //Default number of max scores to keep for each during computation node if it's 
    //not specified in the constructor, if scores[V].size() > max scores the lowest 
    //ones gets removed, keeping only the first max scores.
    public static final int DEFAULT_LARGE_TOP = 30;

    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final int DEFAULT_ITERATIONS = 100;
    
    //Default number of maximum iterations to be used if it's not specified in the constructor.
    public static final double DEFAULT_DAMPING_FACTOR = 0.85d;
    
    //Default number of tolerance, if the highest difference of scores between 2 iterations is lower
    //than this the algorithm will stop.
    public static final double DEFAULT_TOLERANCE = 0.0001;
    
    private final DirectedGraph<V, E> g;
    private Map<V, Map<V, Double>> scores;
    private final PartialSorter<V> sorter;
    private final GuerrieriParameters parameters;

    
    
    //Private class to store running parameters
    public class GuerrieriParameters extends Parameters
    {
        private final int smallTop;
        private final int largetTop;
        
        private GuerrieriParameters(final int vertices, final int edges, final int smallTop, 
                final int largeTop, final int iterations, final double damping, final double tolerance)
        {
            super(vertices, edges, iterations, damping, tolerance);
            this.smallTop = smallTop;
            this.largetTop = largeTop;
        }

        private GuerrieriParameters(GuerrieriParameters input)
        {
            super(input.getVertices(), input.getEdges(), input.getIterations(), 
                    input.getDamping(), input.getTolerance());
            this.smallTop = input.smallTop;
            this.largetTop = input.largetTop;
        }
                
        public int getSmallTop() {
            return smallTop;
        }

        public int getLargetTop() {
            return largetTop;
        }
    }
    
    
    
    //CONSTRUCTORS
    ////////////////////
    
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     */
    public GuerrieriRank(final DirectedGraph<V, E> g)
    {
        this.sorter = new PartialSorter<>();
        this.g = g;
        this.scores = new HashMap<>(g.vertexSet().size());
        
        parameters = new GuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                DEFAULT_SMALL_TOP, DEFAULT_LARGE_TOP, DEFAULT_ITERATIONS, 
                DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);
        
        run();
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int smallTop, final int largeTop)
    {
        this.sorter = new PartialSorter<>();
        this.g = g;
        this.scores = new HashMap<>(g.vertexSet().size());

        if(smallTop <= 0)
            throw new IllegalArgumentException("SmallTop k entries to keep must be positive");
        
        if(largeTop <= 0) 
            throw new IllegalArgumentException("LargeTop k entries to keep must be positive");
        
        if(smallTop > largeTop)
            throw new IllegalArgumentException("SmallTop can't be greater than largeTop");
        
        parameters = new GuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, largeTop, DEFAULT_ITERATIONS, DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);   
        
        run();
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     * @param iterations the number of iterations to perform
     *       
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int smallTop, final int largeTop, final int iterations)
    {
        this.sorter = new PartialSorter<>();
        this.g = g;
        this.scores = new HashMap<>(g.vertexSet().size());

        if(smallTop <= 0)
            throw new IllegalArgumentException("SmallTop k entries to keep must be positive");
        
        if(largeTop <= 0) 
            throw new IllegalArgumentException("LargeTop k entries to keep must be positive");
        
        if(smallTop > largeTop)
            throw new IllegalArgumentException("SmallTop can't be greater than largeTop");
                
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        parameters = new GuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, largeTop, iterations, DEFAULT_DAMPING_FACTOR, DEFAULT_TOLERANCE);  
        
        run();
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * 
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int smallTop, final int largeTop, final int iterations, final double dampingFactor)
    {
        this.sorter = new PartialSorter<>();
        this.g = g;
        this.scores = new HashMap<>(g.vertexSet().size());

        if(smallTop <= 0)
            throw new IllegalArgumentException("SmallTop k entries to keep must be positive");
        
        if(largeTop <= 0) 
            throw new IllegalArgumentException("LargeTop k entries to keep must be positive");
        
        if(smallTop > largeTop)
            throw new IllegalArgumentException("SmallTop can't be greater than largeTop");
                
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
            
        parameters = new GuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, largeTop, iterations, dampingFactor, DEFAULT_TOLERANCE);  
        
        run();
    }
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * Negative values are allowed to specify that tolerance must be ignored.
     */
    public GuerrieriRank(final DirectedGraph<V, E> g, final int smallTop, final int largeTop, final int iterations, final double dampingFactor, final double tolerance)
    {
        this.sorter = new PartialSorter<>();
        this.g = g;
        this.scores = new HashMap<>(g.vertexSet().size());

        if(smallTop <= 0)
            throw new IllegalArgumentException("SmallTop k entries to keep must be positive");
        
        if(largeTop <= 0) 
            throw new IllegalArgumentException("LargeTop k entries to keep must be positive");
        
        if(smallTop > largeTop)
            throw new IllegalArgumentException("SmallTop can't be greater than largeTop");
        
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
        
        parameters = new GuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, largeTop, iterations, dampingFactor, tolerance);
        
        run();
    }
    
    
    //GETTERS
    ////////////////////
    
    /**
     * @inheritDoc
     */
    @Override
    public Parameters getParameters() 
    {
        return new GuerrieriParameters(this.parameters);
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
    
    //methods (no getters)
    ////////////////////
    
    /**
     * Executes the algorithm, this.scores will store the results.
     */
    private void run()
    {
        int iterations = this.parameters.getIterations();
        double maxDiff = this.parameters.getTolerance();
        //init scores
        Map<V, Map<V, Double>> nextScores = new HashMap<>(g.vertexSet().size());
        for(V v: g.vertexSet())
        {
            HashMap<V, Double> tmp = new HashMap<>();
            tmp.put(v, 1d);
            scores.put(v, tmp);
            nextScores.put(v, new HashMap<>());
        }
        
        while(iterations > 0 && maxDiff >= this.parameters.getTolerance())
        {
            for(V v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                double factor = this.parameters.getDamping() / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                Map<V, Double> currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.put(v, 1 - this.parameters.getDamping());
                
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
                if(currentMap.size() > this.parameters.largetTop)
                    keepTopL3(currentMap, this.parameters.largetTop);
            }
            // swap scores
            Map<V, Map<V,Double>> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            iterations--;            
        }
        //keep smalltop only
        for(V v: scores.keySet())
        {
            if(scores.get(v).size() > this.parameters.smallTop)
                keepTopL3(scores.get(v), this.parameters.smallTop);
        }
    }
    
    
    //keeping 3 versions of it for profiling and other purposes
    
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
        sorter.partialSort(values, topL);
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
        sorter.partialSort(values, topL);
        input.clear();
        for(int i = 0; i < topL; i++)
            input.put(values[i].getKey(), values[i].getValue());
    }
   
}
