package personalizedpagerank;

import java.util.Map;

/**
 *
 * @param <V> Type of nodes.
 * @param <D> Value of the rank.
 */
public interface PersonalizedPageRankAlgorithm<V extends Object, D extends Object>
{
    public Map<V, D> getMap(V origin);
    
    public Map<V, Map<V, D>> getMaps();
    
    public D getRank(V origin, V target);
}
