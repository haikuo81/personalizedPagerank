
package algorithmsTesting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import personalizedpagerank.algorithms.*;

import junit.framework.*;
import org.jgrapht.DirectedGraph;
import personalizedpagerank.PersonalizedPageRankAlgorithm;


public class GuerrieriRankTest extends TestCase
{
    public void testBadConstructorsParameters()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        
        //top L with L = 0
        try 
        {
            new GuerrieriRank<>(g, 0);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //0 iterations
        try 
        {
            new GuerrieriRank<>(g, 10, 0);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //negative dampingfactor
        try 
        {
            new GuerrieriRank<>(g, 10, 10, -0.1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
      
        //dampingfactor over 1
        try 
        {
            new GuerrieriRank<>(g, 10, 10, 1.1);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
    }
    
    public void testSingleNode()
    {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph(DefaultEdge.class);
        
        g.addVertex(1);
        
        PersonalizedPageRankAlgorithm res = new GuerrieriRank(g);
        
        
    }
}
