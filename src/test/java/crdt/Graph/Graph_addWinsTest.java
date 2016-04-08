package crdt.Graph;

import org.junit.Test;


import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

//TODO: add tests for removing the end and start sentinels
public class Graph_addWinsTest {

    /**
     * Graph instances and Vertex instances to minimize repeating code in each Test case.
     */
    Graph_addWins<String> replica1 = new Graph_addWins<String>();
    Graph_addWins<String> replica2 = new Graph_addWins<String>();

    Vertex startSentinel = new Vertex("startSentinel");
    Vertex endSentinel = new Vertex("endSentinel");

    Vertex a = new Vertex("a");
    Vertex b = new Vertex("b");
    Vertex c = new Vertex("c");
    Vertex d = new Vertex("d");
    Vertex e = new Vertex("e");
    Vertex f = new Vertex("f");
    Vertex g = new Vertex("g");

    Edge edge = new Edge(startSentinel, endSentinel);

    /**
     * Check if the constructor added the sentinels.
     */
    @Test
    public void testInitGraph_AddedSentinels() throws Exception {
        replica1.initGraph();
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
        Edge edge1 = new Edge(startSentinel, k);
        Edge edge2 = new Edge(k, endSentinel);

        assertEquals(newHashSet(edge, edge1, edge2), replica1.edgesAdded);
    }


    /**
     * Remove an entire subtree of Vertexes including and below 'a'
     * This test ensures that all Edges between nodes are successfully remove, and vertices.
     */
    @Test
    public void testRemoveVertex_entireSubtree() throws Exception {
        replica1.initGraph();

        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
        assertEquals(newHashSet(edge), replica1.edgesAdded);

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);
        replica1.addBetweenVertex(a, c, endSentinel);
        replica1.addBetweenVertex(b, d, endSentinel);
        replica1.addBetweenVertex(c, e, endSentinel);

        replica1.removeVertex(a);

        assertEquals( newHashSet(startSentinel, endSentinel), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge), replica1.getGraph().edgesAdded);
    }

    /**
     * Test: Remove a Vertex 'b' which has no nodes below it. Test whether the correct Vertex's are removed.
     */
    @Test
    public void testRemoveEdge_OnelevelAboveEndSentinel() throws Exception {
        replica1.initGraph();

        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");
        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);
        replica1.removeVertex(b);

        assertEquals( newHashSet(startSentinel, endSentinel, a), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge, new Edge(startSentinel, a), new Edge(a, endSentinel)), replica1.getGraph().edgesAdded);
    }

    /**
     * Simple Merge test to see if two graphs can merge together to represent the same Edges and Vertices.
     */
    @Test
    public void testMerge_simpleNoConflicts() throws Exception {
        replica1.initGraph();
        replica2.initGraph();


        replica1.addBetweenVertex(startSentinel, a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, replica1.getEndSentinel());

        replica2.addBetweenVertex(startSentinel, c, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, d, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, e, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, endSentinel), new Edge(e, endSentinel),
                new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, endSentinel), new Edge(e, endSentinel),
                new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     * Test: check if Graphs can converge after one of them removes a sub-tree.
     */
    @Test
    public void testMerge_withRemoveVertex_noConflict() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, replica1.getEndSentinel());

        replica2.addBetweenVertex(startSentinel, c, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, d, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, e, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        replica1.removeVertex(c);

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
    }


    /**
     * Conflict_1. A user adds a vertex using the (addBetween method) and a another user removes a vertex that exists in
     * that add method. Imagine a user adding a file under a directory that has just been deleted. Clearly you cannot
     * add something that to a path if a sub section of the start of the path does not exist.
     * Our concept of a Graph gives precedence to the addBetween method. And should automatically resolve this conflict
     * by re-adding the Vertices and Edges removed by 'removeVertex' so that the add can succeed.
     */
    @Test
    public void testMerge_conflict_addBetween_removeVertex() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, replica1.getEndSentinel());

        replica2.addBetweenVertex(startSentinel, c, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, d, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, e, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        replica1.removeVertex(c);
        replica2.addBetweenVertex(d, f, endSentinel);

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e, f), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, f), new Edge(d, endSentinel), new Edge(e, endSentinel), new Edge(f, endSentinel),
                        new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e, f), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, f), new Edge(d, endSentinel), new Edge(e, endSentinel), new Edge(f, endSentinel),
                        new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     * Test: Trying to add a Vertex to a Vertex that should have been removed from a sub-tree removal.
     *       The addBetween(d, f , endSentinel) Should fail beca
     */
    @Test
    public void testMerge_conflict_addBetween_removeVertex_partialTree_removal() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, replica1.getEndSentinel());

        replica2.addBetweenVertex(startSentinel, c, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, d, replica2.getEndSentinel());
        replica2.addBetweenVertex(c, e, replica2.getEndSentinel());
        replica2.addBetweenVertex(e, f, replica2.getEndSentinel());

        replica1.removeVertex(b);
        replica2.removeVertex(e);

        replica2.addBetweenVertex(f, g, replica2.getEndSentinel());
        //replica2.addBetweenVertex(d, b, replica2.getEndSentinel());
        replica2.addBetweenVertex(d, f, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals( newHashSet(startSentinel, endSentinel, a, c, d), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, endSentinel), new Edge(d, endSentinel),
                        new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, c, d), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, endSentinel), new Edge(d, endSentinel),
                        new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     *  Test: removing a Vertex 'b' in from one replica. And trying to add that same Vertex to replica 2 before merging.
     */
    @Test
    public void testMerge_conflict_removal() throws Exception {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, replica1.getEndSentinel());

        replica2.addBetweenVertex(startSentinel, c, replica2.getEndSentinel());

        //Remove(b) and add 'b' in the other replica before a merge. 'b' should be in the set.
        replica1.removeVertex(b);
        replica2.addBetweenVertex(c, b, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals( newHashSet(startSentinel, endSentinel, a, b, c), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, endSentinel), new Edge(c, b), new Edge(b, endSentinel),
                        new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(edge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, endSentinel), new Edge(c, b), new Edge(b, endSentinel),
                        new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
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
        assertTrue(replica1.edgesAdded.equals(replica2.edgesAdded));
        assertTrue(replica1.edgesRemoved.equals(replica2.edgesRemoved));
    }

    @Test
    public void testCopy() throws Exception {
        replica1.initGraph();
        Vertex v = new Vertex("v");

        Edge edge1 = new Edge(startSentinel, endSentinel);
        Edge edge2 = new Edge(startSentinel, v);
        Edge edge3 = new Edge(v, endSentinel);

        replica1.addBetweenVertex(startSentinel, v, endSentinel);

        assertEquals( newHashSet(startSentinel, v, endSentinel), replica1.copy().verticesAdded);
        assertEquals( newHashSet(), replica1.copy().verticesRemoved);
        assertEquals( newHashSet(edge1, edge2, edge3), replica1.copy().edgesAdded);
        assertEquals( newHashSet(), replica1.copy().edgesRemoved);

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

        replica1.edgesAdded.add(edge1);

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