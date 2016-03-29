package crdt.sets;


import crdt.CRDT;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class GSet<T> implements CRDT<GSet<T>> {

//  A LinkedHashSet maintains a linked list of the entries in the set, in the order in which they were inserted.
    private Set<T> elements = new LinkedHashSet<T>();

//    add an element to the set.
    public void add(T element){
        elements.add(element);
    }

    public Set<T> get() {
        return Collections.unmodifiableSet(elements);
    }

    /*
    Merge two CRDT's into the current CRDT (set).
    */
    public void merge(GSet<T> set) {
        elements.addAll(set.elements);
    }

//    return true if the set contains the element passed in
    public boolean contains(T element)
    {
        return elements.contains(element);
    }


    public void addAll(Collection<T> addingElement) {
        elements.addAll(addingElement);
    }

    public GSet<T> copy(){
        GSet<T> copy = new GSet<T>();

        copy.elements = new LinkedHashSet<T>(elements);
        return copy;
    }
}
