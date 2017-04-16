/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package UtilityTesting;

import java.util.ArrayList;
import java.util.Collections;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import personalizedpagerank.Utility.NodeScores;

public class NodeScoresTest extends TestCase
{
    public void testConstructor()
    {
        NodeScores scores;
        NodeScores tmp = new NodeScores();
        tmp.addTo(0, 1.0);
        tmp.addTo(1, 2.0);
        
        scores = new NodeScores(tmp);
        assertEquals(scores.size(), 2);
        scores.remove(1);
        assertEquals(scores.size(), 1);
        scores.remove(0);
        assertEquals(scores.size(), 0);
        scores = new NodeScores(tmp);
        scores.clear();
        assertEquals(scores.size(), 0);
    }

    public void testTop0FromEmpty()
    {
        NodeScores scores = new NodeScores();
        scores.keepTop(0);
        assertEquals(scores.size(), 0);
    }
    
    public void testTop0()
    {
        NodeScores scores = new NodeScores();
        scores.addTo(1,1);
        scores.addTo(2,2);
        scores.keepTop(0);
        assertEquals(scores.size(), 0);
    }
    
    public void testTopN()
    {
        NodeScores scores = new NodeScores();
        int n = 500;
        ArrayList<Integer> nodes = new ArrayList<>();
        for(int i = 0; i <= n; i++)
            nodes.add(i);
        Collections.shuffle(nodes);
        for(int i = 0; i <= n; i++)
            scores.addTo(nodes.get(i), nodes.get(i));
        for(int i = 0; i <= n + 1; i++)
        {
            NodeScores tmpScores = new NodeScores(scores);
            tmpScores.keepTop(i);
            assertEquals(tmpScores.size(), i);
            for(int u = 0; u < i; u++)
            {
                assertEquals(tmpScores.get(n - u), (double)n - u);
                assertEquals(scores.get(n - u), (double)n - u);
            }
        }
    }
    
    public void testTopNSameValue()
    {
        NodeScores scores = new NodeScores();
        int n = 500;
        ArrayList<Integer> nodes = new ArrayList<>();
        for(int i = 0; i <= n; i++)
            nodes.add(i);
        Collections.shuffle(nodes);
        for(int i = 0; i <= n; i++)
            scores.addTo(nodes.get(i), 1);
        for(int i = 0; i <= n + 1; i++)
        {
            NodeScores tmpScores = new NodeScores(scores);
            tmpScores.keepTop(i);
            assertEquals(tmpScores.size(), i);
            for(int u = 0; u < i; u++)
            {
                assertEquals(tmpScores.get(u), 1d);
                assertEquals(scores.get(u), 1d);
            }
        }
    }
}
