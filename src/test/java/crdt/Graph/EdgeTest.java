package crdt.Graph;

import org.junit.Test;

import static org.junit.Assert.*;


public class EdgeTest {

    @Test
    public void testEquals() throws Exception {
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);
        Edge edge2 = new Edge(v, u);

        assertTrue(edge1.equals(edge2));
    }

    @Test
    public void testHashCode() throws Exception {
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);
        Edge edge2 = new Edge(v, u);

        assertEquals(edge1.hashCode(), edge2.hashCode());
    }

    @Test
    public void testHashCode_2() throws Exception {
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");
        Vertex w = new Vertex("w");

        Edge edge1 = new Edge(v, u);
        Edge edge2 = new Edge(v, w);

        assertNotEquals(edge1.hashCode(), edge2.hashCode());
    }
}