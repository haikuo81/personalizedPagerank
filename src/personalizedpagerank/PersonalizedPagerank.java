package personalizedpagerank;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.BoundaryRestrictedPageRank;
import personalizedpagerank.Algorithms.GuerrieriRank;
import personalizedpagerank.Algorithms.GuerrieriRankV2;
import personalizedpagerank.Algorithms.GuerrieriRankV3;
import personalizedpagerank.Algorithms.GuerrieriRankV3;
import personalizedpagerank.Algorithms.MCCompletePathPageRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Algorithms.WrappedOnlinePageRank;
import personalizedpagerank.Algorithms.WrappedStoringPageRank;
import personalizedpagerank.Utility.AlgorithmComparator;
import personalizedpagerank.Utility.ComparisonData;
import personalizedpagerank.Utility.IOclass;
import personalizedpagerank.Utility.NodesComparisonData;

    //indegree, outdegree, pagerankscore, neighbour out/in degree, neighbour pr
    //su quali nodi (ogni altro nodo come origine) si accumula + errore?
    //fare classe csv writer
    public class PersonalizedPagerank 
    {

        public static void main(String[] args) 
        {
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            //generateUndirectedBipartite(g,100, 100, 5000);
            importBipartiteUndirectedFromCsv(g, "wikiElec.csv");
            //importGraphFromCsv(g, "wikiElec.csv");
            //printGraph(g, "g1.csv");
            System.out.println("finished importing ");
           
            /*
            while(true)
            {
                            System.out.println("-----------");

            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 0.85, 0.0001, 200);
                System.out.println("done pr");
            int[] ks = {100};
          
            
            
            PersonalizedPageRankAlgorithm mcrank4 = new GuerrieriRankV3(g, 100, 200, 30, 0.85, 0.0001);
            
            ComparisonData[] data4 = AlgorithmComparator.compare(mcrank4, pr, pr.getNodes(), ks);
            System.out.println(data4[0].getJaccard().getAverage());
            System.out.println(data4[0].getJaccard().getMin());
            System.out.println(data4[0].getJaccard().getStd());
            System.out.println(data4[0].getKendall().getAverage());
            
            mcrank4 = new GuerrieriRankV2(g, 100, 200, 30, 0.85, 0.0001);
            data4 = AlgorithmComparator.compare(mcrank4, pr, pr.getNodes(), ks);
            System.out.println(data4[0].getJaccard().getAverage());
            System.out.println(data4[0].getJaccard().getMin());
            System.out.println(data4[0].getJaccard().getStd());
            System.out.println(data4[0].getKendall().getAverage());
            }
            */
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 100, 0.85, 0.0001, g.vertexSet().size());
            int[] differentKs = {50};
            PersonalizedPageRankAlgorithm grank = new GuerrieriRankV3(g, 50, 100, 100, 0.85, 0.0001);
            NodesComparisonData[] data = AlgorithmComparator.compareOrigins(grank, pr, pr.getNodes(), differentKs);
            IOclass.writeCsv("wikiEleckNodesGV3.csv", data);
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

        public static class factory implements VertexFactory<Integer>
        {
            int val = 0;
            public Integer createVertex()
            {
                return val++;
            }
        }
}

    