
package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.Parameters;

public class BoundaryRestrictedPageRank implements PersonalizedPageRankAlgorithm
{
    /*
    Default number of max iterations, the algorithm will be stopped
    if after this many iterations the stopping tolerance has not been met.
    */
    public static final int DEFAULT_MAX_ITERATIONS = 100;
    
    /*
    Default damping factor for pagerank iterations.
    */
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;

        
    /*
    Default tolerance for stopping the algorithm if the difference between two
    iterations is smaller than this.
    */
    public static final double DEFAULT_TOLERANCE = 0.001;
    
    /*
    Default frontier tolerance, a frontier which total pagerank is greater than
    this value will be "unpacked", adding from the frontier to the active
    set L the highest value (pagerank) node until the frontier is below
    this value.
    */
    public static final double DEFAULT_FRONTIER_THRESHOLD = 0.001;
    
    
    private final DirectedGraph<Integer, DefaultEdge> g;
    private Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> scores;
    private final BoundaryRestrictedParameters parameters;
    
    //Private class to store running parameters
    public static class BoundaryRestrictedParameters extends Parameters
    {
        private final double frontierThreshold;
        
        private BoundaryRestrictedParameters(final int vertices, final int edges, 
                final double frontierThreshold, final int iterations,
                final double damping, final double tolerance)
        {
            super(vertices, edges, iterations, damping, tolerance);
            this.frontierThreshold = frontierThreshold;
        }

        private BoundaryRestrictedParameters(BoundaryRestrictedParameters input)
        {
            super(input.getVertices(), input.getEdges(), input.getIterations(), 
                    input.getDamping(), input.getTolerance());
             this.frontierThreshold = input.getFrontierThreshold();
        }

        public double getFrontierThreshold() {
            return frontierThreshold;
        }
    }
    
    //CONSTRUCTOR
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     *
     * @param g the input graph
     * computation.
     * @param iterations the number of iterations to perform
     * @param dampingFactor the damping factor
     * @param tolerance Stop if the difference of scores between iterations is
     * lower than tolerance. Negative values are allowed to specify that
     * tolerance must be ignored.
     * @param frontierThreshold A frontier which total pagerank is greater than
     * this value will be "unpacked", adding from the frontier to the active
     * set L the highest value (pagerank) node until the frontier is below this
     * value.
     */
    public BoundaryRestrictedPageRank(final DirectedGraph<Integer, DefaultEdge> g, 
            final int iterations, final double dampingFactor, final double tolerance,
            final double frontierThreshold)
    {
        this.g = g;
        scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
        
        
        parameters = new BoundaryRestrictedParameters(g.vertexSet().size(),
                g.edgeSet().size(), frontierThreshold, iterations, dampingFactor, 
        tolerance);
        //saving successors list to avoid calling Graphs.successorListOf(g, scores)
        //more than necessary
        Int2ObjectOpenHashMap<int[]> successors = new Int2ObjectOpenHashMap<>();
        successors.defaultReturnValue(null);
        for(int node: g.vertexSet())
            scores.put(node, run(node, successors));
    }
    
    public BoundaryRestrictedPageRank(final DirectedGraph<Integer, DefaultEdge> g, 
            final int iterations, final double dampingFactor, final double tolerance,
            final double frontierThreshold, Set<Integer> nodes)
    {
        this.g = g;
        scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor > 1)
            throw new IllegalArgumentException("Damping factor must be [0,1]");
        
        
        parameters = new BoundaryRestrictedParameters(g.vertexSet().size(),
                g.edgeSet().size(), frontierThreshold, iterations, dampingFactor, 
        tolerance);
        //saving successors list to avoid calling Graphs.successorListOf(g, scores)
        //more than necessary
        Int2ObjectOpenHashMap<int[]> successors = new Int2ObjectOpenHashMap<>();
        successors.defaultReturnValue(null);
        for(int node: nodes)
            scores.put(node, run(node, successors));
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
        return new BoundaryRestrictedPageRank.BoundaryRestrictedParameters(this.parameters);
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
    
