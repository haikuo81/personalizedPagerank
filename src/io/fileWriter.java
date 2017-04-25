package io;

import benchmarking.NodesComparisonData;
import benchmarking.ComparisonData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import algorithms.BoundaryRestrictedPageRank;
import algorithms.GuerrieriRank;
import algorithms.GuerrieriRankV2;
import algorithms.GuerrieriRankV3;
import algorithms.MCCompletePathPageRank;
import utility.Parameters;

public class fileWriter 
{
    private fileWriter(){}
    
    /**
     * Writes the comparison results in a csv file.
     * @param path File to write results in (not appending).
     * @param data Comparisons, each comparison will be an entry (a line) in the
     * resulting csv.
     */
    public static void writeCsv(final String path, final ComparisonData[] data)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new File(path));
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("nodes,edges,");
                      
        //add headers for first and second algorithm parameters
        getHeaderNames(builder, data[0].getParam1(),",");
        getHeaderNames(builder, data[0].getParam2(),",");
        
        builder.append("max entries,jaccard min,jaccard average,jaccard max,jaccard std,"
                + "kendall min,kendall average,kendall max,kendall std");
        
        builder.append(System.getProperty("line.separator"));
        
        //write each instance of the ComparisonData[] as a line 
        for(int i = 0; i < data.length; i++)
        {
            builder.append(data[i].getParam1().getVertices()).append(",");
            builder.append(data[i].getParam1().getEdges()).append(",");
            
            getParametersData(builder, data[i].getParam1(), ",");
            getParametersData(builder, data[i].getParam2(), ",");
            
            builder.append(data[i].getMaxEntries()).append(",");
            builder.append(data[i].getJaccard().getMin()).append(",");
            builder.append(data[i].getJaccard().getAverage()).append(",");
            builder.append(data[i].getJaccard().getMax()).append(",");
            builder.append(data[i].getJaccard().getStd()).append(",");
            
            builder.append(data[i].getKendall().getMin()).append(",");
            builder.append(data[i].getKendall().getAverage()).append(",");
            builder.append(data[i].getKendall().getMax()).append(",");
            builder.append(data[i].getKendall().getStd());
            
            builder.append(System.getProperty("line.separator"));
        }
        writer.write(builder.toString());
        writer.close();
    }
    
    /**
     * Writes the origins comparison results in a csv file.
     * @param path File to write results in (not appending).
     * @param data Comparisons, each comparison will be an entry (a line) in the
     * resulting csv.
     */
    public static void writeCsv(final String path, final NodesComparisonData[] data)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new File(path));
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("nodes,edges,");
        
         //add headers for first and second algorithm parameters
        getHeaderNames(builder, data[0].getParam1(),",");
        getHeaderNames(builder, data[0].getParam2(),",");
        
        builder.append("max entries,node id,indegree,outdegree,"
                + "neighbour indegree,neighbour outdegree,pagerank,"
                + "neighbour pagerank,jaccard,neighbour jaccard,kendall,"
                + "neighbour kendall,"
                + "pagerank error,neighbour pagerank error,"
                + "excluded,neighbour excluded,included,neighbour included");
          
        builder.append(System.getProperty("line.separator"));
        
        //write each node of each instance of the OriginComparisonData[] as a line 
        for(int i = 0; i < data.length; i++)
        {
            for(int node = 0; node < data[i].getLength(); node++)
            {
                builder.append(data[i].getParam1().getVertices()).append(",");
                builder.append(data[i].getParam1().getEdges()).append(",");

                getParametersData(builder, data[i].getParam1(), ",");
                getParametersData(builder, data[i].getParam2(), ",");

                builder.append(data[i].getMaxEntries()).append(",");
                builder.append(data[i].getId(node)).append(",");
                
                builder.append(data[i].getIndegree(node)).append(",");
                builder.append(data[i].getOutdegree(node)).append(",");
                builder.append(data[i].getNeighbourIn(node)).append(",");
                builder.append(data[i].getNeighbourOut(node)).append(",");
                
                builder.append(data[i].getPagerank(node)).append(",");
                builder.append(data[i].getNeighbourPagerank(node)).append(",");
                
                builder.append(data[i].getJaccard(node)).append(",");
                builder.append(data[i].getNeighbourJaccard(node)).append(",");
                
                builder.append(data[i].getKendall(node)).append(",");
                builder.append(data[i].getNeighbourKendall(node)).append(",");
                
                builder.append(data[i].getPagerankError(node)).append(",");
                builder.append(data[i].getNeighbourPagerankError(node)).append(",");
                
                builder.append(data[i].getExcluded(node)).append(",");
                builder.append(data[i].getNeighbourExcluded(node)).append(",");
                
                builder.append(data[i].getIncluded(node)).append(",");
                builder.append(data[i].getNeighbourIncluded(node));
                
                builder.append(System.getProperty("line.separator"));
            }
        }
        
        writer.write(builder.toString());
        writer.close();
    }
    /**
     * Add to the header builder parameters names based on parameters class.
     * @param header Builder that will contain the parameter names.
     * @param parameters Class to decide what to add to the header.
     * @param append Last thing to append to the builder.
     */
    private static void getHeaderNames(final StringBuilder header, final Parameters parameters, final String append)
    {
        header.append("iterations,damping,tolerance");
        if(parameters instanceof GuerrieriRank.GuerrieriParameters || 
                parameters instanceof GuerrieriRankV2.GuerrieriParameters
            || parameters instanceof GuerrieriRankV3.GuerrieriParameters)
            header.append(",smallTop,largeTop,topRatio");
        else if(parameters instanceof BoundaryRestrictedPageRank.BoundaryRestrictedParameters)
            header.append(",frontierThreshold");
        else if(parameters instanceof MCCompletePathPageRank.MCCompletePathParameters)
            header.append(",smallTop");
        header.append(append);
    }
    
    /**
     * Get data from parameters to add to the content builder based on parameters
     * class.
     * @param content Builder that data will be appended to.
     * @param parameters Class which fields will be added to the builder. 
     * @param append Last thing to append to the builder.
     */
    private static void getParametersData(final StringBuilder content, 
            final Parameters parameters, final String append)
    {
        content.append(parameters.getIterations()).append(",");
        content.append(parameters.getDamping()).append(",");
        content.append(parameters.getTolerance());
        if(parameters instanceof GuerrieriRank.GuerrieriParameters)
        {
            GuerrieriRank.GuerrieriParameters p = (GuerrieriRank.GuerrieriParameters) parameters;
            content.append(",");
            content.append(p.getSmallTop()).append(",");
            content.append(p.getLargeTop()).append(",");
            content.append( ((double)p.getLargeTop()) / p.getSmallTop());
        }
        else if(parameters instanceof GuerrieriRankV2.GuerrieriParameters)
        {
            GuerrieriRankV2.GuerrieriParameters p = (GuerrieriRankV2.GuerrieriParameters) parameters;
            content.append(",");
            content.append(p.getSmallTop()).append(",");
            content.append(p.getLargeTop()).append(",");
            content.append( ((double)p.getLargeTop()) / p.getSmallTop());
        }
        else if(parameters instanceof GuerrieriRankV3.GuerrieriParameters)
        {
            GuerrieriRankV3.GuerrieriParameters p = (GuerrieriRankV3.GuerrieriParameters) parameters;
            content.append(",");
            content.append(p.getSmallTop()).append(",");
            content.append(p.getLargeTop()).append(",");
            content.append( ((double)p.getLargeTop()) / p.getSmallTop());
        }
        else if(parameters instanceof BoundaryRestrictedPageRank.BoundaryRestrictedParameters)
        {
            BoundaryRestrictedPageRank.BoundaryRestrictedParameters p =
                    (BoundaryRestrictedPageRank.BoundaryRestrictedParameters) parameters;
            content.append(",");
            content.append(p.getFrontierThreshold());
        }
        else if(parameters instanceof MCCompletePathPageRank.MCCompletePathParameters)
        {
            MCCompletePathPageRank.MCCompletePathParameters p = 
                    (MCCompletePathPageRank.MCCompletePathParameters) parameters;
            content.append(",");
            content.append(p.getSmallTop());
        }
        
        content.append(append);
    }

}
