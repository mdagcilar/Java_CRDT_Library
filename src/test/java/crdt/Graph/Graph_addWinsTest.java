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

    //create instances of Vertex to re-use throughout tests
    Vertex startSentinel = new Vertex("startSentinel");
    Vertex endSentinel = new Vertex("endSentinel");
    Vertex a = new Vertex("a");
    Vertex b = new Vertex("b");
    Vertex c = new Vertex("c");
    Vertex d = new Vertex("d");
    Vertex e = new Vertex("e");
    Vertex f = new Vertex("f");
    Vertex g = new Vertex("g");

    //create the Edge between the start and end markers. To save repeat code.
    Edge sentinelEdge = new Edge(startSentinel, endSentinel);

    /**
     * Test: initGraph() adds the start and end sentinels.
     */
    @Test
    public void testInitGraph_AddedSentinels() throws Exception {
        replica1.initGraph();
        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
    }

    /**
     * Test: Adding a new vertex.
     * - maintain existing Vertex's
     * - adding 'v' between u and w; Should resolve in - u points to v and v points to w.
     *
     * the original edge u-v should be removed.
     */
    @Test
    public void testAddBetweenVertex_addsToVertices() throws Exception {
        replica1.initGraph();

        //Add Vertex 'a' between the start and end sentinels
        replica1.addBetweenVertex(replica1.getStartSentinel(), a, replica1.getEndSentinel());

        assertEquals(newHashSet(startSentinel, endSentinel, a), replica1.verticesAdded);
        assertEquals(newHashSet(sentinelEdge, new Edge(startSentinel, a), new Edge(a, endSentinel)), replica1.edgesAdded);
    }


    /**
     * Test: Remove an entire subtree of Vertex's including and below 'a'
     * This test ensures that all Edges between nodes and Vertices are successfully removed.
     */
    @Test
    public void testRemoveVertex_entireSubtree() throws Exception {
        replica1.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);
        replica1.addBetweenVertex(a, c, endSentinel);
        replica1.addBetweenVertex(b, d, endSentinel);
        replica1.addBetweenVertex(c, e, endSentinel);

        replica1.removeVertex(a);

        assertEquals( newHashSet(startSentinel, endSentinel), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge), replica1.getGraph().edgesAdded);
    }

    /**
     * Test: Remove Vertex 'b' which has no nodes below it. Test whether the correct Vertex's are removed.
     */
    @Test
    public void testRemoveEdge_One_level_above_EndSentinel() throws Exception {
        replica1.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);

        //remove Vertex 'b'
        replica1.removeVertex(b);

        assertEquals( newHashSet(startSentinel, endSentinel, a), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge, new Edge(startSentinel, a), new Edge(a, endSentinel)), replica1.getGraph().edgesAdded);
    }

    /**
     * Test Union: Simple Merge to see if two graphs can merge together to represent the same Edges and Vertices.
     */
    @Test
    public void testMerge_test_union_noConflicts() throws Exception {
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
        assertEquals( newHashSet(sentinelEdge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, endSentinel), new Edge(e, endSentinel),
                new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, endSentinel), new Edge(e, endSentinel),
                new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     * Test: If graphs can converge after one removes a sub-tree of Vertex's
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
        assertEquals( newHashSet(sentinelEdge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
    }


    /**
     * Conflict addBetween || remove Vertex.
     *
     * replica1 adds a vertex using the (addBetween method) and a another replica2 removes a vertex that lives above the
     * added Vertex.
     * Imagine a user adding a file under a directory that has just been deleted. Clearly you cannot
     * add a new File to a path if a sub section of the start of the path has just be removed.
     * Our concept of a Graph gives precedence to the addBetween method. And should automatically resolve this conflict
     * by re-adding the Vertices and Edges removed by 'removeVertex' so that the add can succeed. This is handled in the
     * merge method. Individual replicas will diverge for a period of time until they merge and resolve conflicts with
     * automatic deterministic conflict resolution semantic built into the data type.
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
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, f), new Edge(d, endSentinel), new Edge(e, endSentinel), new Edge(f, endSentinel),
                        new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c, d, e, f), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(a, b), new Edge(b, endSentinel),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, e), new Edge(d, f), new Edge(d, endSentinel), new Edge(e, endSentinel), new Edge(f, endSentinel),
                        new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     * Test: Add a Vertex to a Vertex that should have been removed from a sub-tree removal.
     *
     * These adds should fail because there is a removeVertex(e) that happened before.
     * This removal removed Vertex's below 'e' including 'f'. So f is in the VerticesRemoved Set
     * Using 'f' in the addBetween method will fail successfully. Then addBetween(d, f, endSentinel) will fail
     * because we cannot re-add 'f'.
     *  replica2.addBetweenVertex(f, g, replica2.getEndSentinel());
     *  replica2.addBetweenVertex(d, f, replica2.getEndSentinel());
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

        //test
        replica2.addBetweenVertex(f, g, replica2.getEndSentinel());
        replica2.addBetweenVertex(d, f, replica2.getEndSentinel());

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals( newHashSet(startSentinel, endSentinel, a, c, d), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, endSentinel), new Edge(d, endSentinel),
                        new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, c, d), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, d), new Edge(c, endSentinel), new Edge(d, endSentinel),
                        new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
    }

    /**
     *  Test: Removing a Vertex 'b' from one replica. And trying to add that same Vertex to replica 2 before merging.
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
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, endSentinel), new Edge(c, b), new Edge(b, endSentinel),
                        new Edge(a, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a),
                        new Edge(startSentinel, c), new Edge(c, endSentinel), new Edge(c, b), new Edge(b, endSentinel),
                        new Edge(a, endSentinel)),
                replica2.getGraph().edgesAdded);
    }


    /**
     * Test: getGraph method returns correct set minuses for Vertices and Edges.
     */
    @Test
    public void testGetGraph() throws Exception {
        replica1.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);
        replica1.addBetweenVertex(b, c, endSentinel);

        replica1.removeVertex(c);

        //testing manual code for edges
        assertEquals(newHashSet(startSentinel, endSentinel, a, b, c), replica1.verticesAdded);
        assertEquals( newHashSet(c), replica1.verticesRemoved);

        //testing getGraph for vertices
        assertEquals( newHashSet(startSentinel, endSentinel, a , b), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(c), replica1.getGraph().verticesRemoved);


        //testing manual result of edges
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(a, endSentinel), new Edge(a, b), new Edge(b, endSentinel)),
                replica1.edgesAdded);
        assertEquals( newHashSet(new Edge(b, c), new Edge(c, endSentinel)), replica1.edgesRemoved);

        //testing getGraph for edges
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(a, endSentinel), new Edge(a, b), new Edge(b, endSentinel)),
                replica1.getGraph().edgesAdded);
        assertEquals( newHashSet(new Edge(b, c), new Edge(c, endSentinel)), replica1.getGraph().edgesRemoved);
    }

    /**
     * Test for checking that merge see sentinels as the same elements and not unique.
     */
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

    /**
     * Test: sets are equal after initialization
     */
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

    /**
     * Test: lookupVertex returns the correct boolean value.
     */
    @Test
    public void testLookupVertex_true() throws Exception {
        replica1.initGraph();

        replica1.verticesAdded.add(a);

        assertTrue(replica1.lookupVertex(a));
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

    /**
    * Test: testLookupEdge returns the correct boolean value.
     */
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