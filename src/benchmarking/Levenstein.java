package benchmarking;

import java.util.Comparator;

/**
 * Java class for levenstein distance comparison between arrays as if each
 * entry was a different symbol, took it out of ResultComparator for testing purposes.
 * @param <V> Type of objects in the array.
 */
public class Levenstein<V> 
{
    /**
     * Compute the levenstein distance between two arrays as if each entry is a symbol.
     * @param m1 First array.
     * @param m2 Second array.
     * @return Levenstein distance between the two arrays.
     */
    public int distance(V[] m1, V[] m2)
    {
        int [][] matrix = new int[m1.length+1][m2.length+1];
        
        //if the string is empty the distance is the length of the non empty string
        for (int i = 0; i <= m1.length; i++)
            matrix[i][0] = i;
        for (int u = 0; u <= m2.length; u++)
            matrix[0][u] = u;

        //filling from top-left to bottom-right 
        for (int i = 1; i < matrix.length; i++)
        {
            for (int u = 1; u < matrix[i].length; u++)
            {
                //if values are equals this step is free
                if (m1[i-1].equals(m2[u-1]))
                    matrix[i][u] = matrix[i-1][u-1];
                else
                {
                    //if values aren't equals we need to modify a string
                    //pick the sequence that can lead you here with min moves
                    matrix[i][u] = Math.min(matrix[i-1][u], matrix[i][u-1]);
                    matrix[i][u] = Math.min(matrix[i][u], matrix[i-1][u-1]);
                    matrix[i][u]++;
                }
            }
        }
       return matrix[m1.length][m2.length];
    }
    
    /**
     * Compute the levenstein distance between two arrays as if each entry is a symbol,
     * using a provided comparator to decide when two symbols are equal.
     * @param m1 First array.
     * @param m2 Second array.
     * @param comp Comparator that will return 0 when two values are equal.
     * @return Levenstein distance between the two arrays.
     */
    public int distance(V[] m1, V[] m2, Comparator< ? super V> comp)
    {
        int [][] matrix = new int[m1.length + 1][m2.length + 1];
        
        //if the string is empty the distance is the length of the non empty string
        for (int i = 0; i <= m1.length; i++)
            matrix[i][0] = i;
        for (int u = 0; u <= m2.length; u++)
            matrix[0][u] = u;

        //filling from top-left to bottom-right 
        for (int i = 1; i < matrix.length; i++)
        {
            for (int u = 1; u < matrix[i].length; u++)
            {
                //if values are equals this step is free
                if (comp.compare(m1[i - 1], m2[u - 1]) == 0)
                    matrix[i][u] = matrix[i-1][u-1];
                else
                {
                    //if values aren't equals we need to modify a string
                    //pick the sequence that can lead you here with min moves
                    matrix[i][u] = Math.min(matrix[i-1][u], matrix[i][u-1]);
                    matrix[i][u] = Math.min(matrix[i][u], matrix[i-1][u-1]);
                    matrix[i][u]++;
                }
            }
        }
       return matrix[m1.length][m2.length];
    }
}
