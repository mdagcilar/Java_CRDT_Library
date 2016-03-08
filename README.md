# Java_CRDT_Library

#What is a CRDT?
CRDTs are inspired by the work of Marc Shapiro, Preguiça, Baquero, and Zawirski. In distributed computing, a conflict-free replicated data type (abbreviated CRDT) is a type of specially-designed data structure
used to achieve strong eventual consistency (SEC) and monotonicity (absence of rollbacks). There are two alternative routes to ensuring SEC: operation-based CRDTs  and state-based CRDTs.
Simple Conflict-free Replicated Data Types (CRDTs) for distributed systems. CRDTs on different replicas can diverge from one another but at the end they can be safely merged providing an eventually consistent value.
In other words, CRDTs have a merge method that is idempotent, commutative and associative.

The two alternatives are equivalent, as one can emulate the other, but operation-based CRDTs require additional guarantees from the communication middleware.
CRDTs are used to replicate data across multiple computers in a network, executing updates without the need for remote synchronization.
This would lead to merge conflicts in systems using conventional eventual consistency technology, but CRDTs are designed such that conflicts are mathematically impossible.
Under the constraints of the CAP theorem they provide the strongest consistency guarantees for available/partition-tolerant (AP) settings.

#State-based vs Operation-based



#Counter (State-based) G-Counter
A state-based counter is not as straightforward as one would expect. To simplify the problem, we start with a Counter that only increments.

Suppose the payload was a single integer and merge computes max. This data type is a CvRDT as its states form a monotonic semilattice.
Consider two replicas, with the same initial state of 0; at each one, a client originates increment. They converge to 1 instead of the expected 2.

The first vector is the P Counter. It holds all the increments for the counter. The second vector is the N Counter, it holds all the decrements.
Each replica only increments its own entry in the vector. The value of the counter is the difference between the sum of the P Counter and the sum of the N Counter.

Examples
===========
PNCounter:
```java
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");System.out.println("Replica1 value =" + replica1.value());
        replica1.increment("hostname1");System.out.println("Replica1 value =" + replica1.value());

        replica2.increment("hostname2");System.out.println("\nReplica2 value =" + replica2.value());
        replica2.increment("hostname2");System.out.println("Replica2 value =" + replica2.value());

        replica1.merge( replica2 );

        System.out.println("\nReplica1 value after merge = " + replica1.value());
```

Output for PNCounter code:
**********************************

Replica1 value =1

Replica1 value =2

Replica2 value =1

Replica2 value =2

Replica1 value after merge = 4

**********************************