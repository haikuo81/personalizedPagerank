
package io;

import benchmarking.ComparisonData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.json.JsonObject;

public final class DbManager 
{
    private Connection con;
    public DbManager(String dbms, String dbName, String user, String psw)
    {
        if(dbms.equals("postgres"))
        {
            String url = "jdbc:postgresql://localhost/" + dbName + 
                    "?user=" + user + "&password=" + psw + "&ssl=true";
            try 
            {
                Class.forName("org.postgresql.Driver");
                con = DriverManager
                    .getConnection(url);
            } 
            catch (Exception e) 
            {
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }
        else if(dbms.equals("mysql"))
        {
            String url = "jdbc:mysql://localhost/" + dbName + 
                    "?user=" + user + "&password=" + psw + "&ssl=true";
            try 
            {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager
                    .getConnection(url);
            } 
            catch (Exception e) 
            {
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }
        else
        {
            throw new IllegalArgumentException("I don't know that dbms");
        }
        System.out.println("established connection to database");
    }
    
    /**
     * Adds an algorithm to the database.
     * @param name Name of the algorithm (will be used as a key).
     * for costraints and checks).
     * @param params Json describing the parameters of the algorithm
    */
    public final void insertAlgorithm(String name, JsonObject params)
    {
        try (PreparedStatement st = con.prepareStatement("INSERT INTO ALGORITHMS VALUES(?,?)"))
        {
            st.setString(1, name);
            st.setString(2, params.toString());
            st.executeUpdate();
        }
        catch (Exception e) 
        {
            System.err.println("couldnt insert algorithm: " + e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Adds a graph to the database.
     * @param name Name of the graph (will be used as a key).
     * @param vertices Total vertices of the graph.
     * @param edges Total edges of the graph.
     * @param directed True if directed.
     * @param bipartite True if bipartite.
    */
    public final void insertGraph(String name, int vertices, int edges, 
            boolean directed, boolean bipartite)
    {
        try (PreparedStatement st = con.prepareStatement("INSERT INTO GRAPHS VALUES(?,?,?,?,?)"))
        {
            st.setString(1, name);
            st.setInt(2, vertices);
            st.setInt(3, edges);
            st.setBoolean(4, directed);
            st.setBoolean(5, bipartite);
            st.executeUpdate();
        }
        catch (Exception e) 
        {
            System.err.println("couldnt insert graph: " + e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Adds a run into the db.
     * @param graphName The graph used in the run.
     * @param algName Algorithm used in the run.
     * @param cpuName Name of the cpu used for the run.
     * @param sampleNodes Number of sample nodes used to compare results between
     * the algorithm and the classic pagerank.
     * @param params Json object containing the parameters.
     * @param data Contains results of the comparison.
     * @param runTime Time spent during this run.
     */
    public final void insertRun(String graphName, String algName, String cpuName,
            int sampleNodes, JsonObject params, ComparisonData data, int runTime)
    {
        /*
        formatting jaccard and kendall value before inserting it into the db because the admitted
        values will be 0 .. 100 for jaccard and -100 .. 100 for kendall 
        */
        int newJac = (int) (data.getJaccard().getAverage() * 100); 
        int newKen = (int) (data.getKendall().getAverage() * 100);
        
        try (PreparedStatement st = con.prepareStatement("INSERT INTO RUNS"
                + " (graph, algorithm, cpu, sampleNodes, topK, params, jaccardAverage, "
                + " jaccardMin, jaccardStd, kendallAverage, kendallMin, kendallStd, runTime)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)"))
        {
            st.setString(1, graphName);
            st.setString(2, algName);
            st.setString(3, cpuName);
            st.setInt(4, sampleNodes);
            st.setInt(5, data.getMaxEntries());
            st.setString(6, params.toString());
            st.setInt(7, newJac);
            st.setDouble(8, data.getJaccard().getMin());
            st.setDouble(9, data.getJaccard().getStd());
            st.setInt(10, newKen);
            st.setDouble(11, data.getKendall().getMin());
            st.setDouble(12, data.getKendall().getStd());
            st.setInt(13, runTime);
            st.executeUpdate();
        }
        catch (Exception e) 
        {
            System.err.println("couldnt insert run: " + e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Simple method to allow querying directly (mostly used for testing).
     * @param query Sql query.
    */
    public void query(String query)
    {
       try (PreparedStatement st = con.prepareStatement(query))
            {
                st.executeUpdate();
            }
            catch (Exception e) 
            {
                System.err.println("couldnt query: " + e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
    }
    
    /**
     * Simple method to allow getting a prepared statement from the db manager.
     * @param query Sql query.
     * @return
     * @throws SQLException 
    */
    public PreparedStatement getStatement(String query) throws SQLException
    {
        return con.prepareStatement(query);
    }
}

