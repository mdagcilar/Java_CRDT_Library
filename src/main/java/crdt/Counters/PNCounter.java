package crdt.Counters;

import crdt.CRDT;

/**
 * Created by Metin on 25/02/2016.
 */
public class PNCounter<T> implements CRDT<PNCounter<T>> {

	private GCounter<T> increments = new GCounter<T>();
	private GCounter<T> decrements = new GCounter<T>();


	public void inc(T key) {
		increments.increment(key);
	}

	public void dec(T key) {
		decrements.increment(key);
	}

	public int get() {
		return increments.value() - decrements.value();
	}


	/*
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
}
