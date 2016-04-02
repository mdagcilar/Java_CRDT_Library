package crdt.sets;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;

import static org.junit.Assert.*;


public class TwoPhaseSetTest {

    @Test
    public void testAdd() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");

        assertEquals( newHashSet("a", "b", "c"), replica1.getSetMinus());
    }

    @Test
    public void testRemove() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");
        replica1.remove("b");

        assertEquals(newHashSet("a", "c"), replica1.getSetMinus());
    }

    @Test
    public void testRemove_removeNonExistentElement() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("c");

        assertEquals(newHashSet("a", "c"), replica1.getSetMinus());

        replica1.remove("b");
        replica1.add("b");
        assertEquals(newHashSet("a", "b", "c"), replica1.getSetMinus());
    }

    /**
     * *********** Testing different Merging scenarios *************************
     */
    @Test
    public void testMerge() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();
        TwoPhaseSet<String> replica2 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals( newHashSet("a", "b", "c"), replica1.getSetMinus());
        assertEquals( newHashSet("a", "b", "c"), replica2.getSetMinus());


        replica1.remove("b");
        replica2.add("d");
        replica2.add("e");

        replica1.merge(replica2);
        replica2.remove("d");

        replica2.merge(replica1);
        replica1.merge(replica2);

        assertEquals( newHashSet("a", "c", "e"), replica1.getSetMinus());
        assertEquals( newHashSet("a", "c", "e"), replica2.getSetMinus());
    }

    /**
     * Replica 3 never merges with 1 or 2. So Sets 1&2 do not contain 'f'. 'b' is still in the set
     * because replica3 tried to remove an element that didn't exist in their local copy. Cannot remove
     * elements that do not exist.
     */
    @Test
    public void testMerge_threeReplicas() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();
        TwoPhaseSet<String> replica2 = new TwoPhaseSet<String>();
        TwoPhaseSet<String> replica3 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");
        replica2.add("d");
        replica3.add("a");
        replica3.add("f");

        replica3.remove("b");

        replica1.merge(replica2);
        replica2.merge(replica1);
        replica3.merge(replica1);

        assertEquals( newHashSet("a", "b", "c", "d"), replica1.getSetMinus());
        assertEquals( newHashSet("a", "b", "c", "d"), replica2.getSetMinus());
        assertEquals( newHashSet("a", "b", "c", "d", "f"), replica3.getSetMinus());
    }

    @Test
    public void testGetSetMinus() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");
        replica1.remove("b");
        replica1.remove("a");

        assertEquals( newHashSet("c"), replica1.getSetMinus());
    }

    @Test
    public void testCopy() throws Exception {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");

        assertEquals( newHashSet("a", "b", "c"), replica1.copy().getSetMinus());
    }
}