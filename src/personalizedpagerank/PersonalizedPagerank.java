package personalizedpagerank;

import algorithms.BoundaryRestrictedPageRank;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import algorithms.GuerrieriRank;
import algorithms.GuerrieriRankV3;
import algorithms.MCCompletePathPageRank;
import algorithms.MCCompletePathPageRankV2;
import algorithms.PersonalizedPageRankAlgorithm;
import algorithms.WrappedStoringPageRank;
import benchmarking.AlgorithmComparator;
import benchmarking.ComparisonData;
import io.DbManager;
import org.jgrapht.alg.ConnectivityInspector;
import utility.NodeScores;

    //indegree, outdegree, pagerankscore, neighbour out/in degree, neighbour pr
    //su quali nodi (ogni altro nodo come origine) si accumula + errore?
    //fare classe csv writer
    public class PersonalizedPagerank 
    {

        public static void main(String[] args) 
        {
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            //importBipartiteUndirectedFromCsv(g, "wikiElec.csv");
            importGraphFromCsv(g, "eat.csv");
            
            System.out.println("finished importing ");
            System.out.println();
            System.out.println("---------------------- " + g.vertexSet().size() 
            + " vertices and " + g.edgeSet().size() + " edges");
            
            PersonalizedPageRankAlgorithm alg;
            ComparisonData[] data;
            
            long time = System.currentTimeMillis();
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 50, 100, 0.85, 0.0001, 200);
            System.out.println(pr.getNodes().size());
            time = System.currentTimeMillis() - time;
            System.out.println("done pr in " + time);
            int[] ks = {50};
            
            int done = 0;
            DbManager db = new DbManager();
            
            String graph = "gnutella04";
            //db.insertAlgorithm("guerrierirank", 4);
            //db.insertGraph(graph, g.vertexSet().size(), g.edgeSet().size(), true, false);
         
            /*
            for(int l = 750; l <= 1250; l += 100)
                for(int ite = 5; ite <= 40; ite+= 10)
                    for(double tole = 0.0001; tole < 0.05; tole += 0.009)
                    {
                        data = null;
                        alg = null;
                        System.gc();
                        time = System.currentTimeMillis();
                        alg = new GuerrieriRank(g, 50, l, ite, 0.85, tole);
                        time = System.currentTimeMillis() - time; 
                        System.out.println("done Gv in " + time + " ms");
                        data = AlgorithmComparator.compare(alg, pr, pr.getNodes(), ks);
                        System.out.println(data[0].getJaccard().getAverage() + " jaccard average");
                        System.out.println(data[0].getJaccard().getMin() + " jaccard min");
                        System.out.println(data[0].getJaccard().getStd() + " jaccard std");
                        System.out.println(data[0].getKendall().getAverage() + " kendall average");
                        double[] params = new double[4];
                        params[0] = l; params[1] = ite; params[2] = 0.85; params[3] = tole;
                        done--;
                        System.out.println("done " + done);
                        System.out.println(l + " " + ite +" " + tole);
                        db.insertRun(graph, "guerrierirank", "i5-4690", 800, params, data[0], (int) time);
                    }
            */
            /*
            done = 0;
            //db.insertAlgorithm("guerrierirankV3", 4);
            for(int l = 750; l <= 1250; l += 100)
                for(int ite = 5; ite <= 40; ite+= 10)
                    for(double tole = 0.0001; tole < 0.05; tole += 0.009)
                    {
                        data = null;
                        alg = null;
                        System.gc();
                        time = System.currentTimeMillis();
                        alg = new GuerrieriRankV3(g, 50, l, ite, 0.85, tole);
                        time = System.currentTimeMillis() - time; 
                        System.out.println("done Gv3 in " + time + " ms");
                        data = AlgorithmComparator.compare(alg, pr, pr.getNodes(), ks);
                        System.out.println(data[0].getJaccard().getAverage() + " jaccard average");
                        System.out.println(data[0].getJaccard().getMin() + " jaccard min");
                        System.out.println(data[0].getJaccard().getStd() + " jaccard std");
                        System.out.println(data[0].getKendall().getAverage() + " kendall average");
                        double[] params = new double[4];
                        params[0] = l; params[1] = ite; params[2] = 0.85; params[3] = tole;
                        done--;
                        System.out.println("done " + done);
                        System.out.println(l + " " + ite +" " + tole);
                        db.insertRun(graph, "guerrierirankV3", "i5-4690", 800, params, data[0], (int) time);
                    }
            */
            /*
            done = 0;
            //db.insertAlgorithm("mccompletepathV2", 3);
            for(int l = 2100; l <= 4000; l += 100)
                for(int ite = 50; ite < 3000; ite+= 350)
                    {
                        data = null;
                        alg = null;
                        System.gc();
                        time = System.currentTimeMillis();
                        alg = new MCCompletePathPageRankV2(g, l, ite, 0.85);
                        time = System.currentTimeMillis() - time; 
                        System.out.println("done mc2 in " + time + " ms");
                        data = AlgorithmComparator.compare(alg, pr, pr.getNodes(), ks);
                        System.out.println(data[0].getJaccard().getAverage() + " jaccard average");
                        System.out.println(data[0].getJaccard().getMin() + " jaccard min");
                        System.out.println(data[0].getJaccard().getStd() + " jaccard std");
                        System.out.println(data[0].getKendall().getAverage() + " kendall average");
                        double[] params = new double[3];
                        params[0] = l; params[1] = ite; params[2] = 0.85; 
                        done--;
                        System.out.println("done " + done);
                        System.out.println(l + " " + ite);
                        db.insertRun(graph, "mccompletepathV2", "i5-4690", 800, params, data[0], (int) time);
                    }
            */
            /*
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 100, 0.85, 0.0001, g.vertexSet().size());
            int[] differentKs = {50};
            PersonalizedPageRankAlgorithm grank = new GuerrieriRankV3(g, 50, 100, 100, 0.85, 0.0001);
            NodesComparisonData[] data = AlgorithmComparator.compareOrigins(grank, pr, pr.getNodes(), differentKs);
            IOclass.writeCsv("wikiEleckNodesGV3.csv", data);
            */
        }
        
       
        ////////////////
        //////////the code written below here is just some stuff written hastily to try out stuff
        ///not part of the project, not commented, not tested
        /////////////////////////////////////////////////////////////////////////////////////
        
        private static Set<Integer> nodesSubset(Graph<Integer, DefaultEdge> g, int k)
        {
            Set<Integer> res = new HashSet<>(k);
            ArrayList<Integer> nodes = new ArrayList<>(g.vertexSet());
            Collections.shuffle(nodes);
            for(int i = 0; i < k; i++)
                res.add(nodes.get(i));
            return res;
        }
        
        private static void printGraph(DirectedGraph<Integer, DefaultEdge> g, final String path)
        {
            PrintWriter writer = null;
            try
            {
                writer = new PrintWriter(new File(path));
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
            StringBuilder builder = new StringBuilder();
            for(Integer v: g.vertexSet())
            {
                for(DefaultEdge e: g.outgoingEdgesOf(v))
                {
                    Integer other = Graphs.getOppositeVertex(g, e, v);      
                    builder.append(v).append(",").append(other).append(System.getProperty("line.separator"));
                }
            }
            writer.write(builder.toString());
            writer.close();
        }

        /**
         * Given a graph and a csv file where every line contains 2 vertex ids representing
         * an edge in the form:
         * 1,2
         * 4,5
         * 7,2
         * those edges gets added to the graph, vertices gets added aswell.
         * 
         * @param g Graph to insert nodes and edges into.
         * @param csvPath Path too file.
         */
        public static void importGraphFromCsv (Graph<Integer, DefaultEdge> g, String csvPath)
        {
            try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) 
            {
                String line;
                String splitter = ",";
                while ((line = br.readLine()) != null) 
                {
                    //split using splitter
                    String[] edge = line.split(splitter);
                    Integer v1 = Integer.parseInt(edge[0]);
                    Integer v2 = Integer.parseInt(edge[1]);
                    if(!g.containsVertex(v1))
                        g.addVertex(v1);
                    if(!g.containsVertex(v2))
                        g.addVertex(v2);
                    g.addEdge(v1,v2);
                }
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }

        public static void importBipartiteUndirectedFromCsv(Graph<Integer, DefaultEdge> g, String csvPath)
        {
            Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
            Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
            int count = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) 
            {
                String line;
                String splitter = ",";
                while ((line = br.readLine()) != null) 
                {
                    //split using splitter
                    String[] edge = line.split(splitter);
                    Integer v1 = Integer.parseInt(edge[0]);
                    Integer v2 = Integer.parseInt(edge[1]);
                    if(map1.get(v1) == null)
                    {
                        map1.put(v1, count++);
                    }
                    v1 = map1.get(v1);
                    if(map2.get(v2) == null)
                    {
                        map2.put(v2, count++);
                    }
                    v2 = map2.get(v2);
                    if(!g.containsVertex(v1))
                        g.addVertex(v1);
                    if(!g.containsVertex(v2))
                        g.addVertex(v2);
                    g.addEdge(v1,v2);
                    g.addEdge(v2,v1);
                }
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }




        public static void generateUndirectedBipartite(DirectedGraph<Integer, DefaultEdge> g, int n1, int n2, int m)
        {
            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> gen = new GnmRandomBipartiteGraphGenerator<>(n1, n2, m); 
            gen.generateGraph(g, new factory(), null);
            //make it  undirected
            for(Integer i: g.vertexSet())
               for(DefaultEdge e: g.outgoingEdgesOf(i))
                   g.addEdge(Graphs.getOppositeVertex(g, e, i), i);
        }
        
        public static void generateDirectedGraph(DirectedGraph<Integer, DefaultEdge> g, int n, double p)
        {
            GnpRandomGraphGenerator<Integer, DefaultEdge> gen = new GnpRandomGraphGenerator<>(n, p, new Random(), true);
            gen.generateGraph(g, new factory(), null);
        }

        public static class factory implements VertexFactory<Integer>
        {
            int val = 0;
            public Integer createVertex()
            {
                return val++;
            }
        }
        
        public int getsTo(DirectedGraph<Integer, DefaultEdge> g, int node)
        {
            int res = 0;
            List<Integer> queue = new ArrayList<>();
            NodeScores visited = new NodeScores();
            queue.add(node);
            visited.put(node,1);
            while(!queue.isEmpty())
            {
                int next = queue.remove(0);
                for(int successor: Graphs.successorListOf(g, next))
                    if(!visited.containsKey(successor))
                    {
                        queue.add(successor);
                        visited.put(successor, 1);
                        res++;
                    }
                
                
            }
            return res;
        }

}

    