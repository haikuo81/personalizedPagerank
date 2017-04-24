package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.Graphs;
import personalizedpagerank.Utility.NodeScores;
import personalizedpagerank.Utility.Parameters;


/**
 * Computes for each node the top K scorers of personalized pagerank using
 * Monte Carlo techniques and some form of memoization to copy results between
 * neighbours, to ensure each node move to it's successors it an uniformly
 * fashion each node is mapped to an index telling where to go next which is
 * incremented every time it's used.
 */
public class MCCompletePathPageRankV2 implements PersonalizedPageRankAlgorithm
{
    /*
    Default damping factor for pagerank iterations.
    */
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    
    private final DirectedGraph<Integer, DefaultEdge> g;
    private Int2ObjectOpenHashMap<NodeScores> scores;
    private final MCCompletePathParameters parameters;
    
    //Private class to store running parameters
    public static class MCCompletePathParameters extends Parameters
    {
        private final int smallTop;
        
        private MCCompletePathParameters(final int vertices, final int edges, final int smallTop, 
                final int iterations, final double damping)
        {
            super(vertices, edges, iterations, damping, 0d);
            this.smallTop = smallTop;
        }

        private MCCompletePathParameters(MCCompletePathParameters input)
        {
            super(input.getVertices(), input.getEdges(), input.getIterations(), 
                    input.getDamping(), input.getTolerance());
            this.smallTop = input.smallTop;
        }
                
        public int getSmallTop() {
            return smallTop;
        }
    }
    
    //CONSTRUCTOR
    ////////////////////
    
