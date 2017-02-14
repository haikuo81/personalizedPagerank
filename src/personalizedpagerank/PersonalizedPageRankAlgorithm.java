package personalizedpagerank;

import java.util.Map;

/**
 * @param <V> Type of nodes of the graph for which the algorithm has been run.
 * @param <D> An object representing the values/scores.
 */
public interface PersonalizedPageRankAlgorithm<V extends Object, D extends Object>
{
    /**
     * Retrieves a map containing the personalized pagerank scores for a node.
     * Given a node "origin" the map contains pagerank scores for (a number)
     * of nodes  in the graph as if pagerank was run having the origin node
     * as the only starting node and the only node in the teleport set.
     * The map returned could be unmodifiable.
     * @param origin Origin node for the pagerank scores in the map.
     * @return A map where key values are nodes from the graph and are mapped
     * to personalized pagerank scores.
     */
    public Map<V, D> getMap(V origin);
    
    /**
     * Retrieves a map where keys are nodes of the graph used to run the algorithm,
     * values are a map where keys are nodes, and values are whatever object is
     * used to represent the pagerank score.
     * Each map returned associated with a node is a map containing personalized 
     * pagerank scores of a number of nodes, with the key used to retrieve the map
     * being the origin node (the only node in the starting and the teleport set).
     * The map returned could be unmodifiable.
     * @return Map of maps containing personalized pagerank scores.
     */
    public Map<V, Map<V, D>> getMaps();
    
    /**
     * Given an origin node and a target node get the personalized pagerank score
     * of the target node with origin node being the starting node and the only
     * node of the teleport set.
     * @param origin The origin node of the personalized pagerank.
     * @param target Node for which the pagerank score is returned.
     * @return Personalized pagerank score of target node.
     */
    public D getRank(V origin, V target);
    
    /**
     * Returns the parameters used to run the algorithm.
     * @return An object containing the running parameters (may be a subclass).
     */
    public Parameters getParameters();
}
