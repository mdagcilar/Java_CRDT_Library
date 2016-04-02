package crdt.Counters;

import crdt.CRDT;

public class PNCounter<T> implements CRDT<PNCounter<T>> {

	private GCounter<T> increments = new GCounter<T>();
	private GCounter<T> decrements = new GCounter<T>();


	public void increment(T key) {
		increments.increment(key);
	}

	public void decrement(T key) {
		decrements.increment(key);
	}

	public int value() {
		return increments.value() - decrements.value();
	}


	/**
    Merge two CRDT's and return one CRDt.
    */
	public void merge(PNCounter<T> otherReplica){
		increments.merge( otherReplica.increments);
		decrements.merge( otherReplica.decrements);
	}

	public PNCounter<T> copy(){
		PNCounter<T> copy = new PNCounter<T>();
		copy.increments = increments.copy();
		copy.decrements = decrements.copy();
		return copy;
	}
}
