package personalizedpagerank.Algorithms;

import personalizedpagerank.Utility.Parameters;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
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
    private final PartialSorter<Int2DoubleOpenHashMap.Entry> sorter = new PartialSorter();

    
    
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
        this.scores = new Int2ObjectOpenHashMap(g.vertexSet().size());
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
        return (scores.get(origin).get(target) != -1)? scores.get(origin).get(target) : 0d;
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
        Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> nextScores = new Int2ObjectOpenHashMap(g.vertexSet().size());
        for(Integer v: g.vertexSet())
        {
            Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap();
            Int2DoubleOpenHashMap nextScoresMap = new Int2DoubleOpenHashMap();
            //return values for when a key has no mapped value
            scoresMap.defaultReturnValue(-1);
            nextScoresMap.defaultReturnValue(-1);
            scoresMap.put(v.intValue(), 1d);
            scores.put(v, scoresMap);
            nextScores.put(v, nextScoresMap);
        }
        
        double factor, contribution, summation;
        while(iterations > 0 && maxDiff >= this.parameters.getTolerance())
        {
            //reset the highest difference to 0 at the start of the run
            maxDiff = 0;
            for(int v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                factor = this.parameters.getDamping() / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                Int2DoubleOpenHashMap currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.put(v, 1 - this.parameters.getDamping());
                
                //for each successor of v
                for(DefaultEdge e: g.outgoingEdgesOf(v))
                {
                    Integer successor = Graphs.getOppositeVertex(g, e, v);
                    Int2DoubleOpenHashMap successorMap = scores.get(successor);
                    
                    /**
                     * for each value of personalized pagerank (max L values) saved 
                     * in the map  of a successor increment the personalized pagerank of v
                     * of a fraction of it.
                     */
                    for(int key: successorMap.keySet())
                    {
                        contribution = factor * successorMap.get(key);
                        
                        //if there was a previously stored value put the sum of that value and the contribution
                        //stored will be used to store the new value
                        /**
                         * if there was a previously stored value for a key from 
                         * another successor of v put the sum of that value and the 
                         * contribution as the total score of the key.
                         * stored i
                         */
                        summation = (summation = currentMap.get(key)) != -1? (summation + contribution) : contribution;
                        currentMap.put(key, summation);
                        
                        //using contribution to store scores.get(v).get(key) (the old value) to call it only once
                        contribution = (contribution = scores.get(v).get(key)) != -1? 0 : contribution;
                        
                        //update maxDiff
                        maxDiff = Math.max(maxDiff, Math.abs(contribution - summation));
                    }
                }
                //keep the top L values only
                if(currentMap.size() > this.parameters.largetTop)
                    keepTopL3(currentMap, this.parameters.largetTop);
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
                keepTopL3(scores.get(v), this.parameters.smallTop);
        }
    }
    
    
    /**
     * Keeps the topL entries of the map, after finding the value of the Lth 
     * element if they were ordered by values (descending). First pass is inclusive
     * of entries with values = lth, if the size of the result exceeds L 
     * then a second pass is done to remove entries with values = lth.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL2(Int2DoubleOpenHashMap input, final int topL)
    {
        Int2DoubleMap.Entry[] values = (Int2DoubleOpenHashMap.Entry[]) input.entrySet().toArray();
        sorter.partialSort(values, topL, new EntryComparator());
        Double lth = values[topL].getValue();
        input.entrySet().removeIf(e-> e.getValue() < lth );
        if(input.size() > topL)
            input.entrySet().removeIf(e-> e.getValue().equals(lth) && input.size() > topL );
    }
    
    
    private class EntryComparator implements Comparator
    {

        @Override
        public int compare(Object o1, Object o2) 
        {
            Int2DoubleMap.Entry e1 = (Int2DoubleMap.Entry) o1;
            Int2DoubleMap.Entry e2 = (Int2DoubleMap.Entry) o2;
            if(e1.getDoubleValue() < e2.getDoubleValue())
                return -1;
            else if(e1.getDoubleValue() == e2.getDoubleValue())
                return 0;
            else
                return 1;
        }
    }
    
    /**
     * Keeps the topL entries of the map, based on a partial order on the Lth element.
     * @param input Input map.
     * @param topL How many elements to keep from the top.
     */
    private void keepTopL3(Int2DoubleOpenHashMap input, final int topL)
    {
        Int2DoubleMap.Entry[] values = input.entrySet().toArray(new Int2DoubleMap.Entry[0]);
        sorter.partialSort(values, topL, new EntryComparator());
        input.clear();
        for(int i = 0; i < topL; i++)
            input.put(values[i].getKey(), values[i].getValue());
    }
   
}
