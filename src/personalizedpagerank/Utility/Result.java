package personalizedpagerank.Utility;

/**
 * Class which stores results from comparisons in ResultComparator.
 */
public class Result
    {
        private final double min, average, max, std;

        public Result(double min, double average, double max, double std) 
        {
            this.min = min;
            this.max = max;
            this.average = average;
            this.std = std;
        }
        
        public Result(final double[] stats)
        {
            min = stats[0];
            average = stats[1];
            max = stats[2];
            std = stats[3];
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getAverage() {
            return average;
        }

        public double getStd() {
            return std;
        }
        
    }
