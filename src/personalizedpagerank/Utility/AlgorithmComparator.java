package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.PageRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;

//class to do result comparison of the different algorithms, it assumes that the passed objects refer to the same graph
public class AlgorithmComparator
{
    private AlgorithmComparator(){}
    
    private static final Jaccard<Integer> JACCARD = new Jaccard<>();
    private static final PartialSorter<Int2DoubleOpenHashMap.Entry> SORTER = new PartialSorter<>();
     
    /**
     * Given 2 algorithms compares their personalized pagerank results for a 
     * set of nodes and return data about the min, average, max and standard
     * deviation of the jaccard similarity and Kendall coefficient 
     * for the top K scores for each node of the set of nodes, 
     * for every K in the Ks array given as input.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param differentKs Which Ks will be used in the comparison of the
     * algorithm results, given a K, if a node is mapped to a map containing the
     * scores of more than K nodes only the top K scoring nodes are kept.
     * @return Comparison data containing the results of the comparison
     * between the two algorithms related to the K used.
     */
    public static ComparisonData[] compare(PersonalizedPageRankAlgorithm alg1, PersonalizedPageRankAlgorithm alg2,
            Set<Integer> nodes, int[] differentKs) 
    {
        //min, average, max, std deviation for jaccard
        double[] Jmin = new double[differentKs.length];
        double[] Javerage = new double[differentKs.length];
        double[] Jmax = new double[differentKs.length];
        double[] Jstd = new double[differentKs.length];
        double[] JsquareSum = new double[differentKs.length];
        
        //min, average, max, std deviation for Kendall
        double[] Kmin = new double[differentKs.length];
        double[] Kaverage = new double[differentKs.length];
        double[] Kmax = new double[differentKs.length];
        double[] Kstd = new double[differentKs.length];
        double[] KsquareSum = new double[differentKs.length];
        
        Arrays.fill(Jmin, Double.MAX_VALUE);
        Arrays.fill(Jmax, Double.MIN_VALUE);
        Arrays.fill(Javerage, 0d);
        Arrays.fill(JsquareSum, 0d);
        
        Arrays.fill(Kmin, Double.MAX_VALUE);
        Arrays.fill(Kmax, Double.MIN_VALUE);
        Arrays.fill(Kaverage, 0d);
        Arrays.fill(KsquareSum, 0d);
        
        //sort the Ks so comparison can be done with growing sets/arrays
        Arrays.sort(differentKs);
        
        /*
        for each node get the entries from their personalized pagerank map,
        sort the entries of that map and  for each k use the sorted arrays of entries to
        calculate jaccard and Kendall coefficients
        */
        for (Integer node : nodes) 
        {
            //get maps and sort entries
            Int2DoubleOpenHashMap map1 = alg1.getMap(node);
            Int2DoubleOpenHashMap map2 = alg2.getMap(node);

            Int2DoubleMap.Entry[] m1 = map1.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = map2.entrySet().toArray(new Int2DoubleMap.Entry[0]);

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
                
            //max size will be the last K (since the Ks are ordered)
            Set<Integer> j1 = new HashSet<>(differentKs[differentKs.length-1]);
            Set<Integer> j2 = new HashSet<>(differentKs[differentKs.length-1]);
            
            ArrayList<Double> k1 = new ArrayList<>(differentKs[differentKs.length-1]);
            ArrayList<Double> k2 = new ArrayList<>(differentKs[differentKs.length-1]);
            
            for(int i = 0; i < differentKs.length; i++)
            {
                //add to sets
                /*
                if K is the min(K,min(map1.size(),map2.size()))
                only the top K entries from both arrays are kept, else the top min(map1.size(),
                map2.size()) entries are kept.
                */
                int min = Math.min(m1.length, m2.length);
                min = Math.min(min, differentKs[i]);
                
                for(int u = j1.size(); u < min; u++)
                {
                    j1.add(m1[u].getIntKey());
                    j2.add(m2[u].getIntKey());
                }
                
                for(int u = k1.size(); u < min; u++)
                {
                    k1.add(m1[u].getDoubleValue());
                    k2.add(map2.get(m1[u].getIntKey()));
                }
                
                double Jtmp = JACCARD.similarity(j1, j2);
                double Ktmp = Kendall.correlation(k1, k2, false);

                //update min and max for jaccard
                Jmin[i] = Math.min(Jmin[i], Jtmp);
                Jmax[i] = Math.max(Jmax[i], Jtmp);

                Javerage[i] += Jtmp;
                JsquareSum[i] += Jtmp * Jtmp;

                //update min and max for kendall
                Kmin[i] = Math.min(Kmin[i], Ktmp);
                Kmax[i] = Math.max(Kmax[i], Ktmp);

                Kaverage[i] += Ktmp;
                KsquareSum[i] += Ktmp * Ktmp;
            }
        }
        
        ComparisonData[] res = new ComparisonData[differentKs.length];
        
        /*
        for each k calculate the std and average for jaccard and Kendall
        then store those results
        */
        for(int i = 0; i < differentKs.length; i++)
        {
            //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) ) for jaccard
            Jstd[i] = Math.sqrt(
                    (JsquareSum[i] - (Javerage[i] * Javerage[i]) / nodes.size()) / nodes.size()
            );
            Javerage[i] /= nodes.size();

            //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) ) for Kendall
            Kstd[i] = Math.sqrt(
                    (KsquareSum[i] - (Kaverage[i] * Kaverage[i]) / nodes.size()) / nodes.size()
            );
            Kaverage[i] /= nodes.size();

            Result Jr = new Result(Jmin[i], Javerage[i], Jmax[i], Jstd[i]);
            Result Kr = new Result(Kmin[i], Kaverage[i], Kmax[i], Kstd[i]);
            res[i] = new ComparisonData(differentKs[i], Jr, Kr, alg1.getParameters(), alg2.getParameters());
        }
        return res;
    }
      
