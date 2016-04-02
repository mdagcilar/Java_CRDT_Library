package crdt.sets;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;


import static org.junit.Assert.*;


public class GSetTest {

    @Test
    public void testAdd_whenElementsAdded() throws Exception {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");
        gSet.add("c");

        assertEquals( newHashSet("a", "b", "c"), gSet.get());
    }

    @Test
    public void testAdd_addingDuplicates() throws Exception {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");
        gSet.add("b");

        assertEquals( newHashSet("a", "b"), gSet.get());
    }

    @Test
    public void testMerge() throws Exception {
        GSet<String> replica1 = new GSet<String>();
        GSet<String> replica2 = new GSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica2.add("c");
        replica2.add("d");

        replica1.merge(replica2);

        assertEquals( newHashSet("a", "b", "c", "d"), replica1.get());
    }

    @Test
    public void testMerge_duplicateElements() throws Exception {
        GSet<String> replica1 = new GSet<String>();
        GSet<String> replica2 = new GSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica2.add("a");
        replica2.add("b");

        replica1.merge(replica2);

        assertEquals( newHashSet("a", "b"), replica1.get());
    }

    @Test
    public void testCopy() throws Exception {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");

        assertEquals(newHashSet("a", "b"), gSet.copy().get());
    }

    @Test
    public void testGetElement() throws Exception {
        GSet<String> gSet = new GSet<String>();

        gSet.add("a");
        gSet.add("b");

        assertEquals( ("b"), gSet.getElement(1));
    }
}