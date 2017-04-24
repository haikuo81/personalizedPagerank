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
    
    public void testAddEmpty()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i, i);
        scores.add(new NodeScores());
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), (double) i);
    }
    
    public void testAddItself()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i, i);
        scores.add(scores);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i * 2d);
    }
    
    public void testAddDifferent()
    {
        NodeScores scores1 = new NodeScores();
        NodeScores scores2 = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores1.put(i, i);
        for(int i = 0; i < 100; i++)
            scores2.put(i, i * i);
        scores1.add(scores2);
        for(int i = 0; i < 100; i++)
            assertEquals(scores1.get(i), (double)i + i * i);
    }
    
    public void testAddEmptyWithFactor()
    {
        double factor = 0.5;
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i, i);
        scores.add(new NodeScores(), factor);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i),(double) i);
    }
    
    public void testAddItselfWithFactor()
    {
        double factor = 0.5;
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i, i);
        scores.add(scores, factor);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i + (i * factor));
    }
    
    public void testAddDifferentWithFactor()
    {
        double factor = 0.5;
        NodeScores scores1 = new NodeScores();
        NodeScores scores2 = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores1.put(i, i);
        for(int i = 0; i < 100; i++)
            scores2.put(i, i * i);
        scores1.add(scores2, factor);
        for(int i = 0; i < 100; i++)
            assertEquals(scores1.get(i), i + (i * i * factor));
    }
    
    public void testAddToAllToEmpty()
    {
        NodeScores scores = new NodeScores();
        scores.addToAll(0d);
        assertEquals(scores.size(), 0);
        scores.addToAll(Double.MAX_VALUE);
        assertEquals(scores.size(), 0);
        assertEquals(scores.get(1), 0d);
    }
    
    public void testAdd0ToAll()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.addToAll(0);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), (double) i);
    }
    
    public void testAddToAllPositive()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.addToAll(0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i + 0.25);
        scores.clear();
        for(int i = 0; i < 100; i++)
            scores.put(i , -i);
        scores.addToAll(0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), -i + 0.25);
    }
    
    public void testAddToAllNegative()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.addToAll(- 0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i - 0.25);
        scores.clear();
        for(int i = 0; i < 100; i++)
            scores.put(i , -i);
        scores.addToAll(- 0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), - i - 0.25);
    }
    
    public void testMultiplyToAllToEmpty()
    {
        NodeScores scores = new NodeScores();
        scores.multiplyAll(0d);
        assertEquals(scores.size(), 0);
        scores.multiplyAll(Double.MAX_VALUE);
        assertEquals(scores.size(), 0);
        assertEquals(scores.get(1), 0d);
    }
    
    public void testMultiply0ToAll()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.multiplyAll(0);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), 0d);
    }
    
    public void testMultiplyToAllPositive()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.multiplyAll(0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i * 0.25);
        scores.clear();
        for(int i = 0; i < 100; i++)
            scores.put(i , -i);
        scores.multiplyAll(0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), -i * 0.25);
    }
    
    public void testMultiplyToAllNegative()
    {
        NodeScores scores = new NodeScores();
        for(int i = 0; i < 100; i++)
            scores.put(i , i);
        scores.multiplyAll(- 0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), i *  (- 0.25));
        scores.clear();
        for(int i = 0; i < 100; i++)
            scores.put(i , -i);
        scores.multiplyAll(- 0.25);
        for(int i = 0; i < 100; i++)
            assertEquals(scores.get(i), - i * (- 0.25));
    }
}
