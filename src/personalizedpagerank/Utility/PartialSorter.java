package personalizedpagerank.Utility;

import java.util.Comparator;

/**
 * Partial sorter that uses selection sort to partially sort, relations between
 * values are decided by a comparator passed as an argument.
 * @param <V> Class of elements to partially sort.
 */
public class PartialSorter<V>
{
    /**
     * Partially sorts the array using selection sort.
     * @param input Input array of objects to partially sort.
     * @param n The nth element used to sort.
     * @param comp Comparator that returns 0 if 2 values are equal, a negative or 
     * a positive value when they are not depending on which order you want
     * to partially sort.
     */
    public void partialSort(V[] input, int n, Comparator< ? super V> comp) 
    {
        if (n >= input.length)
            throw new IllegalArgumentException("N must be lower than the length of the input");
        int from = 0, to = input.length - 1;

        while (from < to) 
        {
            int leftIndex = from, rightIndex = to;
            V mid = input[(leftIndex + rightIndex) / 2];
            
            while (leftIndex < rightIndex) 
            {
                /*
                if the value is greater than the pivot move it on the right 
                side by swapping it with the value at rightIndex, else move on
                */
                if (comp.compare(input[leftIndex], mid) >= 0) 
                { 
                    V tmp = input[rightIndex];
                    input[rightIndex] = input[leftIndex];
                    input[leftIndex] = tmp;
                    rightIndex--;
                } 
                else
                    leftIndex++;
            }
            if (comp.compare(input[leftIndex], mid) > 0)
                leftIndex--;
            //change to or from depending if what we are looking for is on the left or right part
            if (n <= leftIndex) 
                to = leftIndex;
            else 
                from = leftIndex + 1;
        }
    }
}
