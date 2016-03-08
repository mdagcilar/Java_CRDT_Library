package crdt;

/*
This class Implements the Serializable interface, which is basically just a marker interface to say this class is okay to be serialized
To serialize an object means to convert its state to a byte stream so that the byte stream can be reverted back into a copy of the object.
 */
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
