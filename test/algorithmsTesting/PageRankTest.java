/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: this is a modified version of the original file from Dimitrios Michail and Contributors.
 * Tests for weighted graphs have been removed while some new tests have been added.
 * 
 * 
 */
package algorithmsTesting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import algorithms.*;

import junit.framework.*;
import org.jgrapht.DirectedGraph;

/**
 * Unit tests for PageRank
 * 
 * @author Dimitrios Michail
 */
public class PageRankTest
    extends TestCase
{

    public void testGraph2Nodes()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2");
        g.addEdge("2", "1");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("2"), 0.0001);
    }

    public void testGraph3Nodes()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addEdge("1", "2");
        g.addEdge("2", "3");
        g.addEdge("3", "1");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("2"), 0.0001);
        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("3"), 0.0001);
    }

    public void testGraphWikipedia()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");

        g.addEdge("B", "C");
        g.addEdge("C", "B");
        g.addEdge("D", "A");
        g.addEdge("D", "B");
        g.addEdge("E", "D");
        g.addEdge("E", "B");
        g.addEdge("E", "F");
        g.addEdge("F", "B");
        g.addEdge("F", "E");
        g.addEdge("1", "B");
        g.addEdge("1", "E");
        g.addEdge("2", "B");
        g.addEdge("2", "E");
        g.addEdge("3", "B");
        g.addEdge("3", "E");
        g.addEdge("4", "E");
        g.addEdge("5", "E");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("A"), 0.03278, 0.0001);
        assertEquals(pr.getVertexScore("B"), 0.38435, 0.0001);
        assertEquals(pr.getVertexScore("C"), 0.34295, 0.0001);
        assertEquals(pr.getVertexScore("D"), 0.03908, 0.0001);
        assertEquals(pr.getVertexScore("E"), 0.08088, 0.0001);
        assertEquals(pr.getVertexScore("F"), 0.03908, 0.0001);
        assertEquals(pr.getVertexScore("1"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("2"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("3"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("4"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("5"), 0.01616, 0.0001);
    }

    public void testUndirectedGraphWikipedia()
    {
        Pseudograph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");

        g.addEdge("B", "C");
        g.addEdge("C", "B");
        g.addEdge("D", "A");
        g.addEdge("D", "B");
        g.addEdge("E", "D");
        g.addEdge("E", "B");
        g.addEdge("E", "F");
        g.addEdge("F", "B");
        g.addEdge("F", "E");
        g.addEdge("1", "B");
        g.addEdge("1", "E");
        g.addEdge("2", "B");
        g.addEdge("2", "E");
        g.addEdge("3", "B");
        g.addEdge("3", "E");
        g.addEdge("4", "E");
        g.addEdge("5", "E");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("A"), 0.0404, 0.0001);
        assertEquals(pr.getVertexScore("B"), 0.2152, 0.0001);
        assertEquals(pr.getVertexScore("C"), 0.0593, 0.0001);
        assertEquals(pr.getVertexScore("D"), 0.0945, 0.0001);
        assertEquals(pr.getVertexScore("E"), 0.2511, 0.0001);
        assertEquals(pr.getVertexScore("F"), 0.0839, 0.0001);
        assertEquals(pr.getVertexScore("1"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("2"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("3"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("4"), 0.0373, 0.0001);
        assertEquals(pr.getVertexScore("5"), 0.0373, 0.0001);
    }

    public void testUnweightedGraph1()
    {
        DirectedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");

        g.setEdgeWeight(g.addEdge("center", "a"), 1.0);
        g.setEdgeWeight(g.addEdge("center", "b"), 1.0);
        g.setEdgeWeight(g.addEdge("center", "c"), 1.0);

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 0.2061, 0.0001);
        assertEquals(pr.getVertexScore("a"), 0.2646, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.2646, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.2646, 0.0001);

        // for (String v : g.vertexSet()) {
        // System.out.println("pagerank(" + v + ") = " + pr.getVertexScore(v));
        // }
    }

    public void testUnweightedGraph2()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("center", "a");
        g.addEdge("center", "b");
        g.addEdge("center", "c");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 0.1709, 0.0001);
        assertEquals(pr.getVertexScore("a"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("d"), 0.1709, 0.0001);

        // for (String v : g.vertexSet()) {
        // System.out.println("pagerank(" + v + ") = " + pr.getVertexScore(v));
        // }
    }

    public void testEmptyGraph()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertTrue(pr.getScores().isEmpty());
    }

    public void testNonExistantVertex()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("center", "a");
        g.addEdge("center", "b");
        g.addEdge("center", "c");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        try {
            pr.getVertexScore("unknown");
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testBadParameters()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        try {
            new PageRank<>(g, 1.1, 100, 0.0001);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

        try {
            new PageRank<>(g, 0.85, 0, 0.0001);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

        try {
            new PageRank<>(g, 0.85, 100, 0.0);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

    }
    
    //personalized pagerank tests
    public void testNoNExistantOriginPPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("b", "a");
        g.addEdge("b", "b");
        g.addEdge("a", "c");
        g.addEdge("c","a");
        g.addEdge("d", "a");
        g.addEdge("a", "d");
        g.addEdge("a", "b");

        try
        {
            VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"origin");
            fail("this line shouldn't be reached");
        }
        catch(IllegalArgumentException e)
        {
        }
    }

    public void testSingleNodeGraphPPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        assertEquals(pr.getVertexScore("a"), 0.15, 0.0001);
    }
    
    public void testNoEdgesGraphPPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        assertEquals(pr.getVertexScore("a"), 0.15, 0.0001);
    }
    
    public void testOriginShouldBeHighestScore1PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        
        //a is isolated, b,c,d are a clique
        g.addEdge("b","c");
        g.addEdge("b","d");
        g.addEdge("c","b");
        g.addEdge("c","d");
        g.addEdge("d","b");
        g.addEdge("d","c");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85d, 100, 0.0001,"a");
        assertEquals(pr.getVertexScore("a"), 0.15, 0.0001);
        assertEquals(pr.getVertexScore("b"),0,0);
        assertEquals(pr.getVertexScore("c"),0,0);
        assertEquals(pr.getVertexScore("d"),0,0);
    }
    
    public void testOriginShouldBeHighestScore2PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        
        //a clique
        g.addEdge("a","b");
        g.addEdge("a","c");
        g.addEdge("b","a");
        g.addEdge("b","c");
        g.addEdge("c","a");
        g.addEdge("c","b");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        
        Map<String,Double> scores = sortByComparator(pr.getScores(),false);
        assertTrue(scores.keySet().toArray()[0] == "a");
    }
    
    public void testOriginShouldBeHighestScore3PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        
        g.addEdge("a","b");
        g.addEdge("a","c");
        g.addEdge("b","c");
        g.addEdge("c","b");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"b");
        
        Map<String,Double> scores = sortByComparator(pr.getScores(),false);
        assertTrue(scores.keySet().toArray()[0] == "b");
    }
    
    public void testOriginShouldBeHighestScore4PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        
        g.addEdge("a","b");
        g.addEdge("b","c");
        g.addEdge("c","d");
        g.addEdge("d","e");
        g.addEdge("e","f");
        g.addEdge("f","a");
        
        VertexScoringAlgorithm<String, Double> pr1 = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        Map<String,Double> scores1 = sortByComparator(pr1.getScores(),false);
        assertTrue(scores1.keySet().toArray()[0] == "a");
        
        VertexScoringAlgorithm<String, Double> pr2 = new PageRank<>(g, 0.85, 100, 0.0001,"b");
        Map<String,Double> scores2 = sortByComparator(pr2.getScores(),false);
        assertTrue(scores2.keySet().toArray()[0] == "b");
        
        VertexScoringAlgorithm<String, Double> pr3 = new PageRank<>(g, 0.85, 100, 0.0001,"c");
        Map<String,Double> scores3 = sortByComparator(pr3.getScores(),false);
        assertTrue(scores3.keySet().toArray()[0] == "c");
        
        VertexScoringAlgorithm<String, Double> pr4 = new PageRank<>(g, 0.85, 100, 0.0001,"d");
        Map<String,Double> scores4 = sortByComparator(pr4.getScores(),false);
        assertTrue(scores4.keySet().toArray()[0] == "d");
        
        VertexScoringAlgorithm<String, Double> pr5= new PageRank<>(g, 0.85, 100, 0.0001,"e");
        Map<String,Double> scores5 = sortByComparator(pr5.getScores(),false);
        assertTrue(scores5.keySet().toArray()[0] == "e");
        
        VertexScoringAlgorithm<String, Double> pr6= new PageRank<>(g, 0.85, 100, 0.0001,"f");
        Map<String,Double> scores6 = sortByComparator(pr6.getScores(),false);
        assertTrue(scores6.keySet().toArray()[0] == "f");
        
    }
    
    public void testOrderedScoreCorrect1PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        
        g.addEdge("a","b");
        g.addEdge("a","c");
        g.addEdge("b","d");
        g.addEdge("c","d");
        g.addEdge("d","a");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        Map<String,Double> scores = sortByComparator(pr.getScores(),false);
        
        assertTrue(scores.keySet().toArray()[0] == "a");
        assertTrue(scores.keySet().toArray()[1] == "d");
    }
    
    public void testOrderedScoreCorrect2PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        
        g.addEdge("a","b");
        g.addEdge("a","c");
        g.addEdge("a","d");
        g.addEdge("a","e");
        g.addEdge("a","f");
        g.addEdge("a","g");
        
        g.addEdge("b","h");
        g.addEdge("c","h");
        g.addEdge("d","h");
        g.addEdge("e","h");
        g.addEdge("f","h");
        g.addEdge("g","h");
        
        g.addEdge("h","a");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        Map<String,Double> scores = sortByComparator(pr.getScores(),false);
        
        assertTrue(scores.keySet().toArray()[0] == "a");
        assertTrue(scores.keySet().toArray()[1] == "h");
        
        pr = new PageRank<>(g, 0.85, 100, 0.0001,"h");
        scores = sortByComparator(pr.getScores(),false);
        
        assertTrue(scores.keySet().toArray()[0] == "h");
        assertTrue(scores.keySet().toArray()[1] == "a");
    }
    
    public void testOrderedScoreCorrect3PPR()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        
        g.addEdge("a","b");
        g.addEdge("a","c");
        g.addEdge("a","e");
        
        g.addEdge("b","d");
        g.addEdge("c","d");
        g.addEdge("d","f");
        g.addEdge("e","f");
        g.addEdge("f","a");
        
        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001,"a");
        Map<String,Double> scores = sortByComparator(pr.getScores(),false);
        
        assertTrue(scores.keySet().toArray()[0] == "a");
        assertTrue(scores.keySet().toArray()[1] == "f");
        assertTrue(scores.keySet().toArray()[2] == "d");
        
        pr = new PageRank<>(g, 0.85, 1000, 0.0001,"e");
        scores = sortByComparator(pr.getScores(),false);
        
        assertTrue(scores.keySet().toArray()[0] == "f");
        assertTrue(scores.keySet().toArray()[1] == "a");
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
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        //order the entries with the comparator
        Collections.sort(list, (Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) ->
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
        for (Map.Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}

// End PageRankTest.java
