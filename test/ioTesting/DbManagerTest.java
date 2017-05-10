package ioTesting;

import algorithms.PersonalizedPageRankAlgorithm;
import benchmarking.ComparisonData;
import benchmarking.Result;
import io.DbManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.json.Json;
import javax.json.JsonObject;
import junit.framework.TestCase;

/*
NOTE: to run these tests you need to either have the dabatase having name
and psw "ppr" and connect to it as the "ppr" user or modify the constructors
DbManager db = new DbManager("postgres", <user>, <dbname>, <psw>); 
DbManager db = new DbManager("mysql", <user>, <dbname>, <psw>); 
to reflect your connection infos, in the directory "db" of the project you
can find the scripts to set up the database.
*/
public class DbManagerTest extends TestCase 
{
    public void testConstructor()
    {
        try 
        {
            new DbManager("randomDBThatShouldntExist", "t", "t", "t");
            fail("this line shouldn't be reached");
        } 
        catch (IllegalArgumentException e) {}
        
        //these should work (shouldn't throw exceptions)
        try
        {
            DbManager db1 = new DbManager("postgres", "ppr", "ppr", "ppr");
            DbManager db2 = new DbManager("mysql", "ppr", "ppr", "ppr");
        }
        catch (IllegalArgumentException e)
        {
            fail("this line shouldn't be reached");
        }
    }
    
    
    public void testPSQLInsertAlgorithm()
    {
        DbManager db = new DbManager("postgres", "ppr", "ppr", "ppr");
        JsonObject params = Json.createObjectBuilder()
                .add("testP1", "description")
                .add("testP2", "")
                .build();
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", params);
        
        try (PreparedStatement st = db.getStatement("SELECT * FROM ALGORITHMS WHERE name = ?"))
        {
            st.setString(1, "testAlgorithm");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testAlgorithm", rs.getString("name"));
                    assertEquals(params.toString(), rs.getString("params"));
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
    
    public void testPSQLInsertGraph()
    {
        DbManager db = new DbManager("postgres", "ppr", "ppr", "ppr");
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
    
    
    public void testPSQLInsertRun()
    {
        DbManager db = new DbManager("postgres", "ppr", "ppr", "ppr");
        JsonObject params = Json.createObjectBuilder()
                .add("testP1", "description")
                .add("testP2", "")
                .build();
        double[] in = {0d, 0.5,0.9,0.2};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        PersonalizedPageRankAlgorithm.Parameters p1 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        PersonalizedPageRankAlgorithm.Parameters p2 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        ComparisonData data = new ComparisonData(10, r1, r2, p1, p2);

        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", params);
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
                    assertEquals(params.toString(), rs.getString("params"));
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
        
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
    }
    
    public void testMYSQLInsertAlgorithm()
    {
        DbManager db = new DbManager("mysql", "ppr", "ppr", "ppr");
        JsonObject params = Json.createObjectBuilder()
                .add("testP1", "description")
                .add("testP2", "")
                .build();
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", params);
        
        try (PreparedStatement st = db.getStatement("SELECT * FROM ALGORITHMS WHERE name = ?"))
        {
            st.setString(1, "testAlgorithm");
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    assertEquals("testAlgorithm", rs.getString("name"));
                    assertEquals(params.toString(), rs.getString("params"));
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
    
    public void testMYSQLInsertGraph()
    {
        DbManager db = new DbManager("mysql", "ppr", "ppr", "ppr");
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
    
    
    public void testMYSQLInsertRun()
    {
        DbManager db = new DbManager("mysql", "ppr", "ppr", "ppr");
        JsonObject params = Json.createObjectBuilder()
                .add("testP1", "description")
                .add("testP2", "")
                .build();
        double[] in = {0d, 0.5,0.9,0.2};
        Result r1 = new Result(in);
        Result r2 = new Result(in);
        PersonalizedPageRankAlgorithm.Parameters p1 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        PersonalizedPageRankAlgorithm.Parameters p2 = new PersonalizedPageRankAlgorithm.Parameters(0, 1, 2, 3d, 4d);
        ComparisonData data = new ComparisonData(10, r1, r2, p1, p2);

        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
        db.insertAlgorithm("testAlgorithm", params);
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
                    assertEquals(params.toString(), rs.getString("params"));
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
        
        db.query("DELETE FROM RUNS WHERE graph = 'testGraph'");
        db.query("DELETE FROM GRAPHS WHERE name = 'testGraph'");
        db.query("DELETE FROM ALGORITHMS WHERE name = 'testAlgorithm'");
    }
}
