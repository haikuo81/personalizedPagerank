package personalizedpagerank.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import org.jgrapht.alg.util.Pair;

/**
 * https://en.wikipedia.org/wiki/Kendall_rank_correlation_coefficient
 * Class for calculating the Kendall correlation coefficient with possible
 * ties for values in X or Y, implementation of the algorithm from
 *   A Computer Method for Calculating Kendall's Tau with Ungrouped Data
 *   William R. Knight,
 * If the values have no possible ties and the input length is short it's better
 * to use the O(n^2) method.
 * Arrays with length that would cause (length * (length -1)) to overflow
 * as a double will lead to incorrect results. No checks or corrections for this 
 * have been made since in this project this class is called many times but with
 * small/very small values.
 */
public class Kendall 
{
   private Kendall(){};
   
   public static double correlation(ArrayList<Double> x, ArrayList<Double> y, boolean sortedX)
   {
       if(x.size() != y.size())
           throw new IllegalArgumentException("the two arrays must have same length");
       if(x.isEmpty() || y.isEmpty())
           throw new IllegalArgumentException("length of arrays must be at least 1");
           
       //make array of pairs for easier handling
       Pair<Double, Double>[] pairs = new Pair[x.size()]; 
       for(int i = 0; i < x.size(); i++)
           pairs[i] = new Pair<>(x.get(i), y.get(i));
       
       if(!sortedX)
           Arrays.sort(pairs, (Pair<Double, Double> p1, Pair<Double, Double> p2) ->
           {
               if(p1.getFirst() < p2.getFirst())
                   return - 1;
               if(p1.getFirst() > p2.getFirst())
                   return 1;
               if(p1.getSecond() < p2.getSecond())
                   return -1;
               if(p1.getSecond() > p2.getSecond())
                   return 1;
               return 0;
           });
       
       //accounting for ties by same X or same X and Y 
       double sameX = 0;
       double sameXY = 0;
       double consecutiveSameX = 1;
       double consecutiveSameXY = 1;
       Pair<Double, Double> old = pairs[0];
       for(int i = 1; i < pairs.length; i++) 
       {
           //if same X increment number of consecutive pairs with same X
           if (pairs[i].getFirst().equals(old.getFirst())) 
           {
               consecutiveSameX++;
               /*
               if also same Y increment number of consecutive equal pairs
               else reset consecutive equal pairs to 1 after updating the value
               of the same pairs (sameXY)
               */
               if(pairs[i].getSecond().equals(old.getSecond()))
                   consecutiveSameXY++;
               else
               {
                   // (n * (n -1))/2
                   sameXY += (consecutiveSameXY * (consecutiveSameXY -1d))/2d;
                   consecutiveSameXY = 1d;
               }
           }
           else
           {
               /*
                if they haven't the same X value:
               -update value of pairs with the same X
               -reset consecutiveSameX to 1
               -update value of equal pairs
               -reset consecutiveSameXY to 1
               */
               sameX += (consecutiveSameX * (consecutiveSameX - 1d))/2d;
               consecutiveSameX = 1d;
               
               sameXY += (consecutiveSameXY * (consecutiveSameXY -1d))/2d;
               consecutiveSameXY = 1d;
           } 
           old = pairs[i];
       }
       //(needed if all the values are equal)
       sameX += (consecutiveSameX * (consecutiveSameX - 1d))/2d;
       sameXY += (consecutiveSameXY * (consecutiveSameXY -1d))/2d;
       
       //get number of swaps needed and the pairs ordered by Y
       Pair<Long, Pair<Double,Double>[]> disc = getDiscording(pairs);
       double discording = disc.getFirst();
       pairs = disc.getSecond();
      
       //accounting for ties for same Y 
       double sameY = 0;
       double consecutiveSameY = 1;
       old = pairs[0];
       for(int i = 1; i < pairs.length; i++)
       {
           if(pairs[i].getSecond().equals(old.getSecond()))
               consecutiveSameY++;
           else
           {
               sameY += (consecutiveSameY * (consecutiveSameY -1d))/2d;
               consecutiveSameY = 1d;
           }
           old = pairs[i];
       }
       sameY += (consecutiveSameY * (consecutiveSameY -1d))/2d;
       
       //return correlation
       //https://en.wikipedia.org/wiki/Kendall_rank_correlation_coefficient
       //see Tau-b
       //conversion to double needed to avoid overflow (might still overflow with 
       //extremely high lengthts), note that a conversion happens with 
       //(pairs.lenth - 1d)
       double totalPairs = ((pairs.length * (pairs.length - 1d))/2d);
       double num = totalPairs - sameX - sameY + sameXY - 2 * discording;
       double den = Math.sqrt((totalPairs - sameX) * (totalPairs - sameY));
       
       return (den == 0d)? (sameX == sameY? 1d : 0) : num/den;
   }
   
   /**
    * Given an array of pairs return the number of swaps needed by merge to
    * sort the array and the array sorted by the y value of the pairs.
    * @param Array of pairs.
    * @return A pair consisting of the number of swaps needed and the array
    * sorted by the y value of the pairs.
    */
   private static Pair<Long, Pair<Double, Double>[]> getDiscording(Pair<Double, Double>[] pairs)
   {
       long discording = 0;

       Pair<Double, Double>[] holder = new Pair[pairs.length];

       /*
       non recursive merge sort
       start from chunks of size 1 to n, merge (and count swaps)
       */
       for(int chunk = 1; chunk < pairs.length; chunk *= 2)
       {
           //take 2 sorted chunks and make them one sorted (double the size) chunk
           for(int startChunk = 0; startChunk < pairs.length; startChunk += 2 * chunk)
           {
               //start and end of the left half
               int startLeft = startChunk;
               int endLeft = Math.min(startLeft + chunk, pairs.length);
               
               //start and end of the right half
               int startRight = endLeft;
               int endRight = Math.min(startRight + chunk, pairs.length);
               
               //merge the 2 halfs
               //index is used to point to the right place in the holder array
               int index = startLeft;
               for(;startLeft < endLeft && startRight < endRight; index++)
               {
                   /*
                   if the pairs (ordered by X) discord when checked by Y
                   increment the number of discording pairs by 1 for each
                   remaining pair on the left half, because if the pair on the right
                   half discords with the pair on the left half it surely discords
                   with all the remaining pairs on the left half, since they all
                   have a Y greater than the Y of the left half pair currently
                   being checked
                   */
                   if(pairs[startLeft].getSecond() > pairs[startRight].getSecond())
                   {
                       holder[index] = pairs[startRight];
                       startRight++;
                       discording += endLeft - startLeft;
                   }
                   else
                   {
                       holder[index] = pairs[startLeft];
                       startLeft++;
                   }
               }
               
               /*
               if the left half is over there are no more discording pairs in this
               chunk, the remaining pairs in the right half can be copied
               */
               for(;startRight < endRight; startRight++, index++)
                   holder[index] = pairs[startRight];
               /*
               if the right half is over (and the left one is not) all the
               discording pairs have been accounted for already
               */
               for(;startLeft < endLeft; startLeft++, index++)
                   holder[index] = pairs[startLeft];
           }
           Pair<Double, Double>[] tmp = pairs;
           pairs = holder;
           holder = tmp;
       }
       return new Pair<>(discording, pairs);
   }
}
