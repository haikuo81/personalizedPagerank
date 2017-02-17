package personalizedpagerank;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

public class UntemplatedGuerrieriRank
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
    private final UntemplatedGuerrieriParameters parameters;

    
    
    //Private class to store running parameters
    public class UntemplatedGuerrieriParameters extends Parameters
    {
        private final int smallTop;
        private final int largetTop;
        
        private UntemplatedGuerrieriParameters(final int vertices, final int edges, final int smallTop, 
                final int largeTop, final int iterations, final double damping, final double tolerance)
        {
            super(vertices, edges, iterations, damping, tolerance);
            this.smallTop = smallTop;
            this.largetTop = largeTop;
        }

        private UntemplatedGuerrieriParameters(UntemplatedGuerrieriParameters input)
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
    public UntemplatedGuerrieriRank(final DirectedGraph<Integer, DefaultEdge> g, final int smallTop, final int largeTop, final int iterations, final double dampingFactor, final double tolerance)
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
        
        parameters = new UntemplatedGuerrieriParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, largeTop, iterations, dampingFactor, tolerance);
        
        run();
    }
    
    
    //GETTERS
    ////////////////////
    
    /**
     * @inheritDoc
     */
    public Parameters getParameters() 
    {
        return new UntemplatedGuerrieriParameters(this.parameters);
    }
    
    /**
     * @inheritDoc
     */
    public Int2DoubleOpenHashMap getMap(final int origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return scores.get(origin);
    }
    
    /**
     * @inheritDoc
     */
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
            Int2DoubleOpenHashMap tmp = new Int2DoubleOpenHashMap();
            tmp.put(v.intValue(), 1d);
            scores.put(v, tmp);
            nextScores.put(v, new Int2DoubleOpenHashMap());
        }
        
        double factor, contribution, stored;
        while(iterations > 0 && maxDiff >= this.parameters.getTolerance())
        {
            for(int v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                factor = this.parameters.getDamping() / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                Int2DoubleOpenHashMap currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.defaultReturnValue(-1);
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
                        stored = (currentMap.get(key) != -1)? (currentMap.get(key) + contribution) : contribution;
                        //stored = (stored = currentMap.get(key)) != null? (contribution + stored) : contribution;
                        currentMap.put(key, stored);
                        //using contribution to store scores.get(v).get(key) (the old value) to call it only once
                        //contribution = (contribution = scores.get(v).get(key)) != null? contribution : 0;
                        contribution = (scores.get(v).get(key)) != -1? scores.get(v).get(key) : 0;
                        //update maxDiff
                        maxDiff = Math.max(maxDiff, Math.abs(contribution - stored));
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
    private void keepTopL3(Int2DoubleOpenHashMap input, final int topL)
    {
        Int2DoubleMap.Entry[] values = input.entrySet().toArray(new Int2DoubleOpenHashMap.Entry[0]);
        partialSort(values, topL);
        input.clear();
        for(int i = 0; i < topL; i++)
            input.put(values[i].getKey(), values[i].getValue());
    }
   
    public void partialSort(Int2DoubleMap.Entry[] input, int n) 
    {
        if (n >= input.length)
            throw new IllegalArgumentException("N must be lower than the length of the input");
        int from = 0, to = input.length - 1;

        while (from < to) 
        {
            int leftIndex = from, rightIndex = to;
            Int2DoubleMap.Entry mid = input[(leftIndex + rightIndex) / 2];
            
            while (leftIndex < rightIndex) 
            {
                /*
                if the value is greater than the pivot move it on the right 
                side by swapping it with the value at rightIndex, else move on
                */
                if (input[leftIndex].getValue() <= mid.getValue()) 
                { 
                    Int2DoubleMap.Entry tmp = input[rightIndex];
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
