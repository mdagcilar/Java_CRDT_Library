package crdt.Graph;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class Graph_addWinsTest {

    /**
     * Graph instances and Vertex instances to minimize repeating code in each Test case.
     */
    Graph_addWins<String> replica1 = new Graph_addWins<String>();
    Graph_addWins<String> replica2 = new Graph_addWins<String>();

    Vertex startSentinel = new Vertex("startSentinel");
    Vertex endSentinel = new Vertex("endSentinel");


    /**
     * Check if the constructor added the sentinels.
     */
    @Test
    public void testInitGraph_AddedSentinels() throws Exception {
        replica1.initGraph();

        assertEquals(newHashSet("startSentinel", "endSentinel"), replica1.verticesAdded);
        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
    }

    /**
     * Adding a new vertex. Should  maintain existing Vertex's. adding 'v' between u and w.
     * Should resolve in - u points to v and v points to w. So the original edge should be removed.
     */
    @Test
    public void testAddBetweenVertex_addsToVertices() throws Exception {
        replica1.initGraph();
        //trying to add a new Vertex 'k' between the start and end sentinels
        Vertex k = new Vertex("k");

        replica1.addBetweenVertex(replica1.getStartSentinel(), k, replica1.getEndSentinel());

        assertEquals(newHashSet(startSentinel, endSentinel, k), replica1.verticesAdded);

        //check the edges
        Edge edge = new Edge(startSentinel, endSentinel);
        Edge edge1 = new Edge(startSentinel, k);
        Edge edge2 = new Edge(k, endSentinel);

        assertEquals(newHashSet(edge, edge1, edge2), replica1.edges);
    }

    @Test
    public void testRemoveVertex_entireSubtree() throws Exception {
        replica1.initGraph();

        Edge edge = new Edge(startSentinel, endSentinel);

        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
        assertEquals(newHashSet(edge), replica1.edges);

        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");
        Vertex c = new Vertex("c");
        Vertex d = new Vertex("d");
        Vertex e = new Vertex("e");

        System.out.println(replica1.addBetweenVertex(startSentinel, a, endSentinel));
        System.out.println(replica1.addBetweenVertex(a, b, endSentinel));
        System.out.println(replica1.addBetweenVertex(a, c, endSentinel));
        System.out.println(replica1.addBetweenVertex(b, d, endSentinel));
        System.out.println(replica1.addBetweenVertex(c, e, endSentinel));

        System.out.println(replica1.removeVertex(a));
        replica1.removeVertex(a);

        System.out.println("dfgdfgdgfgdf" + replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(startSentinel, endSentinel), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge), replica1.getGraph().edges);
    }

    @Test
    public void testRemoveEdge() throws Exception {

    }


    @Test
    public void testGetGraph_forVertices() throws Exception {
        replica1.initGraph();

        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");
        Vertex c = new Vertex("c");

        replica1.verticesAdded.add(a);
        replica1.verticesAdded.add(b);
        replica1.verticesAdded.add(c);
        replica1.verticesRemoved.add(c);


        assertEquals(newHashSet(startSentinel, endSentinel, a , b, c), replica1.verticesAdded);
        assertEquals(newHashSet(c), replica1.verticesRemoved);

        //calling getGraph c should not be in the verticesAdded set.
        assertEquals(newHashSet(startSentinel, endSentinel, a , b), replica1.getGraph().verticesAdded);
        assertEquals(newHashSet(c), replica1.getGraph().verticesRemoved);
    }

    @Test
    public void testMerge_noDuplicateSentinels() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        replica1.merge(replica2);

        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
        assertEquals(newHashSet(startSentinel, endSentinel), replica2.verticesAdded);
    }

    @Test
    public void testMerge() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        //trying to add a new Vertex 'k' between the start and end sentinels
        Vertex k = new Vertex("k");
        Vertex m = new Vertex("m");

        replica1.addBetweenVertex(replica1.getStartSentinel(), k, replica1.getEndSentinel());


        replica1.addBetweenVertex(k, m, replica1.getEndSentinel());

    }

    @Test
    public void testInitialization(){
        replica1.initGraph();
        replica2.initGraph();

        assertEquals( newHashSet(replica1.getStartSentinel(), replica1.getEndSentinel()) , replica1.verticesAdded);
        assertEquals( newHashSet(replica2.getStartSentinel(), replica2.getEndSentinel()) , replica2.verticesAdded);
    }

    @Test
    public void test_equals(){
        replica1.initGraph();
        replica2.initGraph();

        assertTrue(replica1.getStartSentinel().equals(replica2.getStartSentinel()));

        //test all sets within the graph are equal. Vertices added/Vertices removed/Edges added/Edges removed
        assertTrue(replica1.verticesAdded.equals(replica2.verticesAdded));
        assertTrue(replica1.verticesRemoved.equals(replica2.verticesRemoved));
        assertTrue(replica1.edges.equals(replica2.edges));
    }

    @Test
    public void testCopy() throws Exception {
        replica1.initGraph();
        Vertex v = new Vertex("v");

        Edge edge1 = new Edge(startSentinel, endSentinel);
        Edge edge2 = new Edge(startSentinel, v);
        Edge edge3 = new Edge(v, endSentinel);

        replica1.addBetweenVertex(startSentinel, v, endSentinel);

        assertEquals(newHashSet(startSentinel, v, endSentinel), replica1.copy().verticesAdded);
        assertEquals(newHashSet(), replica1.copy().verticesRemoved);
        assertEquals( newHashSet(edge1, edge2, edge3), replica1.copy().edges);

    }

    @Test
    public void testLookupVertex_true() throws Exception {
        replica1.initGraph();

        Vertex v = new Vertex("v");
        replica1.verticesAdded.add(v);

        assertTrue(replica1.lookupVertex(v));
    }

    /**
     * Creating a Vertex, never adding it. Should return false.
     */
    @Test
    public void testLookupVertex_false() throws Exception {
        replica1.initGraph();


        Vertex v = new Vertex("v");

        assertFalse(replica1.lookupVertex(v));
    }

    @Test
    public void testLookupEdge_true() throws Exception {
        replica1.initGraph();

        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);

        replica1.edges.add(edge1);

        assertTrue(replica1.lookupEdge(edge1));
    }

    /**
     *Creating an Edge, never adding it. Should return false
     */
    @Test
    public void testLookupEdge_false() throws Exception {
        replica1.initGraph();

        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);

        //replica1.edges.add(edge1);
        assertFalse(replica1.lookupEdge(edge1));
    }

    @Test
    public void testGetStartSentinel() throws Exception {
        replica1.initGraph();

        Vertex startSentinel = new Vertex("startSentinel");

        assertEquals(startSentinel, replica1.getStartSentinel());
    }

    @Test
    public void testGetEndSentinel() throws Exception {
        replica1.initGraph();

        Vertex endSentinel = new Vertex("endSentinel");

        assertEquals(endSentinel, replica1.getEndSentinel());
    }
}