package personalizedpagerank.Utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import personalizedpagerank.PersonalizedPageRankAlgorithm;

//class to do result comparison of the different algorithms, it assumes that the passed object refer to the same graph
public class ResultComparator<V, D>
{
    /**
     * Given 2 objects implementing the PersonalizedPagerankAlgorithm interface
     * referring to the same graph (it's assumed, not checked) calculate the jaccard 
     * similarity between the sets of nodes that would be the topK scorers for
     * both algorithms, starting from the same "selected" node (see param selected).
     * If one algorithm stores the topK scores while the other one stores the topL
     * with K!=L only the top min(K,L) scoring nodes are considered.
     * @param alg1 The first object containing computed personalized pageranks.
     * @param alg2 The second object containing computed personalized pageranks.
     * @param selected For which node results gets compared.
     * @return 
     */
    public double jaccard(final PersonalizedPageRankAlgorithm<V, D> alg1, final PersonalizedPageRankAlgorithm<V, D> alg2, final V selected)
    {
       PartialSorter<V> sorter = new PartialSorter<>();
       
       //for the selected node get entries for both algos as arrays
       Map.Entry<V, Double>[] m1 = alg1.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       Map.Entry<V, Double>[] m2 = alg2.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       
       //in case alg1 stores topK results and alg2 stores topL results with K!=L
       int min = Math.min(m1.length, m2.length);
       sorter.partialSort(m1, min - 1);
       sorter.partialSort(m2, min - 1);
       
       //create sets to to intersection and union
       Set<V> entries1 = new HashSet<>(min);
       Set<V> entries2 = new HashSet<>(min);
       Set<V> union = new HashSet<>();
       for(int i = 0; i < min; i++)
       {
           entries1.add(m1[i].getKey());
           entries2.add(m2[i].getKey());
       }
       union.addAll(entries1);
       union.addAll(entries2);
       //entries1 becomes the intersection
       entries1.retainAll(entries2);
       
       return ((double) entries1.size()) / union.size();
    }
    
    /**
     * Given 1 object implementing the PersonalizedPagerankAlgorithm interface 
     * and a Map<Nodes, Values> related to the same graph(it's assumed, not checked)
     * calculate the jaccard similarity between the sets of the nodes that would 
     * be the topK scorers for both the algorithm and the map starting from the 
     * same "selected" node (see the param selected),
     * If the algorithm stores topK scores while the map stores topL scores
     * with K!=L only the top min(K,L) scoring nodes are considered.
     * @param alg1 Object containing computed personalized pageranks.
     * @param alg2 Map containing personalized pagerank scores as if "selected"
     * was the origin.
     * @param selected For which node results gets compared.
     * @return 
     */
    public double jaccard(final PersonalizedPageRankAlgorithm<V, D> alg1, final Map<V, D> alg2, final V selected)
    {
       PartialSorter<V> sorter = new PartialSorter<>();
       
       //for the selected node get entries for both algos as arrays
       Map.Entry<V, Double>[] m1 = alg1.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       Map.Entry<V, Double>[] m2 = alg2.entrySet().toArray(new Map.Entry[0]);
       
       
       //in case alg1 stores topK results and alg2 stores topL results with K!=L
       int min = Math.min(m1.length, m2.length);
       sorter.partialSort(m1, min - 1);
       sorter.partialSort(m2, min - 1);
       
       //create sets to to intersection and union
       Set<V> entries1 = new HashSet<>(min);
       Set<V> entries2 = new HashSet<>(min);
       Set<V> union = new HashSet<>();
       for(int i = 0; i < min; i++)
       {
           entries1.add(m1[i].getKey());
           entries2.add(m2[i].getKey());
       }
       union.addAll(entries1);
       union.addAll(entries2);
       //entries 1 becomes the intersection
       entries1.retainAll(entries2);
       
       return ((double) entries1.size()) / union.size();
    }
    
    /**
     * Given a set of nodes, for each node the jaccard similarity between 
     * the two sets of top scorers (in terms of personalized pagerank) 
     * provided by the two algorithms is computed. The information is then used
     * to compute the min, average, max and standard deviation of the jaccard
     * similarity values.
     * An example of use would be computing those statistics using all the nodes,
     * with
     * jaccard(res1, res2, res1.getMaps().keySet())
     * Once again it's assumed that alg1 and alg2 have been computed from the 
     * same graph.
     * @param alg1 First algorithm providing results.
     * @param alg2 Second algorithm providing results.
     * @param nodes Set of nodes to use for compute the jaccard similarities.
     * @return An array of 4 elements, min, average, max, standard deviation of the
     * jaccard similarities.
     */
    public double[] jaccard(final PersonalizedPageRankAlgorithm<V, D> alg1, final PersonalizedPageRankAlgorithm<V, D> alg2, Set<V> nodes)
    {
        //min, average, max, std deviation
        double[] res = {1, 0, 0, 0};
        double squareSum = 0;
        for(V v: nodes)
        {
            double tmp = jaccard(alg1, alg2, v);
            //update min and max
            res[0] = Math.min(res[0], tmp);
            res[2] = Math.max(res[2], tmp);
            
            res[1] += tmp;
            squareSum += tmp * tmp;
        }
        //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) )
        res[3] = Math.sqrt
        (
                (squareSum -(res[1] * res[1]) / nodes.size()) / nodes.size()
        );
        res[1] /= nodes.size();
        return res;
    }
    
    /**
     * Given an algorithm containing personalized pageranks and a map of
     * maps where every map contains the result of personalized pagerank for the
     * node used as a key, for each key in the map of maps computed the jaccard
     * similarity between the two sets of top scorers (in terms of personalized
     * pagerank) provided by the algorithm and the map of maps.
     * The information is then used to compute the min, average, max and 
     * standard deviation of the jaccard similarity values.
     * Once again it's assumed that alg1 and the map of maps have been computed
     * from the same graph.
     * @param alg1 Algorithm containing personalized pagerank results.
     * @param maps Map of maps containing nodes (keys) mapped to their personalized pagerank.
     * @return An array of 4 elements, min, average, max, standard deviation of the 
     * jaccard similarities.
     */
    public double[] jaccard(final PersonalizedPageRankAlgorithm<V, D> alg1, Map<V, Map<V, D>> maps)
    {
        //min, average, max, std deviation
        double[] res = {1, 0, 0, 0};
        double squareSum = 0;
        for(V v: maps.keySet())
        {
            double tmp = jaccard(alg1, maps.get(v), v);
            //update min and max
            res[0] = Math.min(res[0], tmp);
            res[2] = Math.max(res[2], tmp);
            
            res[1] += tmp;
            squareSum += tmp * tmp;
        }
        //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) )
        res[3] = Math.sqrt
        (
                (squareSum -(res[1] * res[1]) / maps.size()) / maps.size()
        );
        res[1] /= maps.size();
        return res;
    }
}
