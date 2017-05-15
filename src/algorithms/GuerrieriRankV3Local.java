package algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utility.Graphs;
import utility.NodeScores;

 /**
+ * Runs an instance of GuerrieriRank, which runs an approximation of pagerank
+ * for each node in the graph, obtaining personalized pagerank scores for each node.
+ * For I iterations (or until convergence) for each edge pagerank score is passed
+ * from a child node to it's parent. For each node only the top L scores of 
+ * personalized pagerank (as if that node was the origin and only node of the
+ * teleport set) are kept, while the rest is pruned.
* * The L for each node is decided based on a budget given by the smallTop
* * and largeTop parameters, each node will have at least a budget of "smallTop", 
* * and on average each node will have a  budget of "largeTop", the budget is 
* * distributed proportionally based on the number of out going edges a node has.
+ * The complexity is O(I *|Edges| * L).
  */
public class GuerrieriRankV3Local extends PersonalizedPageRankAlgorithm
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
    public static class GuerrieriParameters extends PersonalizedPageRankAlgorithm.Parameters
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
    
    //getters
    /**
     * @inheritDoc
     */
    @Override
    public GuerrieriRankV3Local.GuerrieriParameters getParameters()
    {
        return parameters;
    }
    
    //CONSTRUCTOR
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results, it will
     * also be used as a minimum for how much space to allocate for each node.
     * @param largeTop How many max entries on average to keep for each vertex during computation,
     * this value will be used as an average for how much space to allocate for each
     * node while calculating the budget for each node.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is lower than tolerance. 
     * Negative values are allowed to specify that tolerance must be ignored.
     */
    public GuerrieriRankV3Local(final DirectedGraph<Integer, DefaultEdge> g, final int smallTop, 
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
    
    //methods (no getters)
    ////////////////////
    
    /**
     * Executes the algorithm, this.scores will store the results.
     */
    private void run()
    {
        Int2ObjectOpenHashMap<NodeScores> nextScores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());

        //2 partition of vertixes, keeping intra edges as low as possible for each partition
        int[][] partitions = getPartitions();
        
        //successors for each node, to avoid calling Graphs.successorListOf which is slow
        Int2ObjectOpenHashMap<int[]> successors = Graphs.getSuccessors(g);
        
        /*
        for  each vertex init its map with a score for itself and its neighbours
        */
        for(int v: g.vertexSet())
        {
            NodeScores scoresMap = new NodeScores();
            scoresMap.put(v, 1 -  parameters.getDamping());
            double factor = parameters.getDamping() / g.outDegreeOf(v);
            for(int successor: successors.get(v))
                scoresMap.addTo(successor, factor);
            scoresMap.keepTop(parameters.largetTop);
            scores.put(v, scoresMap);
            
            nextScores.put(v, new NodeScores());
        }
        
        //nodes yet to converge
        Set<Integer> notConverged = new HashSet<>(g.vertexSet());
        
        int[] currentPartition = partitions[0];
        for(int i = 0; i < parameters.getIterations() && notConverged.size() > 0; i++)
        {
            //nodes to be removed because they have converged
            ArrayList<Integer> toRemove = new ArrayList<>();
            
            for(int v: currentPartition)
            {
                if(notConverged.contains(v))
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

                    //keep the top L values only, where L is the allocated budget for the node
                    currentMap.keepTop(parameters.largetTop);

                    //assign the map to the new scores
                    nextScores.put(v, currentMap);

                    //check if the node has converged
                    if(currentMap.norm1(scores.get(v)) < parameters.getTolerance())
                        toRemove.add(v);
                }
            }
            //remove converged nodes
            for(Integer removeNode: toRemove)
                notConverged.remove(removeNode);
            
            currentPartition = currentPartition == partitions[0]? partitions[1] : partitions[0];
            
            //this copy could be avoided but it's needed to produce way less garbage
            for(int node: currentPartition)
            {
                nextScores.get(node).clear();
                nextScores.get(node).add(scores.get(node));
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
    
    /**
     * Tries to divide the vertices in 2 partitions by two coloring it.
     * If the graph is not bipartite the two partitions will be approximated as
     * best as possible.
     * @return An array containing 2 jagged arrays of integers, representing two
     * different partition of vertices.
     */
    private int[][] getPartitions()
    {
        //will contain the 2 partitions during the execution of the method
        Set<Integer> p1 = new HashSet<>(), p2 = new HashSet<>();

        //queue for going breadth first
        List<Integer> queue = new ArrayList<>();
        //keep track of visited nodes
        Set<Integer> visited = new HashSet<>(g.vertexSet().size());
        
        for(int node: g.vertexSet())
        {
            if(!visited.contains(node))
            {
                visited.add(node);
                queue.add(node);
                p1.add(node);
            }
            
            while(!queue.isEmpty())
            {
                int next = queue.remove(0);
                for(int successor: org.jgrapht.Graphs.successorListOf(g, next))
                    if(!visited.contains(successor))
                    {
                        visited.add(successor);
                        queue.add(successor);
                        if(p1.contains(next))
                            p2.add(successor);
                        else
                            p1.add(successor);
                    }
                for(int successor: org.jgrapht.Graphs.predecessorListOf(g, next))
                    if(!visited.contains(successor))
                    {
                        visited.add(successor);
                        queue.add(successor);
                        if(p1.contains(next))
                            p2.add(successor);
                        else
                            p1.add(successor);
                    }
            }
        }
        
        //convert the 2 sets into arrays of integers
        int[][] res = new int[2][0];
        res[0] = new int[p1.size()];
        res[1] = new int[p2.size()];
        int index = 0;
        for(int node: p1)
        {
            res[0][index] = node;
            index++;
        }
        index = 0;
        for(int node: p2)
        {
            res[1][index] = node;
            index++;
        }
        return res;
    }
}
