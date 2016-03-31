package crdt.sets;


import crdt.CRDT;

import java.util.*;

public class GSet<T> implements CRDT<GSet<T>> {

//  A LinkedHashSet maintains a linked list of the entries in the set, in the order in which they were inserted.
    private Set<T> elements = new LinkedHashSet<T>();

    //    return true if the set contains the element passed in
    public boolean contains(T element)
    {
        return elements.contains(element);
    }


    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    //    add an element to the set.
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

    public void printLinkedHashSet()
    {
        Iterator<T> itr = elements.iterator();
        while(itr.hasNext()){
            T t = itr.next();
            System.out.println(t + " Hashcode " + t.hashCode());
        }
    }
}