    /**
     * Given 2 algorithms compares their personalized pagerank results for
     * each node of the "nodes" parameter, returning data about them and their 
     * neighbours for each different K in differentKs, see differentKs
     * param for more information.
     * If neighbours of a node are not part of the "nodes" param this means
     * that their personalized pagerank scores have not been computed, so the
     * data about neighbour jaccard and neighbour Kendall is flagged with -1 for
     * a node that has such neighbours.
     * AlgorithmComparator.compare is about general statistics, this method is
     * about getting data point by point.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param differentKs Which Ks will be used in the comparison of the algorithm
     * results, given a K, if a node is mapped to a map containing the scores
     * of more than K nodes only the top K scoring nodes are kept.
     * Each comparison data is a comparison done with a different K.
     * @return Data about every origin node, in degree, out degree, pagerank 
     * centrality, jaccard, Kendall for itself and the neighbour, for each 
     * different K
     */
    public static NodesComparisonData[] compareOrigins(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, int[] differentKs) 
    {
        DirectedGraph<Integer,DefaultEdge> g = alg1.getGraph(); 
        
        //sort the Ks so comparison can be done with growing sets/arrays
        Arrays.sort(differentKs);
        
        //pagerank value (not personalized pagerank)
        Map<Integer, Double> pagerank = (new PageRank<>(g, alg1.getParameters().getDamping(), 
                alg1.getParameters().getIterations(), 
                alg1.getParameters().getTolerance()).getScores());
        NodesComparisonData[] res = new NodesComparisonData[differentKs.length];
        
        //(k, nodes.size(), alg1.getParameters(), alg2.getParameters());
        
        //maps to store jaccard/kendall values to avoid calculating them more than once
        Int2DoubleOpenHashMap[] jMap = new Int2DoubleOpenHashMap[differentKs.length];
        Int2DoubleOpenHashMap[] kMap = new Int2DoubleOpenHashMap[differentKs.length];
        
        /*
        map of cumulative personalized pagerank error for each node, for every
        node in the top K of alg1 and for every node in the top K of alg2
        the double value mapped to that node is incremented by the absolute
        difference between the personalized pagerank score assigned by alg1
        and alg2 divided by the score assigned by alg2,
        this is repeated for every origin node (node part of the "nodes"
        set parameter)
        */
        Int2DoubleOpenHashMap[] errorMap = new Int2DoubleOpenHashMap[differentKs.length];
        
        /**
         * for every node in the top K of alg2 that is not in the top K of alg1
         * increment the value associated to that node by 1
         * this is repeated
         * for every origin node (node part of the "nodes" set parameter)
         */
        Int2IntOpenHashMap[] excludedMap = new Int2IntOpenHashMap[differentKs.length];
        
        /**
         * for every node in the top K of alg1 that is not in the top K of alg2
         * increment the value associated to that node by 1
         * this is repeated for every origin node (node part of the "nodes"
         * set parameter)
         */
        Int2IntOpenHashMap[] includedMap = new Int2IntOpenHashMap[differentKs.length];
        
        /*
        for each different K compute errors/excluded/included maps, initialize
        other maps and NodesComparisoData
        */
        for(int i = 0; i < differentKs.length; i++)
        {
            res[i] = new NodesComparisonData(differentKs[i], nodes.size(), 
                    alg1.getParameters(), alg2.getParameters());
            
            Int2DoubleOpenHashMap err = new Int2DoubleOpenHashMap(g.vertexSet().size());
            Int2IntOpenHashMap exl = new Int2IntOpenHashMap(g.vertexSet().size());
            Int2IntOpenHashMap incl = new Int2IntOpenHashMap(g.vertexSet().size());
            errors(alg1, alg2, nodes, differentKs[i], err, exl, incl);
            errorMap[i] = err;
            excludedMap[i] = exl;
            includedMap[i] = incl;
            
            jMap[i] = new Int2DoubleOpenHashMap(nodes.size());
            kMap[i] = new Int2DoubleOpenHashMap(nodes.size());
            jMap[i].defaultReturnValue(-1d);
            kMap[i].defaultReturnValue(-1d);
        }

        //set stats for nodes that can be computed now
        int index = 0;
        for(Integer node: nodes)
        {
            //get maps and sort entries
            Int2DoubleOpenHashMap map1 = alg1.getMap(node);
            Int2DoubleOpenHashMap map2 = alg2.getMap(node);

            Int2DoubleMap.Entry[] m1 = map1.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = map2.entrySet().toArray(new Int2DoubleMap.Entry[0]);

            //sort entries by values, descending
            Arrays.sort(m1, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue()
                        ? (e1.getIntKey() < e2.getIntKey() ? -1 : 1) : -1;
            });

            Arrays.sort(m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                    -> {
                return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                        : e1.getDoubleValue() == e2.getDoubleValue()
                        ? (e1.getIntKey() < e2.getIntKey() ? -1 : 1) : -1;
            });

            //max size will be the last K (since the Ks are ordered)
            Set<Integer> j1 = new HashSet<>(differentKs[differentKs.length - 1]);
            Set<Integer> j2 = new HashSet<>(differentKs[differentKs.length - 1]);

            ArrayList<Double> k1 = new ArrayList<>(differentKs[differentKs.length - 1]);
            ArrayList<Double> k2 = new ArrayList<>(differentKs[differentKs.length - 1]);
            
            for(int k = 0; k < differentKs.length; k++)
                {
                    //add to sets
                    /*
                    if K is the min(K,min(map1.size(),map2.size()))
                    only the top K entries from both arrays are kept, else the top min(map1.size(),
                    map2.size()) entries are kept.
                    */
                    int min = Math.min(m1.length, m2.length);
                    min = Math.min(min, differentKs[k]);

                    for(int u = j1.size(); u < min; u++)
                    {
                        j1.add(m1[u].getIntKey());
                        j2.add(m2[u].getIntKey());
                    }

                    for(int u = k1.size(); u < min; u++)
                    {
                        k1.add(m1[u].getDoubleValue());
                        k2.add(map2.get(m1[u].getIntKey()));
                    }

                    jMap[k].put(node.intValue(), JACCARD.similarity(j1, j2));
                    kMap[k].put(node.intValue(), Kendall.correlation(k1, k2, false));

                    res[k].setId(index, node);
                    res[k].setIndegree(index, g.inDegreeOf(node));
                    res[k].setOutdegree(index, g.outDegreeOf(node));
                    res[k].setPagerank(index, pagerank.get(node));
                    res[k].setJaccard(index, jMap[k].get(node.intValue()));
                    res[k].setKendall(index, kMap[k].get(node.intValue()));
                    res[k].setPagerankError(index, errorMap[k].get(node.intValue()));
                    res[k].setExcluded(index, excludedMap[k].get(node.intValue()));
                    res[k].setIncluded(index, includedMap[k].get(node.intValue()));
            }
            index++;
        }
        
