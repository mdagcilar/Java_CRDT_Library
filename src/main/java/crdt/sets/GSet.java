package crdt.sets;

import crdt.CRDT;
import java.util.*;

public class GSet<T> implements CRDT<GSet<T>> {

    /**
     *  A HashSet to manage the added elements.
     */
    private Set<T> elements = new HashSet<T>();

    /**
     * This method is implemented, so that an instance of GSet can call the contains method on the HashSet.
     * This method is used by the TwoPhaseSet.java class.
     * @param element to check
     * @return true if set contains element.
     */
    public boolean contains(T element)
    {
        return elements.contains(element);
    }


    /**
     * add an element to the set.
     * @param element the element to be added.
     */
    public void add(T element)
    {
        elements.add(element);
    }

    public void addAll(Collection<T> addingElement)
    {
        elements.addAll(addingElement);
    }

    public Set<T> get() {
        return Collections.unmodifiableSet(elements);
    }

    /**
     * Merge two GSet CRDTs into the current CRDT (set) by Union which is commutative.
     * @param set the set to merge with.
     */
    public void merge(GSet<T> set) {
        elements.addAll(set.elements);
    }

    public GSet<T> copy(){
        GSet<T> copy = new GSet<T>();

        copy.elements = new HashSet<T>(elements);
        return copy;
    }

    public T getElement(int index)
    {
        List<T> nameList = new ArrayList<T>(elements);
        return nameList.get(index);
    }
}
