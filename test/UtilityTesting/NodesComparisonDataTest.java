package UtilityTesting;

import junit.framework.TestCase;
import personalizedpagerank.Utility.NodesComparisonData;
import personalizedpagerank.Utility.Parameters;

public class NodesComparisonDataTest extends TestCase
{
    
    public void testConstructor()
    {
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        
        try 
        {
           new NodesComparisonData(0, 100, p1, p2);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        try 
        {
           new NodesComparisonData(-1, 100, p1, p2);
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        NodesComparisonData data = new NodesComparisonData(10, 100, p1, p2);
        assertSame(data.getParam1(), p1);
        assertSame(data.getParam2(), p2);
        assertNotSame(data.getParam1(), p2);
        assertNotSame(data.getParam2(), p1);
        assertEquals(data.getMaxEntries(), 10, 0);
        assertEquals(data.getLength(), 100, 0);
        
        for(int i = 0; i < 100; i++)
        {
            assertEquals(data.getId(i), 0, 0);
            assertEquals(data.getIndegree(i), 0, 0);
            assertEquals(data.getJaccard(i), 0, 0);
            assertEquals(data.getKendall(i), 0, 0);
            assertEquals(data.getNeighbourIn(i), 0, 0);
            assertEquals(data.getNeighbourJaccard(i), 0, 0);
            assertEquals(data.getNeighbourKendall(i), 0, 0);
            assertEquals(data.getNeighbourOut(i), 0, 0);
            assertEquals(data.getNeighbourPagerank(i), 0, 0);
            assertEquals(data.getNeighbourPagerankError(i), 0, 0);
            assertEquals(data.getOutdegree(i), 0, 0);
            assertEquals(data.getPagerank(i), 0, 0);
            assertEquals(data.getPagerankError(i), 0, 0);
            assertEquals(data.getExcluded(i), 0 , 0);
            assertEquals(data.getIncluded(i), 0 , 0);
        }
    }
    
    public void testSettersAndGetters()
    {
        Parameters p1 = new Parameters(0, 1, 2, 3d, 4d);
        Parameters p2 = new Parameters(0, 1, 2, 3d, 4d);
        NodesComparisonData data = new NodesComparisonData(10, 100, p1, p2);
        for(int i = 0; i < 100; i++)
        {
            data.setId(i, i);
            data.setIndegree(i, i);
            data.setJaccard(i, i);
            data.setKendall(i, i);
            data.setNeighbourIn(i, i);
            data.setNeighbourJaccard(i, i);
            data.setNeighbourKendall(i, i);
            data.setNeighbourOut(i, i);
            data.setNeighbourPagerank(i, i);
            data.setNeighbourPagerankError(i, i);
            data.setOutdegree(i, i);
            data.setPagerank(i, i);
            data.setPagerankError(i, i);
            data.setExcluded(i, i);
            data.setIncluded(i, i);
            data.setNeighbourExcluded(i, i);
            data.setNeighbourIncluded(i, i);
            
        }
        
        for(int i = 0; i < 100; i++)
        {
            assertEquals(data.getId(i), i, 0);
            assertEquals(data.getIndegree(i), i, 0);
            assertEquals(data.getJaccard(i), i, 0);
            assertEquals(data.getKendall(i), i, 0);
            assertEquals(data.getNeighbourIn(i), i, 0);
            assertEquals(data.getNeighbourJaccard(i), i, 0);
            assertEquals(data.getNeighbourKendall(i), i, 0);
            assertEquals(data.getNeighbourOut(i), i, 0);
            assertEquals(data.getNeighbourPagerank(i), i, 0);
            assertEquals(data.getNeighbourPagerankError(i), i, 0);
            assertEquals(data.getOutdegree(i), i, 0);
            assertEquals(data.getPagerank(i), i, 0);
            assertEquals(data.getPagerankError(i), i, 0);
            assertEquals(data.getExcluded(i), i , 0);
            assertEquals(data.getIncluded(i), i , 0);
            assertEquals(data.getNeighbourExcluded(i), i, 0);
            assertEquals(data.getNeighbourIncluded(i), i, 0);
        }
    }

}
