package crdt.Counters;

import org.junit.Test;

import static org.junit.Assert.*;

public class PNCounterTest {

    @Test
    public void testIncrement() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();

        replica1.increment("hostname1");
        assertEquals(1, replica1.value());
    }

    @Test
    public void testIncrement_multiplePNCounters() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica1.increment("hostname1");

        replica2.increment("hostname2");
        replica2.increment("hostname2");

        assertEquals(2, replica1.value());
        assertEquals(2, replica2.value());
    }

    @Test
    public void testDecrement() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica1.decrement("hostname1");

        assertEquals(0, replica1.value());
    }

    @Test
    public void testDecrement_negative() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica1.decrement("hostname1");
        replica1.decrement("hostname1");

        assertEquals(-1, replica1.value());
    }

    @Test
    public void testCopy() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();

        replica1.increment("hostname1");
        assertEquals(1, replica1.copy().value());
    }

    @Test
    public void testMerge() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica1.increment("hostname1");

        replica2.increment("hostname2");
        replica2.increment("hostname2");

        replica1.merge(replica2);
        replica2.merge(replica1);
        assertEquals(4, replica1.value());
        assertEquals(4, replica2.value());
    }

    @Test
    public void testMerge_incrementAndDecrement() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica1.increment("hostname1");
        replica2.increment("hostname2");
        replica2.increment("hostname2");

        replica1.decrement("hostname1");

        replica1.merge(replica2);
        replica2.merge(replica1);
        assertEquals(3, replica1.value());
        assertEquals(3, replica2.value());
    }

    /**
     * Incrementing each by 1. Then making the counter go to -1.
     */
    @Test
    public void testMerge_negativeValue() throws Exception {
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");
        replica2.increment("hostname2");

        replica1.decrement("hostname1");
        replica1.decrement("hostname1");
        replica1.decrement("hostname1");
        replica1.decrement("hostname1");

        replica1.merge(replica2);
        replica2.merge(replica1);
        assertEquals(-2, replica1.value());
        assertEquals(-2, replica2.value());
    }
}