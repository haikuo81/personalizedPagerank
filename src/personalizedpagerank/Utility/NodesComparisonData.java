package personalizedpagerank.Utility;

/**
 * Class to hold comparison data regarding the origin nodes of personalized
 * pagerank individually.
 */

public class NodesComparisonData 
{
    private final int maxEntries;//max number of entries for each node considered for the comparison
    private final int[] id;
    private final int[] indegree;
    private final int[] outdegree;
    private final double[] neighbourIn;//average neighbour in degree
    private final double[] neighbourOut;//average neighbour out degree
    private final double[] pagerank;//score in classic pagerank
    private final double[] neighbourPagerank;//average pagerank of the neighbour
    private final double[] jaccard;
    private final double[] neighbourJaccard;
    private final double[] levenstein;
    private final double[] neighbourLevenstein;
    private final double[] spearman;
    private final double[] neighbourSpearman;
    private final double[] pagerankError;
    private final double[] neighbourPagerankError;
    private final Parameters param1;
    private final Parameters param2;

    
    public NodesComparisonData(int maxEntries, int length, Parameters p1, Parameters p2)
    {
        this.maxEntries = maxEntries;
        id = new int[length];
        indegree = new int[length];
        outdegree = new int[length];
        neighbourIn = new double[length];
        neighbourOut = new double[length];
        pagerank = new double[length];
        neighbourPagerank = new double[length];
        jaccard = new double[length];
        neighbourJaccard = new double[length];
        levenstein = new double[length];
        neighbourLevenstein = new double[length];
        spearman = new double[length];
        neighbourSpearman = new double[length];
        pagerankError = new double[length];
        neighbourPagerankError = new double[length];
        param1 = p1;
        param2 = p2;
    }
    
    //SETTERS
    /////////
    
    public void setId(int index, int value) {
        id[index] = value;
    }

    public void setIndegree(int index, int value) {
        indegree[index] = value;
    }

    public void setOutdegree(int index, int value) {
        outdegree[index] = value;
    }

    public void setNeighbourIn(int index, double value) {
        neighbourIn[index] = value;
    }

    public void setNeighbourOut(int index, double value) {
        neighbourOut[index] = value;
    }

    public void setPagerank(int index, double value) {
        pagerank[index] = value;
    }

    public void setNeighbourPagerank(int index, double value) {
        neighbourPagerank[index] = value;
    }

    public void setJaccard(int index, double value) {
        jaccard[index] = value;
    }

    public void setNeighbourJaccard(int index, double value) {
        neighbourJaccard[index] = value;
    }

    public void setLevenstein(int index, double value) {
        levenstein[index] = value;
    }

    public void setNeighbourLevenstein(int index, double value) {
        neighbourLevenstein[index] = value;
    }

    public void setSpearman(int index, double value) {
        spearman[index] = value;
    }

    public void setNeighbourSpearman(int index, double value) {
        neighbourSpearman[index] = value;
    }
    
    public void setPagerankError(int index, double value) {
        pagerankError[index] = value;
    }
    
    public void setNeighbourPagerankError(int index, double value) {
        neighbourPagerankError[index] = value;
    }
    
    
    //GETTERS
    /////////

    public int getMaxEntries() {
        return maxEntries;
    }
    
    public int getLength()
    {
        return id.length;
    }

    public int getId(int index) {
        return id[index];
    }

    public int getIndegree(int index) {
        return indegree[index];
    }

    public int getOutdegree(int index) {
        return outdegree[index];
    }

    public double getNeighbourIn(int index) {
        return neighbourIn[index];
    }

    public double getNeighbourOut(int index) {
        return neighbourOut[index];
    }

    public double getPagerank(int index) {
        return pagerank[index];
    }

    public double getNeighbourPagerank(int index) {
        return neighbourPagerank[index];
    }

    public double getJaccard(int index) {
        return jaccard[index];
    }

    public double getNeighbourJaccard(int index) {
        return neighbourJaccard[index];
    }

    public double getLevenstein(int index) {
        return levenstein[index];
    }

    public double getNeighbourLevenstein(int index) {
        return neighbourLevenstein[index];
    }

    public double getSpearman(int index) {
        return spearman[index];
    }

    public double getNeighbourSpearman(int index) {
        return neighbourSpearman[index];
    }

    public double getPagerankError(int index) {
        return pagerankError[index];
    }
    
    public double getNeighbourPagerankError(int index) {
        return neighbourPagerankError[index];
    }
    
    public Parameters getParam1() {
        return param1;
    }

    public Parameters getParam2() {
        return param2;
    }
}
