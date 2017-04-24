package benchmarking;

import java.util.Objects;
import personalizedpagerank.Utility.Parameters;

/**
 * Class which stores the results of the comparison and the running
 * parameters of the confronted algorithms.
 */
public class ComparisonData
{
    private final int maxEntries;//max number of entries for each node considered for the comparison
    private final Result jaccard;
    private final Result kendall;
    private final Parameters param1;//parameters of the first algorithm
    private final Parameters param2;//parameters of the second algorithm

    public ComparisonData(int maxEntries, Result jaccard, Result kendall, 
            Parameters param1, Parameters param2)
    {
        if(maxEntries <= 0)
            throw new IllegalArgumentException("max scores to keep 'k' must be positive");
        this.maxEntries = maxEntries;
        this.jaccard = jaccard;
        this.kendall = kendall;
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

    public Result getKendall()
    {
        return kendall;
    }

    public Parameters getParam1() 
    {
        return param1;
    }

    public Parameters getParam2() 
    {
        return param2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.maxEntries;
        hash = 83 * hash + Objects.hashCode(this.jaccard);
        hash = 83 * hash + Objects.hashCode(this.kendall);
        hash = 83 * hash + Objects.hashCode(this.param1);
        hash = 83 * hash + Objects.hashCode(this.param2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComparisonData other = (ComparisonData) obj;
        if (this.maxEntries != other.maxEntries) {
            return false;
        }
        if (!Objects.equals(this.jaccard, other.jaccard)) {
            return false;
        }
        if (!Objects.equals(this.kendall, other.kendall)) {
            return false;
        }
        if (!Objects.equals(this.param1, other.param1)) {
            return false;
        }
        if (!Objects.equals(this.param2, other.param2)) {
            return false;
        }
        return true;
    }
}