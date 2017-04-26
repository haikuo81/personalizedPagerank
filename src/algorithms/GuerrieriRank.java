package algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utility.Graphs;
import utility.NodeScores;

 /**
+ * Runs an instance of GuerrieriRank, which runs an approximation of pagerank
+ * for each node in the graph, obtaining personalized pagerank scores for each node.
+ * For I iterations (or until convergence) for each edge pagerank score is passed
+ * from a child node to an ancestor. For each node only the top L scores of 
+ * personalized pagerank (as if that node was the origin and only node of the
+ * teleport set) are kept, while the rest is pruned.
+ * The complexity is O(I *|Edges| * L).
  */
public class GuerrieriRank extends PersonalizedPageRankAlgorithm
{
    //Default number of scores to return for each node, after doing calculations with
    //the LARGE_TOP only the small top will be kept as a valid result.
    public static final int DEFAULT_SMALL_TOP = 10;
    
    //Default number of max scores to keep for each during computation node,
    //if scores[V].size() > max scores the lowest 
    //ones gets removed, keeping only the first max scores.
    public static final int DEFAULT_LARGE_TOP = 30;

    //Default number of maximum iterations to be used 
    public static final int DEFAULT_ITERATIONS = 100;
    
    //Default damping factor
    public static final double DEFAULT_DAMPING_FACTOR = 0.85d;
    
    //Default tolerance, if the highest difference of scores between 2 iterations is lower
    //than this the algorithm will stop.
    public static final double DEFAULT_TOLERANCE = 0.0001;
    
    private final GuerrieriParameters parameters;

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

        public int getLargeTop() {
            return largetTop;
        }
    }
    
    //CONSTRUCTOR
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g the input graph
     * @param smallTop How many max entries for each vertex to keep in the final results.
     * @param largeTop How many max entries to keep for each vertex during computation.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * Negative values are allowed to specify that tolerance must be ignored.
     */
    public GuerrieriRank(final DirectedGraph<Integer, DefaultEdge> g, final int smallTop, 
            final int largeTop, final int iterations, final double dampingFactor, final double tolerance)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        
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
    
    //getters
    /**
     * @inheritDoc
     */
    @Override
    public GuerrieriRank.GuerrieriParameters getParameters()
    {
        return parameters;
    }
    
    //methods (no getters)
    ////////////////////
    
    /**
     * Executes the algorithm, this.scores will store the results.
     */
    private void run()
    {
        double maxDiff = parameters.getTolerance();
        
        //init scores
        Int2ObjectOpenHashMap<NodeScores> nextScores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        for(int v: g.vertexSet())
        {
            NodeScores scoresMap = new NodeScores(parameters.largetTop);
            scoresMap.put(v, 1d);
            scores.put(v, scoresMap);

            nextScores.put(v, new NodeScores());
        }
        
        //successors for each node, to avoid calling Graphs.successorListOf which is slow
        Int2ObjectOpenHashMap<int[]> successors = Graphs.getSuccessors(g);
        
        for(int i = 0; i < parameters.getIterations() && maxDiff >= parameters.getTolerance(); i++)
        {
            //reset the highest difference to 0 at the start of the run
            maxDiff = 0;
            for(int v: g.vertexSet())
            {
                //to avoid calculating it for each successor
                double factor = parameters.getDamping() / g.outDegreeOf(v);
                                
                //every node starts with a rank of (1 - dampingFactor) in it's own map
                NodeScores currentMap = nextScores.get(v);
                currentMap.clear();
                currentMap.put(v, 1 - parameters.getDamping());
                
                //for each successor of v
                for(int successor: successors.get(v))
                {
                    /**
                     * for each value of personalized pagerank (max L values) saved 
                     * in the map  of a successor increment the personalized pagerank of v
                     * for that key of a fraction of it.
                     */
                    currentMap.add(scores.get(successor), factor);
                }
                //keep the top L values only
                currentMap.keepTop(parameters.largetTop);
                
                //check if the norm1 of the difference is greater than the maxDiff
                maxDiff = Math.max(currentMap.norm1(scores.get(v)), maxDiff);
            }
            
            // swap scores
            Int2ObjectOpenHashMap<NodeScores> tmp = scores;
            scores = nextScores;
            nextScores = tmp;
        }
        /*
        //trim to avoid wasting space
        for(int v: scores.keySet())
        {
            scores.get(v).keepTop(parameters.smallTop);
            scores.get(v).trim();
        }
*/
    }
    
}
