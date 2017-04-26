package ioTesting;

import algorithms.PersonalizedPageRankAlgorithm;
import benchmarking.ComparisonData;
import benchmarking.Result;
import io.DbManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import junit.framework.TestCase;

public class DbManagerTest extends TestCase 
{
    DbManager db = new DbManager();
    
    public void testInsertAlgorithm()
    {
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", 0);
        
        try (PreparedStatement st = db.getStatement("SELECT * FROM ALGORITHMS WHERE name = ?"))
        {
            st.setString(1, "testAlgorithm");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testAlgorithm", rs.getString("name"));
                    assertEquals(0, rs.getInt("numberOfParameters"));
                }
                else
                    fail();
            }
        }
        catch (Exception e) 
        {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
    }
    
    public void testInsertGraph()
    {
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertGraph("testGraph", 10, 20, true, false);
        
        try (PreparedStatement st = db.getStatement("SELECT * FROM GRAPHS WHERE name = ?"))
        {
            st.setString(1, "testGraph");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testGraph", rs.getString("name"));
                    assertEquals(10, rs.getInt("vertices"));
                    assertEquals(20, rs.getInt("edges"));
                    assertTrue(rs.getBoolean("directed"));
                    assertTrue(!rs.getBoolean("bipartite"));
                }
                else
                    fail();
            }
        }
        catch (Exception e) 
        {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
    }
    
    
    public void testInsertUpdateRun()
    {
        double[] params = {2, 3, 4.0, -0.3};
        double[] in = {0d, 0.5,0.9,0.2};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        PersonalizedPageRankAlgorithm.Parameters p1 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        PersonalizedPageRankAlgorithm.Parameters p2 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        ComparisonData data = new ComparisonData(10, r1, r2, p1, p2);

        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", 4);
        db.insertGraph("testGraph", 10, 20, true, false);
        db.insertRun("testGraph", "testAlgorithm", "testCpu", 50, params, data, 1337);
        
        //inserting 
        try (PreparedStatement st = db.getStatement("SELECT * FROM RUNS WHERE graph = ?"))
        {
            st.setString(1, "testGraph");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testGraph", rs.getString("graph"));
                    assertEquals("testAlgorithm", rs.getString("algorithm"));
                    assertEquals("testCpu", rs.getString("cpu"));
                    assertEquals(50, rs.getInt("sampleNodes"));
                    assertEquals(10, rs.getInt("topK"));
                    Double[] tmp = (Double[])(rs.getArray("params")).getArray();
                    for(int i = 0; i < params.length; i++)
                        assertEquals(tmp[i], params[i], 0.0001);
                    assertEquals(50, rs.getInt("jaccardAverage"));
                    assertEquals(r1.getMin(), rs.getFloat("jaccardMin"), 0.0001);
                    assertEquals(r1.getStd(), rs.getFloat("jaccardStd"), 0.0001);
                    assertEquals(50, rs.getInt("kendallAverage"), 0.0001);
                    assertEquals(r1.getMin(), rs.getFloat("KendallMin"), 0.0001);
                    assertEquals(r1.getStd(), rs.getFloat("KendallStd"), 0.0001);
                    assertEquals(1337, rs.getInt("runTime"));
                }
                else
                    fail();
            }
        }
        catch (Exception e) 
        {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        
        //updating
        db.insertRun("testGraph", "testAlgorithm", "testCpu", 50, params, data, 1336);
        try (PreparedStatement st = db.getStatement("SELECT * FROM RUNS WHERE graph = ?"))
        {
            st.setString(1, "testGraph");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testGraph", rs.getString("graph"));
                    assertEquals("testAlgorithm", rs.getString("algorithm"));
                    assertEquals("testCpu", rs.getString("cpu"));
                    assertEquals(50, rs.getInt("sampleNodes"));
                    assertEquals(10, rs.getInt("topK"));
                    Double[] tmp = (Double[])(rs.getArray("params")).getArray();
                    for(int i = 0; i < params.length; i++)
                        assertEquals(tmp[i], params[i], 0.0001);
                    assertEquals(50, rs.getInt("jaccardAverage"));
                    assertEquals(r1.getMin(), rs.getFloat("jaccardMin"), 0.0001);
                    assertEquals(r1.getStd(), rs.getFloat("jaccardStd"), 0.0001);
                    assertEquals(50, rs.getInt("kendallAverage"), 0.0001);
                    assertEquals(r1.getMin(), rs.getFloat("KendallMin"), 0.0001);
                    assertEquals(r1.getStd(), rs.getFloat("kendallStd"), 0.0001);
                    assertEquals(1336, rs.getInt("runTime"));
                }
                else
                    fail();
            }
        }
        catch (Exception e) 
        {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
    }
}
