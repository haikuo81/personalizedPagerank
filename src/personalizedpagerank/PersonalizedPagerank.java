package personalizedpagerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import personalizedpagerank.Algorithms.GuerrieriRank;
import personalizedpagerank.Algorithms.PageRank;
import personalizedpagerank.Algorithms.WrappedPageRank;
import personalizedpagerank.Utility.ResultComparator;

    /**
     *
     * @author jacopo
     */
    public class PersonalizedPagerank {

        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) {
            DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
            ResultComparator<Integer, Double> comp = new ResultComparator();
            importGraphFromCsv(g, "../data/graphs/directed/p2p-Gnutella04.csv");
            System.out.println("finished importing ");
            
            WrappedPageRank res2 = new WrappedPageRank(g, 100, 0.85, 0.0001, 100);
            System.out.println("done prank");
            
            PersonalizedPageRankAlgorithm res1 = new GuerrieriRank(g, 30, 30, 100, 0.85);
            System.out.println("done grank");
            
            System.out.println(comp.levenstein(res1, res2, (Integer) res2.getNodes().toArray()[0]));
            
            
            /*
            double[] res = comp.jaccard(res1, res2, res2.getNodes());
            System.out.println(res[0]);
            System.out.println(res[1]);
            System.out.println(res[2]);
            System.out.println(res[3]);
            
            //
            res1 = new GuerrieriRank(g, 30, 50, 100, 0.85);
            res = comp.jaccard(res1, res2, res2.getNodes());
            System.out.println(res[0]);
            System.out.println(res[1]);
            System.out.println(res[2]);
            System.out.println(res[3]);
            */
        }

    /**
     * Sorts a map keys based on values, returning a new map.
     * @param unsortMap Map to sort based on values.
     * @param order True for ascending, false for descending.
     * @return A sorted by values Map.
     */
    private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order)
    {
        //trasform the map in a list of entries
        List<Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        //order the entries with the comparator
        Collections.sort(list, (Entry<String, Double> e1, Entry<String, Double> e2) ->
        {
            //order = true -> ascending
            if (order)
            {
                return e1.getValue().compareTo(e2.getValue());
            }
            else
            {
                return e2.getValue().compareTo(e1.getValue());
                
            }
        });

        
        //insert ordered entries (and keep order) thanks to linked hash map
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
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

}
