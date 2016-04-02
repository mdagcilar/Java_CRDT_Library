package crdt.Graph;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;

import static org.junit.Assert.*;


public class GraphTest {

    /**
     * Graph instances and Vertex instances to minimize repeating code in each Test case.
     */
    Graph<String> replica1 = new Graph<String>();
    Graph<String> replica2 = new Graph<String>();

    Vertex startSentinel = new Vertex("startSentinel");
    Vertex endSentinel = new Vertex("endSentinel");


    /**
     * Check if the constructor added the sentinels.
     */
    @Test
    public void testConstructorAddedSentinels() throws Exception {

        assertEquals( newHashSet("startSentinel", "endSentinel"), replica1.getGraph().vertices.added.get());
        assertEquals( newHashSet(startSentinel, endSentinel), replica1.getGraph().vertices.added.get());
    }

    /**
     * Adding a new vertex. Should  maintain existing Vertex's. adding 'v' between u and w.
     * Should resolve in - u points to v and v points to w. So the original edge should be removed.
     */
    @Test
    public void testAddBetweenVertex_addsToVertices() throws Exception {
        //trying to add a new Vertex 'k' between the start and end sentinels
        Vertex k = new Vertex("k");

        replica1.addBetweenVertex(replica1.getStartSentinel(), k, replica1.getEndSentinel());

        assertEquals( newHashSet(startSentinel, endSentinel, k), replica1.getGraph().vertices.added.get());

        //check the edges

        Edge edge1 = new Edge(startSentinel, k);
        Edge edge2 = new Edge(k, endSentinel);

        Edge e3 = new Edge(startSentinel, endSentinel);
        //TODO: removeEdge method insert here.
        assertEquals( newHashSet(edge1, edge2), replica1.getGraph().edges.added.get());
    }

    @Test
    public void testRemoveVertex() throws Exception {

    }

    @Test
    public void testRemoveEdge() throws Exception {

    }


    @Test
    public void testGetGraph() throws Exception {

    }

    @Test
    public void testMerge_noDuplicateSentinels() throws Exception {
        replica1.merge(replica2);

        assertEquals( newHashSet(startSentinel, endSentinel), replica1.getGraph().vertices.added.get());
        assertEquals( newHashSet(startSentinel, endSentinel), replica2.getGraph().vertices.added.get());
    }

    @Test
    public void testMerge() throws Exception {
        //trying to add a new Vertex 'k' between the start and end sentinels
        Vertex k = new Vertex("k");
        Vertex m = new Vertex("m");

        replica1.addBetweenVertex(replica1.getStartSentinel(), k, replica1.getEndSentinel());


        replica1.addBetweenVertex(k, m, replica1.getEndSentinel());

    }

    @Test
    public void testInitialization(){
        assertEquals( newHashSet(replica1.getStartSentinel(), replica1.getEndSentinel()) , replica1.vertices.added.get());
        assertEquals( newHashSet(replica1.getStartSentinel(), replica1.getEndSentinel()) , replica2.vertices.added.get());
    }

    @Test
    public void test_equals(){
        assertTrue(replica1.getStartSentinel().equals(replica2.getStartSentinel()));

        //test all sets within the graph are equal. Vertices added/Vertices removed/Edges added/Edges removed
        assertTrue(replica1.getGraph().vertices.added.get().equals(replica2.getGraph().vertices.added.get()));
        assertTrue(replica1.getGraph().vertices.removed.get().equals(replica2.getGraph().vertices.removed.get()));
        assertTrue(replica1.getGraph().edges.added.get().equals(replica2.getGraph().edges.added.get()));
        assertTrue(replica1.getGraph().edges.removed.get().equals(replica2.getGraph().edges.removed.get()));
    }

    @Test
    public void testCopy() throws Exception {
        assertEquals(newHashSet(startSentinel, endSentinel), replica1.copy().getGraph().vertices.added.get());

    }

    @Test
    public void testLookupVertex() throws Exception {
        Vertex v = new Vertex("v");

        replica1.vertices.added.add(v);

        assertTrue(replica1.lookupVertex(v));
    }

    @Test
    public void testLookupEdge() throws Exception {
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);

        replica1.edges.add(edge1);

        assertTrue(replica1.lookupEdge(edge1));
    }

    @Test
    public void testGetStartSentinel() throws Exception {
        Vertex startSentinel = new Vertex("startSentinel");

        assertEquals(startSentinel, replica1.getStartSentinel());
    }

    @Test
    public void testGetEndSentinel() throws Exception {
        Vertex endSentinel = new Vertex("endSentinel");

        assertEquals(endSentinel, replica1.getEndSentinel());
    }
}