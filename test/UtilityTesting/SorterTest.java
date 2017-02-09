package UtilityTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import junit.framework.TestCase;
import personalizedpagerank.Utility.PartialSorter;

public class SorterTest extends TestCase
{
    
    PartialSorter<Integer> sorter = new PartialSorter();
    
    public void testEmpty()
    {
        
        HashMap<Integer, Double> map = new HashMap<>();
        Map.Entry<Integer, Double>[] values = map.entrySet().toArray(new Map.Entry[0]);
        try
        {
            sorter.partialSort(values, 0);
            fail("this line shouldn't be reached");
        }
        catch(Exception e){}
    }
    
    public void testLTooLarge()
    {
        HashMap<Integer, Double> map = new HashMap<>();
        map.put(0, 0d);
        map.put(1, 0d);
        map.put(2, 0d);
        Map.Entry<Integer, Double>[] values = map.entrySet().toArray(new Map.Entry[0]);
        try
        {
            sorter.partialSort(values, map.size());
            fail("this line shouldn't be reached");
        }
        catch(Exception e){}
    }
    
    public void testShuffleAndCheckPartialOrder()
    {
        HashMap<Integer, Double> map = new HashMap<>();
        int n = 200;
        Map.Entry<Integer, Double>[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                 map.put(i, i + 0d);
            values = map.entrySet().toArray(new Map.Entry[0]);
            shuffleArray(values);
            
            sorter.partialSort(values, number);
            Double val = values[number].getValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getValue() <= val);
        }
    }
    
    public void testShuffleAndCheckPartialOrderWithRandomInts()
    {
        //testing with random "ints" to have some chance of equal values
        Random random = new Random();
        HashMap<Integer, Double> map = new HashMap<>();
        int n = 200;
        Map.Entry<Integer, Double>[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                map.put(i, random.nextInt(n/200) + 0d);
            values = map.entrySet().toArray(new Map.Entry[0]);
            shuffleArray(values);
            
            sorter.partialSort(values, number);
            Double val = values[number].getValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getValue() <= val);
            
        }
    }
    
    public void testShuffleAndCheckPartialOrderWithRandomDoubles()
    {
        Random random = new Random();
        HashMap<Integer, Double> map = new HashMap<>();
        int n = 200;
        Map.Entry<Integer, Double>[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                map.put(i, random.nextDouble());
            values = map.entrySet().toArray(new Map.Entry[0]);
            shuffleArray(values);
            
            sorter.partialSort(values, number);
            Double val = values[number].getValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getValue() <= val);
        }
    }
    
    
    private static void shuffleArray(Map.Entry<Integer, Double>[] array)
    {   
        int index;
        Map.Entry<Integer, Double> temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }   
    
}