    private Int2DoubleOpenHashMap run(int node, Int2ObjectOpenHashMap<int[]> successors)
    {
        /*
        init phase, initialize stuff and set the starting node score
        to 1
        */
        //set of active nodes, each active node is mapped to the product of
        //dampingfactor / outgoing edges
        Int2DoubleOpenHashMap active = new Int2DoubleOpenHashMap();
        Int2DoubleOpenHashMap scores = new Int2DoubleOpenHashMap();
        Int2DoubleOpenHashMap nextScores = new Int2DoubleOpenHashMap();
        
        //frontier, the nodes that are reached but not part of the active set
        Int2DoubleOpenHashMap frontier = new Int2DoubleOpenHashMap();
        scores.defaultReturnValue(0d);
        nextScores.defaultReturnValue(0d);
        frontier.defaultReturnValue(0d);
        //total value of the frontier
        double totalFrontier;
        
        active.put(node, parameters.getDamping() / g.outDegreeOf(node));
        scores.put(node, 1d);
        successors.put(node, computeSuccessors(node));
        
        double diff = Double.MAX_VALUE;
        int iterations = parameters.getIterations();
        while(diff > parameters.getTolerance() && iterations > 0)
        {
            //do a pagerank iteration in which the frontier and its total value
            //are calculated
            totalFrontier = pageRankIteration(active, frontier, scores, nextScores, successors);
            
            //add score back to "node" so that the sum of the scores is 1
            nextScores.addTo(node, missingScore(nextScores));
            
            //unpack the frontier
            unpackFrontier(totalFrontier, active, frontier, successors);
            
            //check difference
            diff = difference(scores, nextScores);
            
            //swap scores
            Int2DoubleOpenHashMap tmp = scores;
            scores = nextScores;
            nextScores = tmp;
            
            iterations --;
        }
        return scores;
    }
    
    /**
     * Return the total difference in mapped values of the two maps.
     * For every key mapped to either m1 or m2 add to the result the absolute
     * difference between the value mapped to it in m1 and in m2. If a key is not
     * mapped in one of the maps it's value is the defaultReturnValue of the map.
     * @param m1 First map.
     * @param m2 Second map.
     * @return Total difference of mapped values between the two maps.
     */
    private double difference(Int2DoubleOpenHashMap m1, Int2DoubleOpenHashMap m2)
    {
        double res = 0;
        for(Int2DoubleMap.Entry entry: m1.int2DoubleEntrySet())
            res += Math.abs(entry.getDoubleValue() - m2.get(entry.getIntKey()));
        //some keys might be in m1 but not in m2 or viceversa
        for(Int2DoubleMap.Entry entry: m2.int2DoubleEntrySet())
            if(m1.get(entry.getIntKey()) == 0)
                res += Math.abs(entry.getDoubleValue() - m1.get(entry.getIntKey()));
        return res;
    }
    
    /**
     * Given a map of integers to doubles sums all the values and returns the absolute
     * difference between 1 and the sum.
     * @param map Map for which to sum the values.
     * @return Absolute difference between 1 and the sum of the values of the map.
     */
    private double missingScore(Int2DoubleOpenHashMap map)
    {
        double missing = 0;
        for (Int2DoubleMap.Entry entry : map.int2DoubleEntrySet())
                missing += entry.getDoubleValue();
            missing = Math.abs(1d - missing);
        return missing;
    }
    
