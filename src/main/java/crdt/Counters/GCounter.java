package crdt.Counters;

import crdt.CRDT;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.Math.max;

/**
    A state-based Counter that can only increment. Can be used as a building block
    for more complex data structures like PN (positive-negative) counters.

    A GCounter is a state-based counter that can only increment. A HashMap acts as a collection of keys and values
    to map replicas to their unique integer. Executing the increment method can only increase the value
    associated with that replica by 1. The value method, iterates through the entire HashMap and returns
    the sum of all the values in the collection.
 */
public class GCounter<T> implements CRDT<GCounter<T>> {

    private Map<T, Integer> counts = new HashMap<T, Integer>();

    /**
    Increment a replica with a given key by 1
     */
    public void increment(T key){
        Integer count = counts.get(key);
        //if count is null, initialize to 0
        if(count == null)
            count = 0;

        counts.put(key, count + 1);
    }

    /**
     Query returns the value of the sum of all counters in the HashMap.
     */
    public int value(){
        int sum = 0;

        for(int count: counts.values())
            sum += count;
        return sum;
    }

    /**
     * Merge() converges the passed in CRDT with the current CRDT. The argument CRDT does not see any changes, only the object that the method is invoked on.
     * Hashmap.entrySet() is a method used to return a Set view of the mappings contained in the current HashMap.
    */
     public void merge(GCounter<T> otherReplica) {

        //iterate through the second replicas keys.
        for(Entry<T, Integer> e: otherReplica.counts.entrySet()) {
            T key = e.getKey();

            if( counts.containsKey(key) )
                counts.put(key, max(e.getValue(), counts.get(key)) );
            else
                counts.put(key, e.getValue());
        }
    }


    public String toString(){
        return "GCounter{" + counts + "}";
    }

    /**
    Creates a copy of the current GCounter CRDT and
     */
    public GCounter<T> copy() {
        GCounter<T> copy = new GCounter<T>();
        copy.counts = new HashMap<T, Integer>(counts);

        return copy;
    }
}