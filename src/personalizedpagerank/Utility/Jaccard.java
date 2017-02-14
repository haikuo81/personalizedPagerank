package personalizedpagerank.Utility;

import java.util.HashSet;
import java.util.Set;

/**
 * Java class for jaccard comparison between sets, took it out
 * of ResultComparator for testing purposes.
 * @param <V> Type of objects contained in the set.
 */
public class Jaccard<V> 
{
    /**
     * Given two sets return the jaccard similarity between them, which is
     * |intersection|/|union|, [0,1].
     * @param s1 First set.
     * @param s2 Second set.
     * @return The jaccard similarity between the two sets.
     */
    public double similarity(Set<V> s1, Set<V> s2)
    {
        Set<V> union = new HashSet<>();
        union.addAll(s1);
        union.addAll(s2);
        //entries1 becomes the intersection
        s1.retainAll(s2);
        return ((double) s1.size()) / union.size();
    }
    
}
