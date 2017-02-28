package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;

//class to do result comparison of the different algorithms, it assumes that the passed objects refer to the same graph
public class AlgorithmComparator
{
    private AlgorithmComparator(){}
    
    private static final Jaccard<Integer> JACCARD = new Jaccard<>();
    private static final Levenstein<Int2DoubleMap.Entry> LEVENSTEIN = new Levenstein<>();
    private static final PartialSorter<Int2DoubleOpenHashMap.Entry> SORTER = new PartialSorter<>();
    
    
    //interface for functions returning some kind of value based on the personalized pagerank results for a node
    //see the implementing classes for details 
    static private interface Function
    {
        double apply(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2,
                final int selected, final int k);
    }
    
    /**
     * Given a set of nodes, for each node a function returning a value
     * depending on the two sets of top scorers (in terms of personalized
     * pagerank) provided by the two algorithms is computed. The information is
     * then used to compute the min, average, max and standard deviation of the
     * values returned by the function. It's assumed that alg1 and alg2 have
     * been computed from the same graph.
     * @param alg1 First algorithm providing results.
     * @param alg2 Second algorithm providing results.
     * @param nodes Set of nodes to use as an argument for the function..
     * @param function A Function that will return some kind of significant
     * value (i.e. a coefficient) given a node and the 2 algorithms having
     * personalized pagerank scores for that node.
     * @return An array of 4 elements, min, average, max, standard deviation of
     * the values returned by the function.
     */
    static private Result getStats(final PersonalizedPageRankAlgorithm alg1, final PersonalizedPageRankAlgorithm alg2,
            Set<Integer> nodes, Function function, final int k) 
    {
        //min, average, max, std deviation
        double min, average, max, std, squareSum;
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        average = squareSum = 0;
        
        for (Integer v : nodes) 
        {
            double tmp = function.apply(alg1, alg2, v, k);
            //update min and max
            min = Math.min(min, tmp);
            max = Math.max(max, tmp);

            average += tmp;
            squareSum += tmp * tmp;
        }
        //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) )
        std = Math.sqrt
        (
                (squareSum - (average * average) / nodes.size()) / nodes.size()
        );
        average /= nodes.size();
        return new Result(min, average, max, std);
    }
    
