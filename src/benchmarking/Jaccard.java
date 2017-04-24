package benchmarking;

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
     * If both sets are empty jaccard is defined as 1.
     * @param s1 First set.
     * @param s2 Second set.
     * @return The jaccard similarity between the two sets.
     */
    public double similarity(final Set<V> s1,final Set<V> s2)
    {
        if(s1.isEmpty() && s2.isEmpty())
            return 1d;
        else
        {
            Set<V> union = new HashSet<>();
            Set<V> intersection = new HashSet<>(s1);
            union.addAll(s1);
            union.addAll(s2);
            intersection.retainAll(s2);
            return ((double) intersection.size()) / union.size();
        }
    }
    
}
