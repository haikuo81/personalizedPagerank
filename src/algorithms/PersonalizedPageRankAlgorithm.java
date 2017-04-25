package algorithms;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utility.NodeScores;

/**
 * Abstract class for classes that will contain scores related to personalized
 * pagerank.
 */
abstract public class PersonalizedPageRankAlgorithm
{
    protected DirectedGraph<Integer, DefaultEdge> g;
    protected Int2ObjectOpenHashMap<NodeScores> scores;
    
    /**
     * Retrieves a map containing the personalized pagerank scores for a node.
     * Given a node "origin" the map contains pagerank scores for (a number)
     * of nodes  in the graph as if pagerank was run having the origin node
     * as the only starting node and the only node in the teleport set.
     * @param origin Origin node for the pagerank scores in the map.
     * @return A map where key values are nodes from the graph and are mapped
     * to personalized pagerank scores.
     */
    public NodeScores getMap(final int origin)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        return scores.get(origin);
    }
    
    /**
     * Retrieves a map where keys are nodes of the graph used to run the algorithm,
     * values are a map where keys are nodes, and values are whatever object is
     * used to represent the pagerank score.
     * Each map returned associated with a node is a map containing personalized 
     * pagerank scores of a number of nodes, with the key used to retrieve the map
     * being the origin node (the only node in the starting and the teleport set).
     * @return Map of maps containing personalized pagerank scores.
     */
    public Int2ObjectOpenHashMap<NodeScores> getMaps()
    {
        return scores;
    }
    /**
     * Given an origin node and a target node get the personalized pagerank score
     * of the target node with origin node being the starting node and the only
     * node of the teleport set.
     * @param origin The origin node of the personalized pagerank.
     * @param target Node for which the pagerank score is returned.
     * @return Personalized pagerank score of target node.
     */
    public double getRank(final int origin,final int target)
    {
        if(!g.containsVertex(origin))
            throw new IllegalArgumentException("Origin vertex isn't part of the graph.");
        if(!g.containsVertex(target))
            throw new IllegalArgumentException("Target vertex isn't part of the graph.");
        return scores.get(origin).get(target);
    }
    
    /**
     * Returns the parameters used to run the algorithm.
     * @return An object containing the running parameters (may be a subclass).
     */
    abstract public Parameters getParameters();
    
    /**
     * Returns the graph for which the algorithm was run.
     * @return 
     */
    public DirectedGraph<Integer, DefaultEdge> getGraph()
    {
        return g;
    }
    
    //parameters class, used as a base for other algorithms parameters
    static public class Parameters 
    {
        private final int edges;
        private final int vertices;
        private final int iterations; 
        private final double damping;
        private final double tolerance;

        public Parameters(final int vertices, final int edges, final int iterations,
                final double damping, final double tolerance)
        {
            this.edges = edges;
            this.vertices = vertices;
            this.iterations = iterations;
            this.damping = damping;
            this.tolerance = tolerance;
        }

        //generated automatically
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Parameters other = (Parameters) obj;
            if (this.edges != other.edges) {
                return false;
            }
            if (this.vertices != other.vertices) {
                return false;
            }
            if (this.iterations != other.iterations) {
                return false;
            }
            if (Double.doubleToLongBits(this.damping) != Double.doubleToLongBits(other.damping)) {
                return false;
            }
            if (Double.doubleToLongBits(this.tolerance) != Double.doubleToLongBits(other.tolerance)) {
                return false;
            }
            return true;
        }

        public int getIterations() {
            return iterations;
        }

        public double getDamping() {
            return damping;
        }

        public double getTolerance() {
            return tolerance;
        }

        public int getEdges() {
            return edges;
        }

        public int getVertices() {
            return vertices;
        }
    }
}