    /**
     * Given 2 objects implementing the PersonalizedPagerankAlgorithm interface
     * referring to the same graph (it's assumed, not checked) calculate the
     * jaccard similarity between the sets of nodes that would be the topK
     * scorers for both algorithms, starting from the same "selected" node (see
     * param selected). If one algorithm stores the topK scores while the other
     * one stores the topL with K!=L only the top min(K,L) scoring nodes are
     * considered.
     *
     * @param alg1 The first object containing computed personalized pageranks.
     * @param alg2 The second object containing computed personalized pageranks.
     * @param selected For which node results gets compared.
     * @param k Max number of entries to keep for each node from the entries of alg1
     * and alg2. (as if the entries were ordered by value descending)
     * @return Jaccard similarity of results for the selected node
     */
    static private class JaccardFunction implements Function
    {
        @Override
        public double apply(PersonalizedPageRankAlgorithm alg1, PersonalizedPageRankAlgorithm alg2,
                final int selected, final int k) 
        {
            
            //for the selected node get entries for both algos as arrays
            Int2DoubleMap.Entry[] m1 = alg1.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = alg2.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Set<Integer> entries1;
            Set<Integer> entries2;

            //in case the first array stores topK results and the second stores topL results with K!=L
            //or if the length is different from k
            if (m1.length != m2.length || m1.length != k)
            {
                int min = Math.min(m1.length, m2.length);
                min = Math.min(min, k);
                SORTER.partialSort(m1, min - 1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                        -> {
                    return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                            : e1.getDoubleValue() == e2.getDoubleValue() ?
                            (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;
                });

                SORTER.partialSort(m2, min - 1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                        -> {
                    return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                            : e1.getDoubleValue() == e2.getDoubleValue() ?
                            (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;
                });

                //create sets and do jaccard
                entries1 = new HashSet<>(min);
                entries2 = new HashSet<>(min);
                for (int i = 0; i < min; i++) 
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
            return JACCARD.similarity(entries1, entries2);
        }    
    }
    
    /**
     * Given 2 objects implementing the PersonalizedPagerankAlgorithm interface
     * referring to the same graph (it's assumed, not checked) calculate the
     * levenstein distance between the nodes that would be the topK scorers for
     * both algorithms, starting from the same "selected" node (see param
     * selected). If one algorithm stores the topK scores while the other one
     * stores the topL with K!=L only the top min(K,L) scoring nodes are
     * considered.
     * @param alg1 The first object containing computed personalized pageranks.
     * @param alg2 The second object containing computed personalized pageranks.
     * @param selected For which node results gets compared.
     * @param k Max number of entries to keep for each node from the entries of alg1
     * and alg2. (as if the entries were ordered by value descending)
     * @return Leveinstein distance of results for the selected node.
     */
    static private class LevensteinFunction implements Function
    {
        @Override
        public double apply(PersonalizedPageRankAlgorithm alg1, PersonalizedPageRankAlgorithm alg2,
                final int selected, final int k) 
        {
            //for the selected node get entries for both algos as arrays
            Int2DoubleMap.Entry[] m1 = alg1.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = alg2.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);

            //sort entries by values, descending
            Arrays.sort(m1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue() ? 
                        (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
            });

            Arrays.sort(m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue() ?
                        (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
            });

            //in case the first array stores topK results and the second stores topL results with K!=L
            //or if the length is different from k
            if (m1.length != m2.length || m1.length != k) 
            {
                m1 = Arrays.copyOf(m1, Math.min(Math.min(m1.length, m2.length), k));
                m2 = Arrays.copyOf(m2, Math.min(Math.min(m1.length, m2.length), k));
            }

            return LEVENSTEIN.distance(m1, m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
            -> {
                return (e1.getKey().equals(e2.getKey())) ? 0 : -1;
            })/(double)Math.min(Math.min(m1.length, m2.length), k);
        }        
    }
    
    /**
     * https://en.wikipedia.org/wiki/Spearman's_rank_correlation_coefficient
     *
     * Given 2 objects implementing the PersonalizedPagerankAlgorithm interface
     * referring to the same graph (it's assumed, not checked) calculate the
     * spearman rank correlation coefficient between the positions that both
     * algorithms would give to the top K ordered descending entries of alg1.
     * It's assumed that every node that is in the topK of alg1 is a key in
     * alg2.getMap(selected), otherwise the node is given a default position
     * given by the size of the keyset of the map from alg2.
     * For an isolated node (alg1.getMap(selected) == 1) the result will always be 1.
     * @param alg1 The first object containing computed personalized pageranks.
     * @param alg2 The second object containing computed personalized pageranks.
     * @param selected For which node results gets compared.
     * @param k Max number of entries to keep for each node in the entries of alg1.
     * (as if the entries were ordered by value descending)
     * @return Spearman correlation of the positions of the results for the
     * selected node.
     */
    static private class SpearmanFunction implements Function
    {
        @Override
        public double apply(PersonalizedPageRankAlgorithm alg1, PersonalizedPageRankAlgorithm alg2,
                final int selected, final int k) 
        {
            //if it's an isolated node just return 1
            if(alg1.getMap(selected).entrySet().size() == 1)
                return 1;
            
            //for the selected node get entries for both algos as arrays
            Int2DoubleMap.Entry[] m1 = alg1.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = alg2.getMap(selected).entrySet().toArray(new Int2DoubleMap.Entry[0]);

            //sort entries by values, descending
            Arrays.sort(m1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue() ?
                        (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
            });

            Arrays.sort(m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue() ?
                        (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
            });
            //keep only the top k
            m1 = Arrays.copyOf(m1, Math.min(m1.length, k));
            
            //if the firts k ordered values are the same just return 1
            boolean same = true;
            for(int i = 0; i < m1.length && same; i++)
                same = m1[i].getIntKey() == m2[i].getIntKey();
            if(same)
                return 1;
            
            //for the second algorithm associate every node to it's position in the order
            //when ranked by the second algorithm
            Int2IntOpenHashMap positionsAlg2 = new Int2IntOpenHashMap(m2.length);
            //to avoid mapping every single node ASAP we map nodes in chunks of 100
            int currentIndex = 0;
            int currentLimit = 0;
            //return value to use when there's nothing mapped to a key
            positionsAlg2.defaultReturnValue(-1);

            //for every node of the topK from alg1
            //store the rankings (position) which the 2 algorithms would give to the node
            double[] ranks1 = new double[m1.length];
            double[] ranks2 = new double[m1.length];

            //add to ranks1 and ranks2 the positions that a node assumes in the topK
            //from alg1 and in alg2
            for (int i = 0; i < m1.length; i++) 
            {
                int tmp;

                //if the m1[i] node is missing from the map read a chunk of nodes from the m2 array
                while ((tmp = positionsAlg2.get(m1[i].getIntKey())) == -1) 
                {
                    //if the entire array has been read but the node still isn't mapped
                    //give the node a position of m2.length
                    if (currentLimit == m2.length) 
                        tmp = m2.length;
                    currentLimit += 100;
                    currentLimit = Math.min(currentLimit, m2.length);
                    for (; currentIndex < currentLimit; currentIndex++) 
                        positionsAlg2.put(m2[currentIndex].getIntKey(), currentIndex);
                }

                ranks1[i] = i;
                ranks2[i] = tmp;
            }

            return Pearson.correlation(ranks1, ranks2);
        }        
    }
    
    /**
     * Given 2 algorithms compares their personalized pagerank results of a 
     * set of nodes.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param differentKs Which Ks will be used in the comparison of the algorithm
     * results, given a K, if a node is mapped to a map containing the scores
     * of more than K nodes only the top K scoring nodes are kept.
     * Each comparison data is a comparison done with a different K.
     * @return Array of comparison data containing the results of the comparison
     * between the two algorithms for each different K used.
     */
   static  public ComparisonData[] compare(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, 
            int[] differentKs)
    {
        ComparisonData[] res = new ComparisonData[differentKs.length];
        for(int i = 0 ; i < differentKs.length; i++)
        {
            Result jc = getStats(alg1, alg2, nodes, new JaccardFunction(), differentKs[i]);
            Result spear = getStats(alg1, alg2, nodes, new SpearmanFunction(), differentKs[i]);
            Result lev = getStats(alg1, alg2, nodes, new LevensteinFunction(), differentKs[i]);
            res[i] = new ComparisonData(differentKs[i], jc, lev, spear, alg1.getParameters(), alg2.getParameters());
        }
        return res;
    }
    
    /**
     * Given 2 algorithms compares their personalized pagerank results of a 
     * set of nodes.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param k Given a k, if a node is mapped to a map containing the scores
     * of more than K nodes only the top K scoring nodes are kept.
     * @return Comparison data containing the results of the comparison
     * between the two algorithms related to the K used.
     */
    static public ComparisonData compare(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, int k)
    {
        Result jc = getStats(alg1, alg2, nodes, new JaccardFunction(), k);
        Result spear = getStats(alg1, alg2, nodes, new SpearmanFunction(), k);
        Result lev = getStats(alg1, alg2, nodes, new LevensteinFunction(), k);
        return new ComparisonData(k, jc, lev, spear, alg1.getParameters(), alg2.getParameters());
    }
}
