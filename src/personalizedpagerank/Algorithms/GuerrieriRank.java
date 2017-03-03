package personalizedpagerank.Algorithms;

import personalizedpagerank.Utility.Parameters;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.PartialSorter;

 /**
+ * Runs an instance of GuerrieriRank, which runs an approximation of pagerank
+ * for each node in the graph, obtaining personalized pagerank scores for each node.
+ * For I iterations (or until convergence) for each edge pagerank score is passed
+ * from a child node to an ancestor. For each node only the top L scores of 
+ * personalized pagerank (as if that node was the origin and only node of the
+ * teleport set) are kept, while the rest is pruned.
+ * The complexity is O(I *|Edges| * L).
  */
public class GuerrieriRank implements PersonalizedPageRankAlgorithm
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
    
    private final DirectedGraph<Integer, DefaultEdge> g;
    private Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> scores;
    private final GuerrieriParameters parameters;
    private final PartialSorter<Int2DoubleOpenHashMap.Entry> sorter = new PartialSorter<>();

    
    //Private class to store running parameters
    public static class GuerrieriParameters extends Parameters
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
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * Negative values are allowed to specify that tolerance must be ignored.
     */
    public GuerrieriRank(final DirectedGraph<Integer, DefaultEdge> g, final int smallTop, final int largeTop, final int iterations, final double dampingFactor, final double tolerance)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        scores.defaultReturnValue(null);
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
    public DirectedGraph<Integer, DefaultEdge> getGraph() 
    {
        return g;
    }
    
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
    @Override
    public double getRank(final int origin,final int target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return scores.get(origin).get(target);
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
        Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> nextScores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        for(Integer v: g.vertexSet())
        {
            Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap(this.parameters.largetTop);
            Int2DoubleOpenHashMap nextScoresMap = new Int2DoubleOpenHashMap(this.parameters.largetTop);
            //return values for when a key has no mapped value
            scoresMap.defaultReturnValue(0d);
            nextScoresMap.defaultReturnValue(0d);
            scoresMap.put(v.intValue(), 1d);
            scores.put(v.intValue(), scoresMap);
            nextScores.put(v.intValue(), nextScoresMap);
        }
        
        while(iterations > 0 && maxDiff >= this.parameters.getTolerance())
        {
            //reset the highest difference to 0 at the start of the run
            maxDiff = 0;
            for(int v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                double factor = this.parameters.getDamping() / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                Int2DoubleOpenHashMap currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.put(v, 1 - this.parameters.getDamping());
                
                //for each successor of v
                for(int successor: Graphs.successorListOf(g, v))
                {
                    /**
                     * for each value of personalized pagerank (max L values) saved 
                     * in the map  of a successor increment the personalized pagerank of v
                     * for that key of a fraction of it.
                     */
                    for(Int2DoubleMap.Entry entry: scores.get(successor).int2DoubleEntrySet())
                    {
                        //increment value (or set if key wasn't mapped)
                        currentMap.addTo(entry.getIntKey(), factor * entry.getDoubleValue());
                    }
                }
                
                //keep the top L values only
                if(currentMap.size() > this.parameters.largetTop)
                    keepTopL(currentMap, this.parameters.largetTop);
                
                //update maxDiff
                for(int key: currentMap.keySet())
                    maxDiff = Math.max(maxDiff, Math.abs(currentMap.get(key) - scores.get(v).get(key)));
            }
            // swap scores
            Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            iterations--;            
        }
        
        //keep smalltop only
        for(int v: scores.keySet())
        {
            if(scores.get(v).size() > this.parameters.smallTop)
                keepTopL(scores.get(v), this.parameters.smallTop);
            scores.get(v).trim();
        }
    }
    
    /**
     * Keeps the topL entries of the map, based on a partial order on the Lth element.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL(Int2DoubleOpenHashMap input, final int topL)
    {
        Int2DoubleMap.Entry[] values = input.int2DoubleEntrySet().toArray(new Int2DoubleMap.Entry[0]);
        
        sorter.partialSort(values, topL, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
        {
            return e1.getDoubleValue() < e2.getDoubleValue()? 1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()?
                    (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;  
        });
        //if too many to remove just clear and add the first topL
        //else just remove the non topL
        if(values.length > topL * 2)
        {
            //res is needed as a temporary holder since doing input.clear() will remove keys from values
            Int2DoubleOpenHashMap res = new Int2DoubleOpenHashMap(topL);
            for(int i = 0; i < topL; i++)
                res.put(values[i].getIntKey(), values[i].getDoubleValue());
            input.clear();
            input.putAll(res);
        }
        else
        {
            //needs a temporary holder since changes in the map are reflected in the Map.Entry[]
            int[] toRemove = new int[values.length - topL];
            for(int i = topL, index = 0; i < values.length; i++, index++)
                toRemove[index]= values[i].getIntKey();
            for(int i = 0; i < toRemove.length; i++)
                input.remove(toRemove[i]);
        }
    }
}
