package crdt.Graph;

import org.junit.Test;

import static org.junit.Assert.*;


public class VertexTest {

    @Test
    public void testAddEdge() throws Exception {
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        v.addEdge(u);

        Edge e = new Edge(v, u);

        assertTrue( v.outEdges.contains(e));
    }

    @Test
    public void testEquals_differentVertexSameName() throws Exception {
        Vertex a = new Vertex("test");
        Vertex b = new Vertex("test");

        assertTrue(a.equals(b));
    }

    @Test
    public void testEquals_sameVertex() throws Exception {
        Vertex a = new Vertex("test");

        assertTrue(a.equals(a));
    }

    @Test
    public void testEquals_differentVertexName() throws Exception {
        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");

        assertFalse(a.equals(b));
    }

    @Test
    public void testHashCode_1() throws Exception {
        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");

        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCode_2() throws Exception {
        Vertex a = new Vertex("a");
        Vertex b = new Vertex("a");

        assertEquals(a.hashCode(), b.hashCode());
    }
}