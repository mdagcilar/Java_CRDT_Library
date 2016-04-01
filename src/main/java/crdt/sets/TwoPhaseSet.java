package crdt.sets;

import crdt.CRDT;

import java.util.LinkedHashSet;
import java.util.Set;

/**
    A 2PSet - A set for adding and removing elements in a Set.
    Built on top of the GSet which only allows adding elements.

    Combining 2 GSets, one for adds, other for removals. Allows elements
    to be added and removed, but once removed they can no longer be re-added.
    The second GSet for removals, acts as a Tombstone set that stores all the removed elements.
 */
public class TwoPhaseSet<T> implements CRDT<TwoPhaseSet<T>> {

    public GSet<T> added = new GSet<T>();
    public GSet<T> removed = new GSet<T>();


    /**
        Add an element to the 'added' set. An element can only be added if it isn't in the tombstone 'removed' set.
     */
    public void add(T element) {
        if(removed.contains(element))
            throw new IllegalArgumentException("Element has already been removed, and is stored in the tombstone set");
        added.add(element);
    }

    /**
        To avoid removing elements that do not exist. We first check that the element is in the 'added' set.
        Then it is added to the tombstone set.
     */
    public void remove(T element){
        if(added.contains(element))
            removed.add(element);
    }
    /**
        The get method returns a Set containing all the elements in the added set that are not in the removed set.

        Using a LinkedHashSet because it maintains a linked list of the entries in the set, in the order in which they were inserted.
    */
    public Set<T> getSetMinus() {
        Set<T> addedSet = new LinkedHashSet<T>( added.get());
        addedSet.removeAll(removed.get() );
        return addedSet;
    }

    /**
        Merges the passed in set with the current set.
        1- Merge the GSet that contains all the added elements because Set Union is commutative
        2- Merge the GSet that contains all the removed elements (tombstone set) because again Set Union is commutative
     */
    public void merge(TwoPhaseSet<T> TwoPSet) {
        added.addAll(TwoPSet.added.get());
        removed.addAll(TwoPSet.removed.get());
    }


    /**
        The copy method creates a new TwoPhaseSet and initialises the two GSets (added and removed)
        by using the copy() methods in the GSet class to clone all the elements.
     */
    public TwoPhaseSet<T> copy() {
        TwoPhaseSet<T> copy = new TwoPhaseSet<T>();

        copy.added = added.copy();  //copy() is the GSet's method copy.
        copy.removed = removed.copy();
        return copy;
    }
}
