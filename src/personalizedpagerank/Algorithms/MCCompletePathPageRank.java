package personalizedpagerank.Algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Random;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Utility.Graphs;
import personalizedpagerank.Utility.NodeScores;
import personalizedpagerank.Utility.Parameters;


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
    public MCCompletePathPageRank(final DirectedGraph<Integer, DefaultEdge> g, 
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
        
        /*
        a part of the walks is wasted because a teleport happens before traversing
        the first edge, so we account for those walks here (lowering the total walks)
        but make it so that the first edge is always traversed
        */
        int walks = (int) (this.parameters.getIterations() * this.parameters.getDamping());
        double teleported;
        
        for(int node: g.vertexSet())
        {
            NodeScores map = new NodeScores();

            //each walk begins at node, so scores(node, node) will at least have a
            //value equal to the number of runs
            map.put(node, this.parameters.getIterations());
            //do a number of random walks equal to iterations
            for(int i = 0 ; i < walks; i++)
            {
                int currentNode = node;

                /*
                random walk which stops if a teleport happens (teleported > damping)
                or if it gets into a node without out going edges
                */
                do
                {
                    //get successors of the current node
                    int[] next = successors.get(currentNode);

                    //if the current node has no outgoing edges the walk ends here
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
                }while(teleported <= this.parameters.getDamping());
            }
            map.keepTop(this.parameters.smallTop);
            scores.put(node, map);
        }
        //trim to avoid wasting space
        for(int v: scores.keySet())
            scores.get(v).trim();
    }
}