        //set stats for nodes that required neighbour information
        index = 0;
        for(Integer node: nodes)
        {
            for(int i = 0; i < differentKs.length; i++)
            {
                boolean skipNeighbourhood = false;
                double in = 0;//in degree
                double out = 0;//out degree
                double pr = 0;//page rank
                double j = 0;//jaccard
                double k = 0;//kendall
                double e = 0;//pagerank error
                double ex = 0;//excluded
                double inc = 0;//included
                int neighbourHood = 0;
                
                //father nodes
                for(DefaultEdge edge: g.incomingEdgesOf(node))
                {
                    neighbourHood++;
                    int neighbour = Graphs.getOppositeVertex(g, edge, node);
                    in += g.inDegreeOf(neighbour);
                    out += g.outDegreeOf(neighbour);
                    pr += pagerank.get(neighbour);
                    j += jMap[i].get(neighbour);
                    k += kMap[i].get(neighbour);
                    e += errorMap[i].get(neighbour);
                    ex += excludedMap[i].get(neighbour);
                    inc += includedMap[i].get(neighbour);

                    //only need to check one of the maps to check if the neighbour
                    //is not part of the nodes for which personalized pagerank scores
                    //have been calculated
                    skipNeighbourhood = skipNeighbourhood || jMap[i].get(neighbour) == -1;
                }

                //children nodes
                for(DefaultEdge edge: g.outgoingEdgesOf(node))
                {
                    neighbourHood++;
                    int neighbour = Graphs.getOppositeVertex(g, edge, node);
                    in += g.inDegreeOf(neighbour);
                    out += g.outDegreeOf(neighbour);
                    pr += pagerank.get(neighbour);
                    j += jMap[i].get(neighbour);
                    k += kMap[i].get(neighbour);
                    e += errorMap[i].get(neighbour);
                    ex += excludedMap[i].get(neighbour);
                    inc += includedMap[i].get(neighbour);

                    //only need to check one of the maps to check if the neighbour
                    //is not part of the nodes for which personalized pagerank scores
                    //have been calculated
                    skipNeighbourhood = skipNeighbourhood || jMap[i].get(neighbour) == -1;
                }

                if(neighbourHood > 0)
                {
                    in /= neighbourHood;
                    out /= neighbourHood;
                    pr /= neighbourHood;
                    j /= neighbourHood;
                    k /= neighbourHood;
                    e /= neighbourHood;
                    ex /= neighbourHood;
                    inc /= neighbourHood;
                }
                
                //if neighbourhood jaccard/kendall data has no value flag it
                if(skipNeighbourhood)
                    j = k = e = -1d;

                res[i].setNeighbourIn(index, in);
                res[i].setNeighbourOut(index, out);
                res[i].setNeighbourPagerank(index, pr);
                res[i].setNeighbourJaccard(index, j);
                res[i].setNeighbourKendall(index, k);
                res[i].setNeighbourPagerankError(index, e);
                res[i].setNeighbourExcluded(index, ex);
                res[i].setNeighbourIncluded(index, inc);
            }
            index ++;
        }
        return res;
    }
    
    /**
     * Given 2 algorithms calculates 3 kinds of "errors" cumulatively for each
     * node of the graph.
     * For each origin node in the "nodes" parameter the top K from alg1 and 
     * alg2 scores are used to increment values in 3 different maps which associate
     * to a node of the graph some kind of value.
     * errMap: for each node accumulates the normalised difference between the 
     * rank given by alg1 and alg2 for this node when this node is part of the 
     * topK of alg1 or alg2
     * 
     * excludedMap: for each node increment by one it's associated value when
     * the node was part of a top K from alg2 but wasn't part of the top K for
     * the same origin node from alg1
     * 
     * includedMap: for each node increment by one it's associated value when
     * the node was part of a top K from alg1 but wasn't part of the top K for
     * the same origin node from alg2
     * @param alg1
     * @param alg2
     * @param nodes
     * @param k
     * @param errMap
     * @param excludedMap
     * @param includedMap
     * @return 
     */
    private static Int2DoubleOpenHashMap errors(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, int k,
            Int2DoubleOpenHashMap errMap, Int2IntOpenHashMap excludedMap,
            Int2IntOpenHashMap includedMap)
    {
        errMap.defaultReturnValue(0d);
        excludedMap.defaultReturnValue(0);
        includedMap.defaultReturnValue(0);
       
        //for each origin node
        for(Integer node: nodes)
        {
            Int2DoubleOpenHashMap map1 = alg1.getMap(node);
            Int2DoubleOpenHashMap map2 = alg2.getMap(node);
            
            //for the selected node get entries for both algos as arrays
            Int2DoubleMap.Entry[] m1 = map1.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            Int2DoubleMap.Entry[] m2 = map2.entrySet().toArray(new Int2DoubleMap.Entry[0]);
           
            int min = Math.min(m1.length, m2.length);
            min = Math.min(min, k);
            
            //in case the first array stores topK results and the second stores topL results with K!=L
            //or if the length is different from k
            if (m1.length != m2.length || m1.length != k)
            {
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
            }
            
            Set<Integer> inTop1 = new HashSet<>(min);
            Set<Integer> inTop2 = new HashSet<>(min);
            //map wich nodes are part of the top K from alg1 and alg2
            for(int i = 0; i < min; i++)
            {
                inTop1.add(m1[i].getIntKey());
                inTop2.add(m2[i].getIntKey());
            }
            
            /*
            for each node in the top K of alg1 and alg2 increment it's error and 
            possibly it's excluded/included values
            */
            for(int i = 0; i < min; i++)
            {
                int target1 = m1[i].getIntKey();
                int target2 = m2[i].getIntKey();
                errMap.addTo(target1, 
                        Math.abs((map1.get(target1) - map2.get(target1)))/map2.get(target1));
                errMap.addTo(target2, 
                        Math.abs((map1.get(target2) - map2.get(target2)))/map2.get(target2));
                //if its in the top of alg1 but not in the top of alg2
                if(!inTop2.contains(target1))
                    includedMap.addTo(target1, 1);
                //if its in the top of alg2 but not in the top of alg1
                if(!inTop1.contains(target2))
                    excludedMap.addTo(target2, 1);
            }
       }
       return errMap;
    }
}
