package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;

//class to do result comparison of the different algorithms, it assumes that the passed object refer to the same graph
public class ResultComparator
{
    //for jaccard similarity
    private final Jaccard<Integer> jaccard = new Jaccard<>();
    private final Levenstein<Int2DoubleMap.Entry> levenstein = new Levenstein<>();
    
    /**
     * Given 2 algorithms compares their personalized pagerank results of a 
     * set of nodes.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @return Class storing comparison results and running parameters for both algorithms.
     */
    public ComparisonData compare(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes)
    {
        return new ComparisonData(jaccard(alg1, alg2, nodes), levenstein(alg1, alg2, nodes), alg1.getParameters(), alg2.getParameters());
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
    private Result jaccard(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes)
    {
        //min, average, max, std deviation
        double[] res = {1, 0, 0, 0};
        double squareSum = 0;
        for(Integer v: nodes)
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
        return new Result(res);
    }
    
    
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
     * @return Jaccard similarity of results for the selected node
     */
    private double jaccard(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2, final int selected)
    {
       PartialSorter<Int2DoubleOpenHashMap.Entry> sorter = new PartialSorter<>();
       //for the selected node get entries for both algos as arrays
       Int2DoubleMap.Entry[] m1 = alg1.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
       Int2DoubleMap.Entry[] m2 = alg2.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
       Set<Integer> entries1;
       Set<Integer> entries2;
       
       //in case the first array stores topK results and the second stores topL results with K!=L
       if(m1.length != m2.length)
       {
           int min = Math.min(m1.length, m2.length);
           
           sorter.partialSort(m1, min - 1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
            {
                if(e1.getDoubleValue() < e2.getDoubleValue())
                    return -1;
                else if(e1.getDoubleValue() == e2.getDoubleValue())
                    return 0;
                else
                   return 1;
           } );
           
           sorter.partialSort(m2, min - 1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
            {
                if(e1.getDoubleValue() < e2.getDoubleValue())
                    return -1;
                else if(e1.getDoubleValue() == e2.getDoubleValue())
                    return 0;
                else
                   return 1;
           } );
           
           //create sets and do jaccard
           entries1 = new HashSet<>(min);
           entries2 = new HashSet<>(min);
           for(int i = 0; i < min; i++)
           {
               entries1.add(m1[i].getKey());
               entries2.add(m2[i].getKey());
           }
       }
       else
       {
           entries1 = alg1.getMap(selected).keySet();
           entries2 = alg2.getMap(selected).keySet();
       }
       
       return jaccard.similarity(entries1, entries2);
    }
    
    
    /**
     * Given a set of nodes, for each node the levenstein distance between 
     * the top scorers (in terms of personalized pagerank) 
     * provided by the two algorithms is computed. The information is then used
     * to compute the min, average, max and standard deviation of the levenstein
     * distance values.
     * An example of use would be computing those statistics using all the nodes,
     * with
     * levenstein(res1, res2, res1.getMaps().keySet())
     * Once again it's assumed that alg1 and alg2 have been computed from the 
     * same graph.
     * @param alg1 First algorithm providing results.
     * @param alg2 Second algorithm providing results.
     * @param nodes Set of nodes to use for compute the levenstein distances.
     * @return An array of 4 elements, min, average, max, standard deviation of the
     * levenstein distances.
     */
    private Result levenstein(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes)
    {
        //min, average, max, std deviation
        double[] res = {1, 0, 0, 0};
        double squareSum = 0;
        for(Integer v: nodes)
        {
            double tmp = levenstein(alg1, alg2, v);
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
        return new Result(res);
    }
    
    /**
     * Given 2 objects implementing the PersonalizedPagerankAlgorithm interface
     * referring to the same graph (it's assumed, not checked) calculate the levenstein 
     * distance between the nodes that would be the topK scorers for
     * both algorithms, starting from the same "selected" node (see param selected).
     * If one algorithm stores the topK scores while the other one stores the topL
     * with K!=L only the top min(K,L) scoring nodes are considered.
     * @param alg1 The first object containing computed personalized pageranks.
     * @param alg2 The second object containing computed personalized pageranks.
     * @param selected For which node results gets compared.
     * @return Leveinstein distance of results for the selected node.
     */
    private double levenstein(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2, final int selected)
    {
        //for the selected node get entries for both algos as arrays
        Int2DoubleMap.Entry[] m1 = alg1.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
        Int2DoubleMap.Entry[] m2 = alg2.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
        
        //sort entries by values, descending
        Arrays.sort(m1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
         {
             if(e1.getDoubleValue() < e2.getDoubleValue())
                 return 1;
             else if(e1.getDoubleValue() == e2.getDoubleValue())
                 return 0;
             else 
                 return -1;
         });
        
        Arrays.sort(m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
         {
             if(e1.getDoubleValue() < e2.getDoubleValue())
                 return 1;
             else if(e1.getDoubleValue() == e2.getDoubleValue())
                 return 0;
             else 
                 return -1;
         });
        
        //in case the first array stores topK results and the second stores topL results with K!=L
        if(m1.length != m2.length)
        {
            m1 = Arrays.copyOf(m1, Math.min(m1.length, m2.length));
            m2 = Arrays.copyOf(m2, Math.min(m1.length, m2.length));
        }
       
       return levenstein.distance(m1, m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
        {
            return (e1.getKey().equals(e2.getKey()))? 0 : -1;
        });
    }
}
