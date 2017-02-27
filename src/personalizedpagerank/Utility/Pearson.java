package personalizedpagerank.Utility;

/**
 * Class which calculates the pearson correlation coefficient between 2 variables,
 * their values are represented by arrays.
 */
public class Pearson 
{
    private Pearson(){}
    
    /**
     * Given 2 arrays return the pearson correlation coefficient, the arrays
     * must have the same length.
     * @param x First array.
     * @param y Second array.
     * @return The pearson correlation coefficient.
     */
    public static double correlation(double[] x, double[] y)
    {
        if(x.length != y.length)
            throw new IllegalArgumentException("arrays must have same length");
        if(x.length == 0 || y.length == 0)
            throw new IllegalArgumentException("arrays must have a length greater than 0");
        double res = 0;
        double averageX = 0;
        double averageY = 0;
        double stdX, stdY; 
        //sum and sumSquared are used to calculate the standard deviation 
        double squareSumX = 0;
        double squareSumY = 0;
        for(int i = 0; i < x.length; i++)
        {
            averageX += x[i];
            averageY += y[i];
            squareSumX += x[i] * x[i];
            squareSumY += y[i] * y[i];
            
        }
        
        //compute standard deviations as sqrt ( 1/n *(squaresum - sum^2/N) )
        stdX = Math.sqrt
        (
                (squareSumX -(averageX * averageX) / x.length) / x.length
        );
        
        stdY = Math.sqrt
        (
                (squareSumY -(averageY * averageY) / y.length) / y.length
        );
        
        if(stdX == 0d || stdY == 0d)
            throw new IllegalArgumentException("pearson correlation is not defined"
                    + " for variables that have a standard deviation of 0");
        
        //compute the averages
        averageX /= x.length;
        averageY /= y.length;
        
        //calculate the correlation coefficient
        //covariance
        for(int i = 0; i < x.length; i++)
            res += (x[i] - averageX) * (y[i] - averageY);
        res /= x.length;
        //divide by the product of the standard deviations
        res /= (stdX * stdY);
        
        return res;
    }
}