    /**
     * Performs one pagerank iteration using the active set of nodes, for each
     * active node "n" increment the value of every children by 
     * pagerank(n) * damping/outdegree(n).
     * Keeps track of the frontier nodes during the iteration (see frontier param),
     * and returns the total pagerank value of the frontier.
     * Both nextScores and frontier are cleared at the start of the method.
     * @param active Set of nodes which can transfer pagerank, the double
     * value mapped to every node must be dampingFactor/outdegree.
     * @param frontier Set of nodes that receive pagerank but aren't part of
     * the active set, after the method has ended this map will map nodes
     * of the frontier to their pagerank value.
     * @param scores Current pagerank scores.
     * @param nextScores Will contain the new pagerank scores after the method
     * has ended.
     * @param successors Map of successors for each node.
     * @return The sum of the pagerank value of the nodes that are part of the
     * frontier.
     */
    private double pageRankIteration(Int2DoubleOpenHashMap active,
            Int2DoubleOpenHashMap frontier, Int2DoubleOpenHashMap scores,
            Int2DoubleOpenHashMap nextScores, Int2ObjectOpenHashMap<int[]> successors)
    {
        nextScores.clear();
        frontier.clear();
        
        double totalFrontier = 0;
        for(Int2DoubleMap.Entry entry: active.int2DoubleEntrySet())
            {
                double value = scores.get(entry.getIntKey()) * active.get(entry.getIntKey());
                for(int successor: successors.get(entry.getIntKey()))
                {
                    nextScores.addTo(successor, value);
                    //if its not part of the active set its part of the frontier
                    if(active.get(successor) == 0)
                    {
                        frontier.addTo(successor, value);
                        totalFrontier += value;
                    }
                }
            }
        return totalFrontier;
    }
    
    /**
     * If the total value of the frontier is above a threshold value keep adding
     * the highest value frontier node to the set of active nodes (and remove it from
     * the frontier) until the frontier value is below the threshold.
     * @param totalFrontier Total value of the frontier.
     * @param active Set of active nodes, if the value of the frontier is above
     * parameters.getFrontierThreshold() the top nodes from the frontier will be
     * added to this map.
     * @param Map of successors for each node, this method will a node (key)
     * and its successors (value) when the node is newly added to the active set.
     * @param frontier Map mapping every frontier node to its current pagerank value. 
     */
    private void unpackFrontier(double totalFrontier, Int2DoubleOpenHashMap active, 
            Int2DoubleOpenHashMap frontier, Int2ObjectOpenHashMap<int[]> successors)
    {
       if(totalFrontier > parameters.getFrontierThreshold())
            {
                //sort frontier entries by value descending
                Int2DoubleMap.Entry[] entries = frontier.entrySet().toArray(new Int2DoubleMap.Entry[0]);
                Arrays.sort(entries, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                        -> {
                    return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                            : e1.getDoubleValue() == e2.getDoubleValue()
                            ? (e1.getIntKey() < e2.getIntKey() ? -1 : 1) : -1;
                });
                
                //keep adding the highest value entry to the active set
                //until the frontier value is below the frontierThreshold
                int index = 0;
                do
                {
                    //map the node to damping factor / outdegree
                    active.put(entries[index].getIntKey(),
                            parameters.getDamping() / g.outDegreeOf(entries[index].getIntKey()));
                    totalFrontier -= entries[index].getDoubleValue();
                    //if successors haven't already been computed do it
                    if(!successors.containsKey(entries[index].getIntKey()))
                        successors.put(entries[index].getIntKey(), computeSuccessors(entries[index].getIntKey()));
                    index++;
                }
                //&& index < frontier.size() needed because of rounding errors that might
                //set totalFrontier > parameters.getFrontierThreshold() 
                //(might happen only if the frontierThreshold is low enough)
                while(totalFrontier > parameters.getFrontierThreshold() && index < frontier.size());
            } 
    }
    
    /**
     * Given a node returns its successors as an array of ints.
     * @param node Node for which to find the successors.
     * @return Array of successor nodes.
     */
    private int[] computeSuccessors(int node)
    {
        List<Integer> s = Graphs.successorListOf(g, node);
        int[] res = new int[s.size()];
        int index = 0;
        for(int successor: s)
        {
            res[index] = successor;
            index++;
        }
        return res;
    }
}
