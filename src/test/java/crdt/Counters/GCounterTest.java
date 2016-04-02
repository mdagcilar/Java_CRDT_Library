package crdt.Counters;

import org.junit.Test;

import static org.junit.Assert.*;

public class GCounterTest {

    @Test
    public void testIncrement() throws Exception {
        GCounter<String> gCounter = new GCounter<String>();

        gCounter.increment("hostname1");

        assertEquals(1, gCounter.value());
    }

    @Test
    public void testIncrement_multipleIncrements() throws Exception {
        GCounter<String> gCounter = new GCounter<String>();

        gCounter.increment("hostname1");
        gCounter.increment("hostname1");
        gCounter.increment("hostname1");
        gCounter.increment("hostname1");

        assertEquals(4, gCounter.value());
    }

    @Test
    public void testIncrement_multipleCounters() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica1.increment("hostname1");
        replica2.increment("hostname2");

        assertEquals(2, replica1.value());
        assertEquals(1, replica2.value());
    }

    @Test
    public void testCopy() throws Exception {
        GCounter<String> gCounter = new GCounter<String>();

        gCounter.increment("hostname1");
        gCounter.increment("hostname1");

        assertEquals(2, gCounter.value());
        assertEquals(2, gCounter.copy().value());
    }

    @Test
    public void testValue() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica1.increment("hostname1");
        replica2.increment("hostname2");
        replica2.increment("hostname2");

        assertEquals(2, replica1.value());
        assertEquals(2, replica2.value());
    }


    /** ***************Testing different merge scenarios **********
     *
     * Merge two different users updating different Keys in the Counter.
     */
    @Test
    public void testMerge() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname2");

        replica1.merge(replica2);

        assertEquals(2, replica1.value());
        assertEquals(1, replica2.value());
    }

    /**
     * Merge both replicas and check their value is the same.
     */
    @Test
    public void testMerge_mergeBothReplicas() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname2");

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals(2, replica1.value());
        assertEquals(2, replica2.value());
    }

    /**
     * Merging two replicas that have updated the same key. This should not happen
     * since each user should only update their own key. We catch this error by
     * taking the Max of the two values. Which disregards duplicate increments.
     */
    @Test
    public void testMerge_updatingSameKey() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname1");

        replica1.merge(replica2);

        assertEquals(1, replica1.value());
        assertEquals(1, replica2.value());
    }

    /**
     * Merging at different stages.
     */
    @Test
    public void testMerge_differentKeys() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("replica1");
        replica1.increment("replica1");
        replica2.increment("replica2");

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals(3, replica1.value());

        replica2.increment("replica2");
        replica2.increment("replica2");
        assertEquals(5, replica2.value());
    }

    /**
     * Multiple merges has no effect.
     */
    @Test
    public void testMerge_multipleMerges() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname2");

        replica1.merge(replica2);
        replica1.merge(replica2);

        assertEquals(2, replica1.value());
        assertEquals(1, replica2.value());

    }

    /**
     * Testing merge for 3 replicas.
     */
    @Test
    public void testMerge_threeReplicas() throws Exception {
        GCounter<String> replica1 = new GCounter<String>();
        GCounter<String> replica2 = new GCounter<String>();
        GCounter<String> replica3 = new GCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname2");
        replica3.increment("hostname3");

        //merge 1 and 2
        replica1.merge(replica2);
        assertEquals(2, replica1.value());
        assertEquals(1, replica2.value());
        assertEquals(1, replica3.value());

        //merge 2 and 3
        replica2.merge(replica3);
        assertEquals(2, replica1.value());
        assertEquals(2, replica2.value());
        assertEquals(1, replica3.value());

        //merge 3 and 1
        replica3.merge(replica1);

        assertEquals(2, replica1.value());
        assertEquals(2, replica2.value());
        assertEquals(3, replica3.value());

        //merge the remaining replicas
        replica1.merge(replica3);
        replica2.merge(replica1);

        assertEquals(3, replica1.value());
        assertEquals(3, replica2.value());
        assertEquals(3, replica3.value());
    }
}