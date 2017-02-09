/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package personalizedpagerank.Utility;

import java.util.Map;

/**
 * Partial sorter that uses selection sort.
 * @param <V> Keys in the Map.Entry pairs.
 */
public class PartialSorter<V>
{
    /**
     * Partially sorts the array using selection sort.
     * Values greater than the nth value (the value that would be on the nth
     * position if the array was sorted by entry.value in descending order) will
     * be on the left, and the lower values on the right.
     * @param input Input array of entries.
     * @param n The nth element used to sort.
     */
    public void partialSort(Map.Entry<V, Double>[] input, int n) 
    {
        if (n >= input.length)
            throw new IllegalArgumentException("N must be lower than the length of the input");
        int from = 0, to = input.length - 1;

        while (from < to) 
        {
            int leftIndex = from, rightIndex = to;
            Map.Entry<V, Double> mid = input[(leftIndex + rightIndex) / 2];
            
            while (leftIndex < rightIndex) 
            {
                /*
                if the value is greater than the pivot move it on the right 
                side by swapping it with the value at rightIndex, else move on
                */
                if (input[leftIndex].getValue() <= mid.getValue()) 
                { 
                    Map.Entry<V, Double> tmp = input[rightIndex];
                    input[rightIndex] = input[leftIndex];
                    input[leftIndex] = tmp;
                    rightIndex--;
                } 
                else
                    leftIndex++;
            }
            if (input[leftIndex].getValue() < mid.getValue())
                leftIndex--;
            //change to or from depending if what we are looking for is on the left or right part
            if (n <= leftIndex) 
                to = leftIndex;
            else 
                from = leftIndex + 1;
        }
    }
}
