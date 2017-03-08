package personalizedpagerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.GuerrieriRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Algorithms.WrappedOnlinePageRank;
import personalizedpagerank.Algorithms.WrappedStoringPageRank;
import personalizedpagerank.Utility.ComparisonData;
import personalizedpagerank.Utility.AlgorithmComparator;
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
            //generateUndirectedBipartite(g,1000,1000, 1000 * 1000);
            importBipartiteUndirectedFromCsv(g, "data/graphs/undirected/bipartite/collab.csv");
            //importGraphFromCsv(g, "data/graphs/directed/p2p-Gnutella04.csv");
            //printGraph(g, "g1.csv");
            
            System.out.println("finished importing ");
            
            WrappedStoringPageRank pr = new WrappedStoringPageRank(g, 100, 0.85, 0.0001, g.vertexSet().size());
            System.out.println("done prank");
            
            /*
            ArrayList<ComparisonData> data = new ArrayList<>();
            for(int i = 1; i <= 500; i++)
            {
                int[] ks = new int[i];
                for(int u = 1; u <= i; u++ )
                    ks[u-1] = u;
                PersonalizedPageRankAlgorithm grank = new GuerrieriRank(g, i, i, 50, 0.85, 0.0001);
                data.addAll(new ArrayList<>
                    (Arrays.asList(AlgorithmComparator.compare(grank, pr, pr.getNodes(), ks))));
                System.out.println(i);
            }
       
           IOclass.writeCsv("data.csv", data.toArray(new ComparisonData[0]));
           */
            
            
            int[] ks = {100};
            PersonalizedPageRankAlgorithm grank = new GuerrieriRank(g, 100, 100, 50, 0.85, 0.0001);
            System.out.println("done grank");
            NodesComparisonData[] data = AlgorithmComparator.compareOrigins(grank, pr, pr.getNodes(), ks);
            IOclass.writeCsv("data.csv", data);
            
            //NodesComparisonData[] originData = AlgorithmComparator.compareOrigins(res1, res2, res2.getNodes(), ks);
            
        }

        ////////////////
        //////////the code written below here is just some stuff written hastily to try out stuff
        ///not part of the project, not commented, not tested
        /////////////////////////////////////////////////////////////////////////////////////
        
        
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

           //make it  undirected
           gen.generateGraph(g, new factory(), null);
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

    