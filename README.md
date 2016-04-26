#A State-based CRDT library for Convergent data structures

# What is a CRDT?

CRDTs are inspired by the work of Marc Shapiro, Preguiça, Baquero, and Zawirski. In distributed computing, a conflict-free replicated data type (abbreviated CRDT) is a type of specially-designed data structure
used to achieve strong eventual consistency (SEC) and monotonicity (absence of rollbacks). There are two alternative routes to ensuring SEC: operation-based CRDTs and state-based CRDTs.
Simple Conflict-free Replicated Data Types (CRDTs) for distributed systems. CRDTs on different replicas can diverge from one another but at the end they can be safely merged providing an eventually consistent value.
In other words, CRDTs have a merge method that is idempotent, commutative and associative.

The two alternatives are equivalent, as one can emulate the other, but operation-based CRDTs require additional guarantees from the communication middleware.
CRDTs are used to replicate data across multiple computers in a network, executing updates without the need for remote synchronization.
This would lead to merge conflicts in systems using conventional eventual consistency technology, but CRDTs are designed such that conflicts are mathematically impossible.
Under the constraints of the CAP theorem they provide the strongest consistency guarantees for available/partition-tolerant (AP) settings.

#The following CRDTs are implemented in this library
- GCounter
- PNCounter
- GSet
- TwoPhaseSet
- add-wins Graph


============================
PNCounter:
```java  
@Test
    public void testAdd_whenElementsAdded() {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");
        gSet.add("c");

        assertEquals( newHashSet("a", "b", "c"), gSet.get());
    }
    
    
@Test
    public void testAdd_addingDuplicates() {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");
        gSet.add("b");

        assertEquals( newHashSet("a", "b"), gSet.get());
    }
```
