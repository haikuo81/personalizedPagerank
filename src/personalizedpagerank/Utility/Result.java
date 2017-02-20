package personalizedpagerank.Utility;

import java.util.Arrays;

/**
 * Class which stores results from comparisons in ResultComparator.
 */
public class Result
    {
        private final double[] stats;

        public Result(final double[] stats)
        {
            this.stats = Arrays.copyOf(stats, stats.length);
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

        //generated automatically
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
            final Result other = (Result) obj;
            if (!Arrays.equals(this.stats, other.stats)) {
                return false;
            }
            return true;
        }
    }
