package crdt.sets;

import crdt.CRDT;
import java.util.*;

public class GSet<T> implements CRDT<GSet<T>> {

    /**
     *  A LinkedHashSet maintains a linked list of the entries in the set, in the order in which they were inserted.
     */
    private Set<T> elements = new LinkedHashSet<T>();

    /**
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
     * Merge two GSet CRDT's into the current CRDT (set) by Union which is commutative.
     * @param set the set to merge with.
     */
    public void merge(GSet<T> set) {
        elements.addAll(set.elements);
    }

    public GSet<T> copy(){
        GSet<T> copy = new GSet<T>();

        copy.elements = new LinkedHashSet<T>(elements);
        return copy;
    }

    public T getElement(int index)
    {
        List<T> nameList = new ArrayList<T>(elements);
        return nameList.get(index);
    }
}
