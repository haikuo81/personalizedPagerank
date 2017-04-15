package UtilityTesting;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Random;
import junit.framework.TestCase;
import personalizedpagerank.Utility.PartialSorter;

public class SorterTest extends TestCase
{
    
    PartialSorter<Int2DoubleOpenHashMap.Entry> sorter = new PartialSorter<>();
    
    public void testEmpty()
    {
        
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        Int2DoubleMap.Entry[] values = map.entrySet().toArray(new Int2DoubleMap.Entry[0]);
        try
        {
            sorter.partialSort(values, 0, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? -1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()? 0 : 1;
            });
            fail("this line shouldn't be reached");
        }
        catch(Exception e){}
    }
    
    public void testLTooLarge()
    {
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        map.put(0, 0d);
        map.put(1, 0d);
        map.put(2, 0d);
        Int2DoubleMap.Entry[] values = map.entrySet().toArray(new Int2DoubleMap.Entry[0]);
        try
        {
            sorter.partialSort(values, map.size(), (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? -1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()? 0 : 1;
            });
            fail("this line shouldn't be reached");
        }
        catch(Exception e){}
    }
    
    public void testShuffleAndCheckPartialOrder()
    {
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        int n = 200;
        Int2DoubleMap.Entry[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                 map.put(i, i + 0d);
            values = map.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            shuffleArray(values);
            sorter.partialSort(values, number, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? 1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()? 0 : -1;
            });
            double val = values[number].getDoubleValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getDoubleValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getDoubleValue() <= val);
        }
    }
    
    public void testShuffleAndCheckPartialOrderWithRandomInts()
    {
        //testing with random "ints" to have some chance of equal values
        Random random = new Random();
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        int n = 200;
        Int2DoubleMap.Entry[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                map.put(i, random.nextInt(n) + 0d);
            values = map.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            shuffleArray(values);
            
            sorter.partialSort(values, number, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? 1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()? 0 : -1;
            });
            
            double val = values[number].getDoubleValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getDoubleValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getDoubleValue() <= val);
            
        }
    }
    
    public void testShuffleAndCheckPartialOrderWithRandomDoubles()
    {
        Random random = new Random();
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        int n = 200;
        Int2DoubleMap.Entry[] values;
        
        //for each number (position) shuffle the array, partially sort it and check if result is correct
        for(int number = 0; number < n; number++)
        {
            //populate and shuffle
            map.clear();
            for(int i = 0; i < n; i++)
                map.put(i, random.nextDouble());
            values = map.entrySet().toArray(new Int2DoubleMap.Entry[0]);
            shuffleArray(values);
            
            sorter.partialSort(values, number, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2)->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? 1 : 
                    e1.getDoubleValue() == e2.getDoubleValue()? 0 : -1;
            });
            double val = values[number].getDoubleValue();
            //check that values on left are greater or equal by moving from 0 to nth
            for(int left = 0; left < number; left++)
                assertTrue(values[left].getDoubleValue() >= val);
            //check that values on the right are lower or equal by moving from nth to the end
            for(int right = number; right < map.size(); right++)
                assertTrue(values[right].getDoubleValue() <= val);
        }
    }
    
    
    private static void shuffleArray(Int2DoubleMap.Entry[] array)
    {   
        int index;
        Int2DoubleMap.Entry temp;
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
