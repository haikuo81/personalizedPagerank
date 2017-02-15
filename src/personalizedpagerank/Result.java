package personalizedpagerank;

/**
 * Class which stores results from comparisons in ResultComparator.
 */
public class Result
    {
        private final double[] stats;

        public Result(final double[] stats)
        {
            this.stats = stats;
        }
        
        public double getMin()
        {
            return stats[0];
        }
        
        public double getAverage()
        {
            return stats[1];
        }
        
        public double getMax()
        {
            return stats[2];
        }
        
        //standard deviation
        public double getStd()
        {
            return stats[3];
        }
    }
