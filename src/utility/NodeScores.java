package utility;

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
    
    /**
     * Given an input map adds all the entries of that map to this map.
     * Equivalent to doing addTo(key, value) for each entry of the input map.
     * The input map is not modified.
     * @param from Input map which entries will be added.
     */
    public void add(NodeScores from)
    {
        for(Int2DoubleMap.Entry entry: from.int2DoubleEntrySet())
            this.addTo(entry.getIntKey(), entry.getDoubleValue());
    }
    
    /**
     * Given an input map adds all the entries of that map to this map, correcting
     * the values of the input entries with a factor (doing a product).
     * Equivalent to doing addTo(key, value * factor) for each entry of the input map.
     * The input map is not modified.
     * @param from Input map which entries will be added.
     * @param factor Value used to multiply the input entries values before adding them.
     */
    public void add(NodeScores from, double factor)
    {
        for(Int2DoubleMap.Entry entry: from.int2DoubleEntrySet())
            this.addTo(entry.getIntKey(), entry.getDoubleValue() * factor);
    }
    
    /**
     * Add a value to all the values in the map.
     * @param increment Value used to increment all other values.
     */
    public void addToAll(double increment)
    {
        for(Int2DoubleMap.Entry entry: this.int2DoubleEntrySet())
            entry.setValue(entry.getDoubleValue() + increment);
    }
  
    /**
     * Multiply all values in the map for a multiplier.
     * @param multiplier Value used to multiply all other values.
     */
    public void multiplyAll(double multiplier)
    {
        for(Int2DoubleMap.Entry entry: this.int2DoubleEntrySet())
            entry.setValue(entry.getDoubleValue() * multiplier);
    }
    
    /**
     * Calculates the norm1 between this map and another one, if a value isn't part
     * of a map while it's part of the other it's value in the first map is the
     * defeaultReturnValue of that map (by default 0).
     * @param other 
     * @return 
     */
    public double norm1(NodeScores other)
    {
        double res = 0;
        for(int k: this.keySet())
            res += Math.abs(get(k) - other.get(k));
        //need to check for keys that aren't part of this map
        for(int k: other.keySet())
            if(!this.containsKey(k))
                res += other.get(k);
        return res;
    }
}
