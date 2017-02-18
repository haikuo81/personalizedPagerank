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
    private final Result jaccard;
    private final Result levenstein;
    private final Parameters param1;//parameters of the first algorithm
    private final Parameters param2;//parameters of the second algorithm

    public ComparisonData(final Result jaccard, final Result levenstein, Parameters param1, Parameters param2)
    {
        this.jaccard = jaccard;
        this.levenstein = levenstein;
        this.param1 = param1;
        this.param2 = param2;
    }

    public Result getJaccard() 
    {
        return jaccard;
    }

    public Result getLevenstein() 
    {
        return levenstein;
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
        String header = "nodes,edges,jaccard min,jaccard average,jaccard max,jaccard std,"
                + "levenstein min,levenstein average,levenstein max,levenstein std";
        
        //add header for first algorithm parameters
        header += ",iterations alg1,damping alg1,tolerance alg1";
        if(data[0].param1 instanceof GuerrieriRank.GuerrieriParameters)
            header += ",smallTop alg1,largeTop alg1,topRatio alg1";
        //add header for second algorithm parameters
        header += ",iterations alg2,damping alg2,tolerance alg2";
        if(data[0].param2 instanceof GuerrieriRank.GuerrieriParameters)
            header += ",smallTop alg2,largeTop alg2,topRatio alg2";
        
        builder.append(header);
        builder.append(System.getProperty("line.separator"));
        
        //write each instance of the ComparisonData[] as a line 
        for(int i = 0; i < data.length; i++)
        {
            builder.append(data[i].param1.getVertices()).append(",");
            builder.append(data[i].param1.getEdges()).append(",");
            
            builder.append(data[i].jaccard.getMin()).append(",");
            builder.append(data[i].jaccard.getAverage()).append(",");
            builder.append(data[i].jaccard.getMax()).append(",");
            builder.append(data[i].jaccard.getStd()).append(",");
            
            builder.append(data[i].levenstein.getMin()).append(",");
            builder.append(data[i].levenstein.getAverage()).append(",");
            builder.append(data[i].levenstein.getMax()).append(",");
            builder.append(data[i].levenstein.getStd()).append(",");
            
            builder.append(data[i].param1.getIterations()).append(",");
            builder.append(data[i].param1.getDamping()).append(",");
            builder.append(data[i].param1.getTolerance()).append(",");
            
            if(data[i].param1 instanceof GuerrieriRank.GuerrieriParameters)
            {
                GuerrieriRank.GuerrieriParameters p = (GuerrieriRank.GuerrieriParameters) data[i].param1;
                builder.append(p.getSmallTop()).append(",");
                builder.append(p.getLargetTop()).append(",");
                builder.append( ((double)p.getLargetTop()) / p.getSmallTop()).append(",");
            }
            
            builder.append(data[i].param2.getIterations()).append(",");
            builder.append(data[i].param2.getDamping()).append(",");
            builder.append(data[i].param2.getTolerance());
            
            if(data[i].param2 instanceof GuerrieriRank.GuerrieriParameters)
            {
                builder.append(",");
                GuerrieriRank.GuerrieriParameters p = (GuerrieriRank.GuerrieriParameters) data[i].param2;
                builder.append(p.getSmallTop()).append(",");
                builder.append(p.getLargetTop()).append(",");
                //do not append "," since it's the last element
                builder.append( ((double)p.getLargetTop()) / p.getSmallTop());
            }
            builder.append(System.getProperty("line.separator"));
        }
        writer.write(builder.toString());
        writer.close();
    }
}