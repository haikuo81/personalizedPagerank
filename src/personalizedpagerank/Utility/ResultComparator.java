package personalizedpagerank.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import personalizedpagerank.Parameters;
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
     * @return Jaccard similarity of results for the selected node
     */
    private double jaccard(final PersonalizedPageRankAlgorithm<V, D> alg1, final PersonalizedPageRankAlgorithm<V, D> alg2, final V selected)
    {
       //for the selected node get entries for both algos as arrays
       Map.Entry<V, Double>[] m1 = alg1.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       Map.Entry<V, Double>[] m2 = alg2.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       
       return jaccardWrapped(m1, m2);
    }
    
    /**
     * Computes the jaccard similarity between two Map.Entry arrays.
     * If an array stores topK entries while the other one stores topL entries
     * with K!=L only the top min(K,L) entries are considered.
     * @param m1 First map.
     * @param m2 Second map.
     * @return Jaccard similarity between the min(m1.length, m2.length) sorted entries.
     */
    private double jaccardWrapped(Map.Entry<V, Double>[] m1, Map.Entry<V, Double>[] m2)
    {
       PartialSorter<V> sorter = new PartialSorter<>();
        //in case the first array stores topK results and the second stores topL results with K!=L
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
     * Class which stores the results of the comparison and the running
     * parameters of the confronted algorithms.
     */
    private class Results
    {
        private final double min;
        private final double average;
        private final double max;
        private final double std;//standard deviation
        private final Parameters param1;//parameters of the first algorithm
        private final Parameters param2;//parameters of the second algorithm
        
        private Results(final double min, final double average, final double max, 
                final double std, Parameters param1, Parameters param2)
        {
            this.min = min;
            this.average= average;
            this.max = max;
            this.std = std;
            this.param1 = param1;
            this.param2 = param2;
        }

        public double getMin() {
            return min;
        }

        public double getAverage() {
            return average;
        }

        public double getMax() {
            return max;
        }

        public double getStd() {
            return std;
        }

        public Parameters getParam1() {
            return param1;
        }

        public Parameters getParam2() {
            return param2;
        }
    }
    

    public double levenstein(final PersonalizedPageRankAlgorithm<V, D> alg1, final PersonalizedPageRankAlgorithm<V, D> alg2, final V selected)
    {
       //for the selected node get entries for both algos as arrays
       Map.Entry<V, Double>[] m1 = alg1.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       Map.Entry<V, Double>[] m2 = alg2.getMap(selected).entrySet().toArray(new Map.Entry[0]);
       
       return levensteinWrapped(m1, m2);
    }
    
    //wip
    private int levensteinWrapped(Map.Entry<V, Double>[] m1, Map.Entry<V, Double>[] m2)
    {
        //in case the first array stores topK results and the second stores topL results with K!=L
        int min = Math.min(m1.length, m2.length);
        Arrays.sort(m1, (Map.Entry<V, Double> e1, Map.Entry<V, Double> e2) ->
        {
                return e2.getValue().compareTo(e1.getValue());
        });
        Arrays.sort(m2, (Map.Entry<V, Double> e1, Map.Entry<V, Double> e2) ->
        {
                return e2.getValue().compareTo(e1.getValue());
        });
        //do levenstein but consider only a string formed by the top values
        int [][] matrix = new int[min+1][min+1];
        
        //if the string is empty the distance is the length of the non empty string
        for (int i = 0; i <= min; i++)
            matrix[i][0] = i;
        for (int u = 0; u <= min; u++)
            matrix[0][u] = u;

        //filling from top-left to bottom-right 
        for (int i = 1; i < matrix.length; i++)
        {
            for (int u = 1; u < matrix[i].length; u++)
            {
                //if values are equals this step is free
                if (m1[i-1].getKey().equals(m2[u-1].getKey()))
                    matrix[i][u] = matrix[i-1][u-1];
                else
                {
                    //if values aren't equals we need to modify a string
                    //pick the sequence that can lead you here with min moves
                    matrix[i][u] = Math.min(matrix[i-1][u], matrix[i][u-1]);
                    matrix[i][u] = Math.min(matrix[i][u], matrix[i-1][u-1]);
                    matrix[i][u]++;
                }
            }
        }
       int res = Integer.MAX_VALUE;
       int[] handle = matrix[matrix.length - 1];
       for(int i = 0; i < handle.length; i++)
           res = Math.min(res, handle[i]);
       return res;
    }
               
}
