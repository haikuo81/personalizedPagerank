
package io;

import benchmarking.ComparisonData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class DbManager 
{
    private Connection con;
    public DbManager()
    {
        String url = "jdbc:postgresql://localhost/ppr?user=ppr&password=ppr&ssl=true";
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
        System.out.println("established connection to database");
    }
    
    /**
     * Adds an algorithm to the database.
     * @param name Name of the algorithm (will be used as a key).
     * @param paramsNumber Number of parameters of the algorithm (will be used
     * for costraints and checks).
     */
    public final void insertAlgorithm(String name, int paramsNumber)
    {
        try (PreparedStatement st = con.prepareStatement("INSERT INTO ALGORITHMS VALUES(?,?)"))
        {
            st.setString(1, name);
            st.setInt(2, paramsNumber);
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
    
    public final void insertRun(String graphName, String algName, String cpuName,
            int sampleNodes, double[] params, ComparisonData data, int runTime)
    {
        /*
        formatting jaccard value before inserting it into the db because the admitted
        values will be 0 .. 100 since for each (algorithm, graph, cpu, topK)
        we are looking to have 101 rows.
        */
        int newJac = (int) (data.getJaccard().getAverage() * 100); 
        System.out.println(newJac);
        int oldTime = -1;
        /*
        get the old runtime for a run with same
        (algorithm, graph, cpu, topK, jaccardAverage)
        */
        try (PreparedStatement st = con.prepareStatement("SELECT runTime"
                + " FROM RUNS WHERE graph = ? AND algorithm = ?"
                + " AND cpu = ? AND topK = ? AND jaccardAverage = ?"))
        {
            st.setString(1, graphName);
            st.setString(2, algName);
            st.setString(3, cpuName);
            st.setInt(4, data.getMaxEntries());
            st.setInt(5, newJac);
            
            try (ResultSet rs = st.executeQuery())
            {
                if(rs.next())
                {
                    oldTime = rs.getInt("runTime");
                }
            }
        }
        catch (Exception e) 
        {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        
        //if the new run time is better update the row with the new values
        if(runTime < oldTime)
        {
            try (PreparedStatement st = con.prepareStatement("UPDATE RUNS SET"
                    + " sampleNodes = ?, params = ?, jaccardMin = ?"
                    + " , jaccardStd = ?, kendallAverage = ?, kendallMin = ?,"
                    + " kendallStd = ?, runTime = ? WHERE graph = ? AND algorithm = ?"
                + " AND cpu = ? AND topK = ? AND jaccardAverage = ?" ))
            {
                Double[] tmp = new Double[params.length];
                for(int i = 0; i < params.length; i++)
                    tmp[i] = params[i];
                st.setInt(1, sampleNodes);
                st.setArray(2, con.createArrayOf("float4", tmp));
                st.setDouble(3, data.getJaccard().getMin());
                st.setDouble(4, data.getJaccard().getStd());
                st.setDouble(5, data.getKendall().getAverage());
                st.setDouble(6, data.getKendall().getMin());
                st.setDouble(7, data.getKendall().getStd());
                st.setInt(8, runTime);
                
                
                st.setString(9, graphName);
                st.setString(10, algName);
                st.setString(11, cpuName);
                st.setInt(12, data.getMaxEntries());
                st.setInt(13, newJac);
                st.executeUpdate();
            }
            catch (Exception e) 
            {
                System.err.println("couldnt update run: " + e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }//if there wasn't a run with the same jaccard insert the data
        else if(oldTime == -1)
        {
            try (PreparedStatement st = con.prepareStatement("INSERT INTO RUNS"
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)"))
            {
                Double[] tmp = new Double[params.length];
                for(int i = 0; i < params.length; i++)
                    tmp[i] = params[i];
                st.setString(1, graphName);
                st.setString(2, algName);
                st.setString(3, cpuName);
                st.setInt(4, sampleNodes);
                st.setInt(5, data.getMaxEntries());
                st.setArray(6, con.createArrayOf("float4", tmp));
                st.setInt(7, newJac);
                st.setDouble(8, data.getJaccard().getMin());
                st.setDouble(9, data.getJaccard().getStd());
                st.setDouble(10, data.getKendall().getAverage());
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
    }
}
