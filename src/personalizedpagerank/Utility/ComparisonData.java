package personalizedpagerank.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import personalizedpagerank.Algorithms.GuerrieriRank;

/**
 * Class which stores the results of the comparison and the running
 * parameters of the confronted algorithms.
 */
public class ComparisonData
{
    private final int maxEntries;//max number of entries for each node considered for the comparison
    private final Result jaccard;
    private final Result levenstein;
    private final Result spearman;
    private final Parameters param1;//parameters of the first algorithm
    private final Parameters param2;//parameters of the second algorithm

    public ComparisonData(int maxEntries, Result jaccard, Result levenstein, 
            Result spearman, Parameters param1, Parameters param2)
    {
        this.maxEntries = maxEntries;
        this.jaccard = jaccard;
        this.levenstein = levenstein;
        this.spearman = spearman;
        this.param1 = param1;
        this.param2 = param2;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public Result getJaccard() 
    {
        return jaccard;
    }

    public Result getLevenstein() 
    {
        return levenstein;
    }

    public Result getSpearman()
    {
        return spearman;
    }
    
    public Parameters getParam1() 
    {
        return param1;
    }

    public Parameters getParam2() 
    {
        return param2;
    }
    
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
        getHeaderNames(builder, data[0].param1,",");
        getHeaderNames(builder, data[0].param2,",");
        
        builder.append("max entries, jaccard min,jaccard average,jaccard max,jaccard std,"
                + "levenstein min,levenstein average,levenstein max,levenstein std,"
                + "spearman min, spearman average, spearman max, spearman std");
        
        builder.append(System.getProperty("line.separator"));
        
        //write each instance of the ComparisonData[] as a line 
        for(int i = 0; i < data.length; i++)
        {
            builder.append(data[i].param1.getVertices()).append(",");
            builder.append(data[i].param1.getEdges()).append(",");
            
            getParametersData(builder, data[i].param1, ",");
            getParametersData(builder, data[i].param2, ",");
            
            builder.append(data[i].getMaxEntries()).append(",");
            builder.append(data[i].jaccard.getMin()).append(",");
            builder.append(data[i].jaccard.getAverage()).append(",");
            builder.append(data[i].jaccard.getMax()).append(",");
            builder.append(data[i].jaccard.getStd()).append(",");
            
            builder.append(data[i].levenstein.getMin()).append(",");
            builder.append(data[i].levenstein.getAverage()).append(",");
            builder.append(data[i].levenstein.getMax()).append(",");
            builder.append(data[i].levenstein.getStd()).append(",");
            
            builder.append(data[i].spearman.getMin()).append(",");
            builder.append(data[i].spearman.getAverage()).append(",");
            builder.append(data[i].spearman.getMax()).append(",");
            builder.append(data[i].spearman.getStd());
            
            builder.append(System.getProperty("line.separator"));
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
        if(parameters instanceof GuerrieriRank.GuerrieriParameters)
            header.append(",smallTop,largeTop,topRatio");
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
            content.append(p.getLargetTop()).append(",");
            content.append( ((double)p.getLargetTop()) / p.getSmallTop());
        }
        content.append(append);
    }
}