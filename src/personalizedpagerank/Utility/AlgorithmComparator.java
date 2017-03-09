package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
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
    private static final Levenstein<Int2DoubleMap.Entry> LEVENSTEIN = new Levenstein<>();
    private static final PartialSorter<Int2DoubleOpenHashMap.Entry> SORTER = new PartialSorter<>();
     
    /**
     * Given 2 entry arrays containing personalized pagerank scores calculate the
     * jaccard similarity between the sets of nodes that would be the topK
     * scorers for those arrays, if K is the min(K,min(map1.size(),map2.size()))
     * only the top K entries from both arrays are kept, else the top min(map1.size(),
     * map2.size()) entries are kept.
     * The top K scorers for each array are the first K elements.
     * @param m1 Array of entries where keys are nodes and values are scores,
     * ordered descending by values.
     * @param m2 Array of entries where keys are nodes and values are scores,
     * ordered descending by values.
     * @param k Max number of entries to keep from the entries
     * of m1 and m2. (as if the entries were ordered by value
     * descending)
     * @return Jaccard similarity between the two arrays top keys.
     */
    static private double jaccard(Int2DoubleMap.Entry[] m1, Int2DoubleMap.Entry[] m2,
            final int k) 
    {
        Set<Integer> entries1;
        Set<Integer> entries2;

        int min = Math.min(m1.length, m2.length);
        min = Math.min(min, k);

        //create sets and do jaccard
        entries1 = new HashSet<>(min);
        entries2 = new HashSet<>(min);
        
        for (int i = 0; i < min; i++) 
        {
            entries1.add(m1[i].getIntKey());
            entries2.add(m2[i].getIntKey());
        }
        return JACCARD.similarity(entries1, entries2);
    }    
    
    /**
     * Given 2 entry arrays containing personalized pagerank scores calculate the
     * normalised levenstein distance between the sets of nodes that would be the topK
     * scorers for those arrays, if K is the min(K,min(map1.size(),map2.size()))
     * only the top K entries from both arrays are kept, else the top min(map1.size(),
     * map2.size()) entries are kept.
     * The normalisation is done by dividing the distance by 
     * min(K,min(map1.size(),map2.size())).
     * The top K scorers for each array are the first K elements.
     * @param m1 Array of entries where keys are nodes and values are scores,
     * ordered descending by values.
     * @param m2 Array of entries where keys are nodes and values are scores,
     * ordered descending by values.
     * @param k Max number of entries to keep for each node from the entries of m1
     * and m2. (as if the entries were ordered by value descending)
     * @return normalised levenstein distance between the arrays top keys.
     */
    static private double levenstein(Int2DoubleMap.Entry[] m1, Int2DoubleMap.Entry[] m2,
            final int k) 
    {
        //in case the first array stores topK results and the second stores topL results with K!=L
        //or if the length is different from k
        int min = Math.min(m1.length, m2.length);
        min = Math.min(min, k);
        
        if(m1.length != min)
            m1 = Arrays.copyOf(m1, min);
        if(m2.length != min)
            m2 = Arrays.copyOf(m2, min);
        
        return LEVENSTEIN.distance(m1, m2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
        -> {
            return (e1.getIntKey() == e2.getIntKey()) ? 0 : -1;
        })/(double)Math.min(Math.min(m1.length, m2.length), k);
    }        
    
    /**
     * https://en.wikipedia.org/wiki/Spearman's_rank_correlation_coefficient
     * Given 2 arrays containing personalized pagerank entries calculate the
     * spearman's rank correlation coefficient between the nodes 
     * that would be the topK scorers for both maps, if K is the
     * min(K,min(map1.size(),map2.size())), else the top min(map1.size(),
     * map2.size()) entries for each map are kept.
     * The top K scorers for each array are the first K elements.
     * @param m1 Array of entries where keys are nodes and values are scores, 
     * ordered descending by values.
     * @param m2 Array of entries where keys are nodes and values are scores, 
     * ordered descending by values.
     * @param k Max number of entries to keep for each node from the entries
     * of map1 and map2. (as if the entries were ordered by value
     * descending)
     * @return spearman coefficient between the entries
     */
    static private double spearman(Int2DoubleMap.Entry[] m1 , Int2DoubleMap.Entry[] m2,
            Int2DoubleOpenHashMap map2, final int k) 
    {
        //if it's an isolated node just return 1
        if(m1.length == 1 || m2.length == 1 || k == 1)
            return (m1[0].getIntKey() == m2[0].getIntKey())? 1d : 0d;

        int min = Math.min(m1.length, k);

        //if the first k ordered values are the same just return 1
        boolean same = true;
        for(int i = 0; i < min && same; i++)
            same = m1[i].getIntKey() == m2[i].getIntKey();
        if(same)
            return 1;

        //for every node in the topK of alg1 get its score from alg2
        Int2DoubleOpenHashMap kScoresAlg2 = new Int2DoubleOpenHashMap(min);
        Int2DoubleOpenHashMap positionsK2 = new Int2DoubleOpenHashMap(min);          
        for (int i = 0; i < min; i++) 
            kScoresAlg2.put(m1[i].getIntKey(), map2.get(m1[i].getIntKey()));

        //sort the kScoresAlg2 entries by value, descending
        Int2DoubleMap.Entry[] kAlg2 = kScoresAlg2.entrySet().toArray(new Int2DoubleMap.Entry[0]);

        Arrays.sort(kAlg2, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)
                -> {
            return e1.getDoubleValue() < e2.getDoubleValue() ? 1
                    : e1.getDoubleValue() == e2.getDoubleValue() ?
                    (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;         
        });
        //for each entry in kScoresAlg2 associate a node to its position in the order
        for(int i = 0; i < kAlg2.length; i++)
            positionsK2.put(kAlg2[i].getIntKey(), i);

        //prepare input for Pearson.correlation
        double[] ranks1 = new double[min];
        double[] ranks2 = new double[min];

        for(int i = 0; i < min; i++)
        {
            ranks1[i] = i;
            ranks2[i] = positionsK2.get(m1[i].getIntKey());
        }
        return Pearson.correlation(ranks1, ranks2);
    }        
    
    /**
     * Given 2 algorithms compares their personalized pagerank results for a 
     * set of nodes and return data about the min, average, max and standard
     * deviation of the jaccard similarity, normalised levenstein distance and
     * spearman coefficient for the top K scores for each node of the set of
     * nodes, for every K in the Ks array given as input.
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
        
        //min, average, max, std deviation for levenstein
        double[] Lmin = new double[differentKs.length];
        double[] Laverage = new double[differentKs.length];
        double[] Lmax = new double[differentKs.length];
        double[] Lstd = new double[differentKs.length];
        double[] LsquareSum = new double[differentKs.length];
        
        //min, average, max, std deviation for spearman
        double[] Smin = new double[differentKs.length];
        double[] Saverage = new double[differentKs.length];
        double[] Smax = new double[differentKs.length];
        double[] Sstd = new double[differentKs.length];
        double[] SsquareSum = new double[differentKs.length];
        
        Arrays.fill(Jmin, Double.MAX_VALUE);
        Arrays.fill(Jmax, Double.MIN_VALUE);
        Arrays.fill(Javerage, 0d);
        Arrays.fill(JsquareSum, 0d);
        
        Arrays.fill(Lmin, Double.MAX_VALUE);
        Arrays.fill(Lmax, Double.MIN_VALUE);
        Arrays.fill(Laverage, 0d);
        Arrays.fill(LsquareSum, 0d);
        
        Arrays.fill(Smin, Double.MAX_VALUE);
        Arrays.fill(Smax, Double.MIN_VALUE);
        Arrays.fill(Saverage, 0d);
        Arrays.fill(SsquareSum, 0d);

        /*
        for each node get the entries from their personalized pagerank map,
        sort the entries of that map and use the sorted arrays of entries to
        calculate jaccard, levenstein and spearman
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
                
            for(int i = 0; i < differentKs.length; i++)
            {
                double Jtmp = jaccard(m1, m2, differentKs[i]);
                double Ltmp = levenstein(m1, m2, differentKs[i]);
                double Stmp = spearman(m1, m2, map2, differentKs[i]);

                //update min and max for jaccard
                Jmin[i] = Math.min(Jmin[i], Jtmp);
                Jmax[i] = Math.max(Jmax[i], Jtmp);

                Javerage[i] += Jtmp;
                JsquareSum[i] += Jtmp * Jtmp;

                //update min and max for levenstein
                Lmin[i] = Math.min(Lmin[i], Ltmp);
                Lmax[i] = Math.max(Lmax[i], Ltmp);

                Laverage[i] += Ltmp;
                LsquareSum[i] += Ltmp * Ltmp;

                //update min and max for spearman
                Smin[i] = Math.min(Smin[i], Stmp);
                Smax[i] = Math.max(Smax[i], Stmp);

                Saverage[i] += Stmp;
                SsquareSum[i] += Stmp * Stmp;
            }
        }
        
        ComparisonData[] res = new ComparisonData[differentKs.length];
        
        /*
        for each k calculate the std and average for jaccard, levenstein and 
        spearman, then store those results
        */
        for(int i = 0; i < differentKs.length; i++)
        {
            //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) ) for jaccard
            Jstd[i] = Math.sqrt(
                    (JsquareSum[i] - (Javerage[i] * Javerage[i]) / nodes.size()) / nodes.size()
            );
            Javerage[i] /= nodes.size();

            //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) ) for levenstein
            Lstd[i] = Math.sqrt(
                    (LsquareSum[i] - (Laverage[i] * Laverage[i]) / nodes.size()) / nodes.size()
            );
            Laverage[i] /= nodes.size();

            //compute std deviation as sqrt ( 1/n *(squaresum - sum^2/N) ) for spearman
            Sstd[i] = Math.sqrt(
                    (SsquareSum[i] - (Saverage[i] * Saverage[i]) / nodes.size()) / nodes.size()
            );
            Saverage[i] /= nodes.size();


            Result Jr = new Result(Jmin[i], Javerage[i], Jmax[i], Jstd[i]);
            Result Lr = new Result(Lmin[i], Laverage[i], Lmax[i], Lstd[i]);
            Result Sr = new Result(Smin[i], Saverage[i], Smax[i], Sstd[i]);
            res[i] = new ComparisonData(differentKs[i], Jr, Lr, Sr, alg1.getParameters(), alg2.getParameters());
        }
        return res;
    }
    
    /**
     * Given 2 algorithms compares their personalized pagerank results for
     * each node of the "nodes" parameter, returning data about them and their 
     * neighbours, using different K values (see differentKs param).
     * AlgorithmComparator.compare is about general statistics, this method is
     * about getting data for each node.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param differentKs Which Ks will be used in the comparison of the algorithm
     * results, given a K, if a node is mapped to a map containing the scores
     * of more than K nodes only the top K scoring nodes are kept.
     * Each comparison data is a comparison done with a different K.
     * @return Array of data about every origin node, in degree, out degree, pagerank 
     * centrality, jaccard, levenstein, spearman for itself and the neighbour. Every
     * entry used a different K from the differentKs parameter.
     */
    public static NodesComparisonData[] compareOrigins(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, 
            int[] differentKs)
    {
        NodesComparisonData[] res = new NodesComparisonData[differentKs.length];
        for(int i = 0; i < differentKs.length; i++)
        {
            res[i] = compareOrigins(alg1, alg2, nodes, differentKs[i]);
        }
        return res;
    }
    
    /**
     * Given 2 algorithms compares their personalized pagerank results for
     * each node of the "nodes" parameter, returning data about them and their 
     * neighbours.
     * If neighbours of a node are not part of the "nodes" param this means
     * that their personalized pagerank scores have not been computed, so you
     * should ignore the data about neighbour jaccard, neighbour levenstein
     * and neighbour spearman.
     * AlgorithmComparator.compare is about general statistics, this method is
     * about getting data point by point.
     * @param alg1 First algorithm.
     * @param alg2 Second algorithm.
     * @param nodes Set of nodes for which to do a comparison on the results.
     * @param k Given a k, if a node is mapped to a map containing the scores
     * of more than K nodes only the top K scoring nodes are kept.
     * @return Data about every origin node, in degree, out degree, pagerank 
     * centrality, jaccard, levenstein, spearman for itself and the neighbour.
     */
    public static NodesComparisonData compareOrigins(PersonalizedPageRankAlgorithm alg1, 
            PersonalizedPageRankAlgorithm alg2, Set<Integer> nodes, int k)
    {
        DirectedGraph<Integer,DefaultEdge> g = alg1.getGraph(); 
        
        //pagerank value (not personalized pagerank)
        Map<Integer, Double> pagerank = (new PageRank<>(g, alg1.getParameters().getDamping(), 
                alg1.getParameters().getIterations(), 
                alg1.getParameters().getTolerance()).getScores());
        NodesComparisonData res = new NodesComparisonData(k, nodes.size(),
                alg1.getParameters(), alg2.getParameters());
        
        //maps to store jaccard/levenstein/spearman values to avoid calculating them more than once
        Int2DoubleOpenHashMap jMap = new Int2DoubleOpenHashMap(nodes.size());
        Int2DoubleOpenHashMap lMap = new Int2DoubleOpenHashMap(nodes.size());
        Int2DoubleOpenHashMap sMap = new Int2DoubleOpenHashMap(nodes.size());
        jMap.defaultReturnValue(-1);
        lMap.defaultReturnValue(-1);
        sMap.defaultReturnValue(-1);
        
        /*
        map of cumulative personalized pagerank error for each node, for every
        node in the top K of alg1 and for every node in the top K of alg2
        the double value mapped to that node is incremented by the absolute
        difference between the personalized pagerank score assigned by alg1
        and alg2 divided by the score assigned by alg2,
        this is repeated for every origin node (node part of the "nodes"
        set parameter)
        */
        Int2DoubleOpenHashMap errorMap = new Int2DoubleOpenHashMap(g.vertexSet().size());
        
        /**
         * for every node in the top K of alg2 that is not in the top K of alg1
         * increment the value associated to that node by 1
         * this is repeated
         * for every origin node (node part of the "nodes" set parameter)
         */
        Int2IntOpenHashMap excludedMap = new Int2IntOpenHashMap(g.vertexSet().size());
        
        /**
         * for every node in the top K of alg1 that is not in the top K of alg2
         * increment the value associated to that node by 1
         * this is repeated for every origin node (node part of the "nodes"
         * set parameter)
         */
        Int2IntOpenHashMap includedMap = new Int2IntOpenHashMap(g.vertexSet().size());
        errors(alg1, alg2, nodes, k, errorMap, excludedMap, includedMap);

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
            
            jMap.put(node.intValue(), jaccard(m1, m2, k));
            lMap.put(node.intValue(),levenstein(m1, m2, k));
            sMap.put(node.intValue(), spearman(m1, m2, map2, k));
            
            res.setId(index, node);
            res.setIndegree(index, g.inDegreeOf(node));
            res.setOutdegree(index, g.outDegreeOf(node));
            res.setPagerank(index, pagerank.get(node));
            res.setJaccard(index, jMap.get(node.intValue()));
            res.setLevenstein(index, lMap.get(node.intValue()));
            res.setSpearman(index, sMap.get(node.intValue()));
            res.setPagerankError(index, errorMap.get(node.intValue()));
            res.setExcluded(index, excludedMap.get(node.intValue()));
            res.setIncluded(index, includedMap.get(node.intValue()));
            index++;
        }
        
        //set stats for nodes that required neighbour information
        index = 0;
        for(Integer node: nodes)
        {
            boolean skipNeighbourhood = false;
            double in = 0;//in degree
            double out = 0;//out degree
            double pr = 0;//page rank
            double j = 0;//jaccard
            double l = 0;//levenstein
            double s = 0;//spearman
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
                j += jMap.get(neighbour);
                l += lMap.get(neighbour);
                s += sMap.get(neighbour);
                e += errorMap.get(neighbour);
                ex += excludedMap.get(neighbour);
                inc += includedMap.get(neighbour);

                //only need to check one of the maps to check if the neighbour
                //is not part of the nodes for which personalized pagerank scores
                //have been calculated
                skipNeighbourhood = skipNeighbourhood || jMap.get(neighbour) == -1;
            }
            
            //children nodes
            for(DefaultEdge edge: g.outgoingEdgesOf(node))
            {
                neighbourHood++;
                int neighbour = Graphs.getOppositeVertex(g, edge, node);
                in += g.inDegreeOf(neighbour);
                out += g.outDegreeOf(neighbour);
                pr += pagerank.get(neighbour);
                j += jMap.get(neighbour);
                l += lMap.get(neighbour);
                s += sMap.get(neighbour);
                e += errorMap.get(neighbour);
                ex += excludedMap.get(neighbour);
                inc += includedMap.get(neighbour);
                
                //only need to check one of the maps to check if the neighbour
                //is not part of the nodes for which personalized pagerank scores
                //have been calculated
                skipNeighbourhood = skipNeighbourhood || jMap.get(neighbour) == -1;
            }
            
            
            if(neighbourHood > 0)
            {
                in /= neighbourHood;
                out /= neighbourHood;
                pr /= neighbourHood;
                j /= neighbourHood;
                l /= neighbourHood;
                s /= neighbourHood;
                e /= neighbourHood;
                ex /= neighbourHood;
                inc /= neighbourHood;
            }
            //if neighbourhood jaccard/levenstein/spearman/error data has no value flag it
            if(skipNeighbourhood)
                j = l = s = e = -1d;
            
            res.setNeighbourIn(index, in);
            res.setNeighbourOut(index, out);
            res.setNeighbourPagerank(index, pr);
            res.setNeighbourJaccard(index, j);
            res.setNeighbourLevenstein(index, l);
            res.setNeighbourSpearman(index, s);
            res.setNeighbourPagerankError(index, e);
            res.setNeighbourExcluded(index, ex);
            res.setNeighbourIncluded(index, inc);
            
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
