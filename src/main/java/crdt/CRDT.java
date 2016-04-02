package crdt;

import java.io.Serializable;

public interface CRDT<T extends CRDT<T>> extends Serializable {
    /**
     * Merge this CRDT with another of the same type.
     */
    void merge(T other);

    /**
     * Create a copy of this CRDT
     */
    T copy();
}
