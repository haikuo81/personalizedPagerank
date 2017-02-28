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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
            hash = 47 * hash + (int) (Double.doubleToLongBits(this.average) ^ (Double.doubleToLongBits(this.average) >>> 32));
            hash = 47 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
            hash = 47 * hash + (int) (Double.doubleToLongBits(this.std) ^ (Double.doubleToLongBits(this.std) >>> 32));
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
            final Result other = (Result) obj;
            if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(other.min)) {
                return false;
            }
            if (Double.doubleToLongBits(this.average) != Double.doubleToLongBits(other.average)) {
                return false;
            }
            if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
                return false;
            }
            if (Double.doubleToLongBits(this.std) != Double.doubleToLongBits(other.std)) {
                return false;
            }
            return true;
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
