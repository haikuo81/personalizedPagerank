/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package personalizedpagerank.Utility;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Map;

public class NodeScores extends Int2DoubleOpenHashMap 
{
    private static final PartialSorter<Int2DoubleOpenHashMap.Entry> SORTER = new PartialSorter<>();

    public NodeScores()
    {
        super();
    }
    
    public NodeScores(int expected)
    {
        super(expected);
    }
    
    public NodeScores(Map<? extends Integer, ? extends Double> map)
    {
        super(map);
    }

    /**
     * Keeps the topL entries of the map, based on a partial order on the Lth element.
     * @param topL How many elements to keep from the top.
     */
    public void keepTop(final int topL)
    {
        if(size > topL)
        {
            Int2DoubleMap.Entry[] array = this.int2DoubleEntrySet().toArray(new Int2DoubleMap.Entry[0]);

            SORTER.partialSort(array, topL, (Int2DoubleMap.Entry e1, Int2DoubleMap.Entry e2) ->
            {
                return e1.getDoubleValue() < e2.getDoubleValue()? 1 : 
                        e1.getDoubleValue() == e2.getDoubleValue()?
                        (e1.getIntKey() < e2.getIntKey()? -1 : 1) : -1;  
            });
            //if too many to remove just clear and add the first topL
            //else just remove the non topL
            if(array.length > topL * 2)
            {
                //res is needed as a temporary holder since doing this.clear() will remove keys from array
                Int2DoubleOpenHashMap res = new Int2DoubleOpenHashMap(topL);
                for(int i = 0; i < topL; i++)
                    res.put(array[i].getIntKey(), array[i].getDoubleValue());
                this.clear();
                this.putAll(res);
            }
            else
            {
                //needs a temporary holder since changes in the map are reflected in the Map.Entry[]
                int[] toRemove = new int[array.length - topL];
                for(int i = topL, index = 0; i < array.length; i++, index++)
                    toRemove[index]= array[i].getIntKey();
                for(int i = 0; i < toRemove.length; i++)
                    this.remove(toRemove[i]);
            }
        }
    }
}
