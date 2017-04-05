/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.Parameters;
import personalizedpagerank.Utility.PartialSorter;


/**
 * Runs an instance of MCCompletePath from 
 * "Quick Detection of Top-k Personalized PageRank Lists"
 * Given a number of runs (the iterations parameter), for each node do that number
 * of runs as random walks starting from that node, at each step calculate if
 * the walk ends (teleport) or the walk goes on.
 * For each node Y traversed during a random walk starting from the node X increment
 * scores(X, Y).
 * After the random walks are done an approximation of the top K personalized
 * pagerank scorers for node X can be obtained by keeping the top K values
 * from scores(X).
 */
public class MCCompletePathPageRank implements PersonalizedPageRankAlgorithm
{
    /*
    Default damping factor for pagerank iterations.
    */
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    
    private final DirectedGraph<Integer, DefaultEdge> g;
    private Int2ObjectOpenHashMap<Int2DoubleOpenHashMap> scores;
    private final MCCompletePathParameters parameters;
    private final PartialSorter<Int2DoubleOpenHashMap.Entry> sorter = new PartialSorter<>();
    
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
    public MCCompletePathPageRank(final DirectedGraph<Integer, DefaultEdge> g, 
            final int smallTop, final int iterations, final double dampingFactor)
    {
        this.g = g;
        this.scores = new Int2ObjectOpenHashMap<>(g.vertexSet().size());
        scores.defaultReturnValue(null);
        
        if(smallTop <= 0)
            throw new IllegalArgumentException("Top k entries to keep must be positive");
        
        if(iterations <= 0) 
            throw new IllegalArgumentException("Maximum iterations must be positive");
        
        if(dampingFactor < 0 || dampingFactor >= 1)
            throw new IllegalArgumentException("Damping factor must be [0,1)");
        
        parameters = new MCCompletePathParameters(g.vertexSet().size(), g.edgeSet().size(), 
                smallTop, iterations, dampingFactor);
        
        //saving successors list to avoid calling Graphs.successorListOf(g, scores)
        //more than necessary
        Int2ObjectOpenHashMap<int[]> successors = new Int2ObjectOpenHashMap<>();
        successors.defaultReturnValue(null);
        for(int node: g.vertexSet())
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
        return new MCCompletePathParameters(this.parameters);
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
    
    private Int2DoubleOpenHashMap run(final int node, Int2ObjectOpenHashMap<int[]> successors)
    {
        Random random = new Random();
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        map.defaultReturnValue(0d);
        
        //each walk begins at node, so scores(node, node) will at least have a
        //value equal to the number of runs
        map.put(node, this.parameters.getIterations());
        
        //do a number of random walks equal to iterations
        for(int i = 0 ; i < this.parameters.getIterations(); i++)
        {
            int currentNode = node;
            
            //decide if the walk actually starts or if a teleport happens
            double teleported = random.nextDouble();
            
            /*
            random walk which stops if a teleport happens (teleported > damping)
            or if it gets into a node without out going edges
            */
            while(teleported <= this.parameters.getDamping())
            {
                //get successors of the current node
                int[] next = successors.get(currentNode);
                if(next == null)
                {
                    next = computeSuccessors(currentNode);
                    successors.put(currentNode, next);
                }
                
                //if the current node has no edges the walk ends here
                if(next.length == 0)
                    teleported = 1d;
                else
                {
                    //get to a random successor 
                    currentNode = next[random.nextInt(next.length)];
                    map.addTo(currentNode, 1d);
                    //decide if the walk ends here or not
                    teleported = random.nextDouble();
                }
            }
        }
        
        if(map.size() > parameters.smallTop)
            keepTopL(map, parameters.smallTop);
        return map;
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
