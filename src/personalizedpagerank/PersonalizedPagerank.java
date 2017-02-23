package personalizedpagerank;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.GuerrieriRank;
import personalizedpagerank.Algorithms.PersonalizedPageRankAlgorithm;
import personalizedpagerank.Algorithms.WrappedPageRank;
import personalizedpagerank.Utility.ComparisonData;
import personalizedpagerank.Utility.ResultComparator;

    public class PersonalizedPagerank {

        public static void main(String[] args) 
        {
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
            generateUndirectedBipartite(g,500,3500,60000);
            ResultComparator comp = new ResultComparator();
            //importBipartiteUndirectedFromCsv(g, "data/graphs/undirected/bipartite/wikiElec.csv");
            //printGraph(g, "g1.csv");
            System.out.println("finished importing ");
            
            WrappedPageRank res2 = new WrappedPageRank(g, 100, 0.85, 0.0001, 800);
            System.out.println("done prank");
            
            int iterations = 90;
            ComparisonData[] data = new ComparisonData[iterations + 1];
            for(int i = 0; i <= iterations; i++)
            {
                PersonalizedPageRankAlgorithm res1 = new GuerrieriRank(g, 50, 50 + i * 5, 100, 0.85, 0.0001);
                data[i] = comp.compare(res1, res2, res2.getNodes());
                System.out.println(i);
                System.gc();
            }
            System.out.println("done grank");
            ComparisonData.writeCsv("genK50.csv", data);
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
                    if(!g.containsVertex(v1))
                        g.addVertex(v1);
                    if(!g.containsVertex(v2))
                        g.addVertex(v2);
                    if(g.containsEdge(v1, v2))
                    {
                       System.out.println(v1 + "," + v2);
                        count ++;
                    }
                    g.addEdge(v1,v2);
                    //g.addEdge(v2,v1);

                }

            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
            System.out.println(count);
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
            System.out.println(count);
        }




        public static void generateUndirectedBipartite(DirectedGraph<Integer, DefaultEdge> g, int n1, int n2, int m)
        {
           GnmRandomBipartiteGraphGenerator gen = new GnmRandomBipartiteGraphGenerator(n1, n2, m); 

           //make it  undirected
           gen.generateGraph(g, new factory(), null);
            for(Integer i: g.vertexSet())
               for(DefaultEdge e: g.outgoingEdgesOf(i))
                   g.addEdge(Graphs.getOppositeVertex(g, e, i), i);
        }

        public static class factory implements VertexFactory
        {
            int val = 0;
            public Integer createVertex()
            {
                return val++;
            }
        }
}

    