    /**
     * Create object and run the algorithm, results of the personalized pagerank
     * are stored in the object.
     * @param g the input graph
     * @param smallTop How many max entries to keep in the final results.
     * @param iterations Number of runs to do for each node.
     * @param dampingFactor Damping factor (chance of following an edge instead
     * of teleporting)
     */
    public MCCompletePathPageRankV2(final DirectedGraph<Integer, DefaultEdge> g, 
            final int smallTop, final int iterations, final double dampingFactor)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        
        if(smallTop <= 0)
            throw new IllegalArgumentException("Top k entries to keep must be positive");
        
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor >= 1)
            throw new IllegalArgumentException("Damping factor must be [0,1)");
        
        parameters = new MCCompletePathParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, iterations, dampingFactor);
        
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
        return new MCCompletePathParameters(this.parameters);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public NodeScores getMap(final int origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return scores.get(origin);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Int2ObjectOpenHashMap<NodeScores> getMaps()
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
    
    private void run()
    {
        Random random = new Random();
        
        //successors for each node, to avoid calling Graphs.successorListOf which is slow
        Int2ObjectOpenHashMap<int[]> successors = Graphs.getSuccessors(g);
        
        //each node has an index telling the next successor to pick when walking
        //away from the node
        Int2IntOpenHashMap indexes = new Int2IntOpenHashMap(g.vertexSet().size());
        
        //an ordering of the vertices to run more efficiently
        int[] order = executionOrder();
        
        //will contain the results from the walks of nodes that have to walk
        Int2ObjectOpenHashMap<NodeScores> walksMap = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        
        for(int node: order)
        {
            NodeScores map = new NodeScores();
            if(g.outDegreeOf(node) > 0)
            {
                double factor = this.parameters.getDamping() / g.outDegreeOf(node);

                for(int successor: successors.get(node))
                {
                    if(scores.get(successor) != null)
                        map.add(scores.get(successor));
                    else
                    {
                        NodeScores tmp = walksMap.get(successor);
                        if(tmp == null)
                        {
                            tmp = this.doWalksForNode(successors, indexes, random, successor);
                            walksMap.put(successor, tmp);
                        }
                        map.add(tmp);
                    }
                }

                map.keepTop(this.parameters.smallTop);

                //multiply each value in the map for the factor
                map.multiplyAll(factor);
            }
            //each walk begins at node, so the average for the node will at least be 1
            map.addTo(node, 1d);
            scores.put(node, map);
            walksMap.remove(node);
        }
        //trim to avoid wasting space
        for(int v: scores.keySet())
            scores.get(v).trim();
    }
  
    /**
     * Find an order of execution as good as possible. Vertices with more
     * in going edges will be computed first.
     * Every time a node is considered done all it's predecessors will have 
     * one less successor to wait for, this means that if all the successors
     * of a node are done the node will be considered computed as well, and it will
     * recursively check if any predecessor can now be considered computed.
     * @param maxWalks Will map the max number of walks needed for every node.
     * @return An array of integers (node ids) representing the order of execution
     * to take.
     */
    private int[] executionOrder()
    {
        //first part: sort nodes by indegree descending and outdegree ascending
        int[] tmpOrder = sortNodes();
        
        /*
        second part: find the order of the nodes after taking into consideration
        that if a node is done then all it's predecessors have 1 less successor
        to wait for
        */
        int index = 0;
        int[] newOrder = new int[g.vertexSet().size()];
        
        //remaining successors to wait for
        Int2IntOpenHashMap outDeg = new Int2IntOpenHashMap(g.vertexSet().size());
        for(int node: g.vertexSet())
            outDeg.put(node, g.outDegreeOf(node));
        
        //using this map as a set to know if a node has been visited already
        Int2IntOpenHashMap visited = new Int2IntOpenHashMap(g.vertexSet().size());
        
        List<Integer> queue = new ArrayList<>();
        
        for(int node: tmpOrder)
        {
            if(!visited.containsKey(node))
            {
                queue.add(node);
                
                while(!queue.isEmpty())
                {
                    int next = queue.remove(0);
                    newOrder[index] = next;
                    index++; 
                    visited.put(next, next);
                    
                    /*
                    for each predecessor decrement the remaining successors to wait
                    for and eventually consider it done when the remaining successors
                    get to 0
                    */
                    for(int pred: org.jgrapht.Graphs.predecessorListOf(g, next))
                    {
                        int remaining = outDeg.get(pred);
                        if(remaining > 0)
                        {
                            remaining--;
                            outDeg.put(pred, remaining);
                            //if the node can be computed
                            if(remaining == 0 && !visited.containsKey(pred))
                                queue.add(pred);
                        }
                    }
                }
            }
        }
        return newOrder;
    }
    
    /**
     *  Sort nodes by indegree descending and outdegree ascending.
     * @return Array of node ids as integers.
     */
    private int[] sortNodes()
    {
        //first part: sort nodes by indegree descending and outdegree ascending
        int index = 0;
        int[] res = new int[g.vertexSet().size()];
        Pair<Integer, Integer>[] nodeOutDeg = new Pair[g.vertexSet().size()];
        for(int node: g.vertexSet())
        {
            nodeOutDeg[index] = new Pair<>(node, g.inDegreeOf(node));
            index++;
        }
        
        Arrays.sort(nodeOutDeg, (Pair<Integer, Integer> e1, Pair<Integer, Integer> e2)
        -> {
            
            return -(e1.getSecond().compareTo(e2.getSecond())) == 0? 
                    (new Integer(g.outDegreeOf(e1.getFirst()))).compareTo(g.outDegreeOf(e2.getFirst()))
                    : -(e1.getSecond().compareTo(e2.getSecond()));
            });
        
        index = 0;
        for(Pair<Integer, Integer> pair: nodeOutDeg)
        {
            res[index] = pair.getFirst();
            index++;
        }
        return res;
    }
    
    /**
     * Do a number of random walks for a node and return a map containing 
     * the average number of visits to the encountered nodes.
     * @param successors Map containing successors for each node, used for caching reasons.
     * @param indexes Map containing for each node an index telling the next successor
     * to pick when walking away from the node.
     * @param random Source of randomness for deciding when to teleport.
     * @param node Starting node.
     * @param walks Number of walks to do.
     * @return Map having the average number of visits to the encountered nodes.
     */
    private NodeScores doWalksForNode(Int2ObjectOpenHashMap<int[]> successors, Int2IntOpenHashMap indexes,
           Random random, int node)
    {
        NodeScores map = new NodeScores(this.parameters.smallTop);
        if(successors.get(node).length > 0)
        {
            double teleported;//tells if a teleport happens
            int currentNode;//keeps the current node
            int[] next;//successors of the current node
            int index;//index telling which successor to take when going from 
            //the currentNode to a successor
            
            /*
            a part of the walks is wasted because a teleport happens before traversing
            the first edge, so we account for those walks here (lowering the total walks)
            but make it so that the first edge is always traversed
            */
            int walks = (int) (this.parameters.getIterations() * this.parameters.getDamping());
            
            //each walk will surely start from the origin node
            map.addTo(node, this.parameters.getIterations());
            
            for(int i = 0 ; i < walks; i++)
            {
                currentNode = node;
                
                /*
                random walk which stops if a teleport happens (teleported > damping)
                or if it gets into a node without out going edges
                */
                do
                {
                    //get successors of the current node
                    next = successors.get(currentNode);

                    //if the current node has no outgoing edges the walk ends here
                    if(next.length == 0)
                        teleported = 1d;
                    else
                    {
                        //pick next node and increment index
                        index = indexes.get(currentNode);
                        indexes.put(currentNode, (index + 1)%next.length);
                        currentNode = next[index];
                        
                        //increment node only if it won't make the map size greater than what's allowed
                        if(map.size() < this.parameters.smallTop || map.containsKey(currentNode))
                            map.addTo(currentNode, 1d);
                        //decide if the walk ends here or not
                        teleported = random.nextDouble();
                    }
                }while(teleported <= this.parameters.getDamping());
            }
            
            //divide by the number of walks done to obtain the mean
            for(Int2DoubleMap.Entry results: map.int2DoubleEntrySet())
                results.setValue(results.getDoubleValue()/this.parameters.getIterations());
            
        }
        else
            map.addTo(node, 1d);
        return map;
    }
}
