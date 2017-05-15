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

    