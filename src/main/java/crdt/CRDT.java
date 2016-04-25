package crdt;

public interface CRDT<T>{
    /**
     * Merge this CRDT with another of the same type.
     */
    void merge(T other);

    /**
     * Create a copy of this CRDT
     */
    T copy();
}
