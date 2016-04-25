package crdt.Graph;

import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class GraphTest {

    /**
     * Graph instances and Vertex instances to minimize repeating code in each Test case.
     */
    Graph<String> replica1 = new Graph<String>();
    Graph<String> replica2 = new Graph<String>();
    Graph<String> replica3 = new Graph<String>();
    Graph<String> replica4 = new Graph<String>();
    Graph<String> replica5 = new Graph<String>();
    Graph<String> replica6 = new Graph<String>();
    Graph<String> replica7 = new Graph<String>();
    Graph<String> replica8 = new Graph<String>();
    Graph<String> replica9 = new Graph<String>();
    Graph<String> replica10 = new Graph<String>();
    Graph<String> replica11 = new Graph<String>();
    Graph<String> replica12 = new Graph<String>();
    Graph<String> replica13 = new Graph<String>();
    Graph<String> replica14 = new Graph<String>();
    Graph<String> replica15 = new Graph<String>();
    Graph<String> replica16 = new Graph<String>();
    Graph<String> replica17 = new Graph<String>();
    Graph<String> replica18 = new Graph<String>();
    Graph<String> replica19 = new Graph<String>();
    Graph<String> replica20 = new Graph<String>();

    /**
     * create instances of Vertex's to re-use throughout tests
     */
    Vertex startSentinel = new Vertex("startSentinel");
    Vertex endSentinel = new Vertex("endSentinel");
    Vertex a = new Vertex("a");
    Vertex b = new Vertex("b");
    Vertex c = new Vertex("c");
    Vertex d = new Vertex("d");
    Vertex e = new Vertex("e");
    Vertex f = new Vertex("f");
    Vertex g = new Vertex("g");
    Vertex h = new Vertex("h");
    Vertex i = new Vertex("i");
    Vertex j = new Vertex("j");
    Vertex k = new Vertex("k");
    Vertex l = new Vertex("l");
    Vertex m = new Vertex("m");
    Vertex n = new Vertex("n");
    Vertex o = new Vertex("o");


    //create the Edge between the start and end markers. To save repeat code.
    Edge sentinelEdge = new Edge(startSentinel, endSentinel);

    ArrayList<Graph> graphs;
    ArrayList<Vertex> vertexes;
    ArrayList<Vertex> betweenVertexes;

    /**
     * Test: initGraph() adds the start and end sentinels.
     */
    @Test
    public void test_graphInit() {
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
    public void test_addBetweenVertex_addsToVertices() {
        replica1.initGraph();

        //Add Vertex 'a' between the start and end sentinels
        replica1.addBetweenVertex(replica1.getStartSentinel(), a, replica1.getEndSentinel());

        assertEquals(newHashSet(startSentinel, endSentinel, a), replica1.verticesAdded);
        assertEquals(newHashSet(sentinelEdge, new Edge(startSentinel, a), new Edge(a, endSentinel)), replica1.edgesAdded);
    }


    /**
     * Tests if the edge the initial edge between (u, w) is remove
     * when adding (u, v, w)
     */
    @Test
    public void test_addBetweenVertex_edgeRemoval() {
        replica1.initGraph();

        //Add Vertex 'a' between the start and end sentinels
        replica1.addBetweenVertex(replica1.getStartSentinel(), a, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, c, replica1.getEndSentinel());
        replica1.addBetweenVertex(a, b, c);

        assertTrue(replica1.edgesRemoved.contains(new Edge(a, c)));
    }


    /**
     * Test: Remove an entire subtree of Vertex's including and below 'a'
     * This test ensures that all Edges between nodes and Vertices are successfully removed.
     */
    @Test
    public void test_removeVertex_entireSubtree() {
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

    // tests if edge between v and the endSentinel is added to the removed edge set.
    // and edge is removed from the outEdges set.
    @Test
    public void test_removeVertex_endSentinelEdge() {
        replica1.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);

        replica1.removeVertex(a);

        Edge edge = new Edge(a, endSentinel);

        assertTrue(replica1.edgesRemoved.contains(edge));
        assertTrue(!a.outEdges.contains(edge));
    }

    //tests if the first if statement in removeVertex, removes the correct edges
    @Test
    public void test_removeVertex_edgeRemoval() {
        replica1.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);

        Edge edge = new Edge(b, endSentinel);

        replica1.edgesAdded.contains(edge);
        replica1.removeVertex(a);

        assertTrue(replica1.edgesRemoved.contains(edge));
        assertTrue(!b.outEdges.contains(edge));
    }

    // tests if the lookupVertex(v) fails upon removeVertex(v)
    // and returns from the method call.
    @Test
    public void test_removeVertex_lookupVertexFailed() {
        replica1.initGraph();

        replica1.removeVertex(a);

    }

    /**
     * Test: Remove Vertex 'b' which has no nodes below it. Test whether the correct Vertex's are removed.
     */
    @Test
    public void test_removeEdgeAboveEndSentinel(){
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
    public void test_merge_no_conflicts() {
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

        assertTrue(replica1.equals(replica2));
    }

    /**
     * Test: If graphs can converge after one removes a sub-tree of Vertex's
     */
    @Test
    public void test_merge_conflict_subtree_removal() {
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

        assertTrue(replica1.equals(replica2));
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
    public void test_merge_conflict_add_remove() {
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

        assertTrue(replica1.equals(replica2));
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
    public void test_merge_conflict_add_remove_partialTreeRemoval() {
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

        assertTrue(replica1.equals(replica2));
    }

    /**
     *  Test: Removing a Vertex 'b' from one replica. And trying to add that same Vertex to replica 2 before merging.
     */
    @Test
    public void test_merge_conflict_removal() {
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

        assertTrue(replica1.equals(replica2));
    }


    /**
     * Test for checking that merge see sentinels as the same elements and not unique.
     */
    @Test
    public void test_merge_noDuplicateSentinels() {
        replica1.initGraph();
        replica2.initGraph();

        replica1.merge(replica2);

        assertEquals(newHashSet(startSentinel, endSentinel), replica1.verticesAdded);
        assertEquals(newHashSet(startSentinel, endSentinel), replica2.verticesAdded);
    }



    /**
     * Test: getGraph method returns correct set minuses for Vertices and Edges.
     */
    @Test
    public void test_getGraph() {
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

    @Test
    public void test_graphInit_twoReplicas(){
        replica1.initGraph();
        replica2.initGraph();

        assertEquals( newHashSet(replica1.getStartSentinel(), replica1.getEndSentinel()) , replica1.verticesAdded);
        assertEquals( newHashSet(replica2.getStartSentinel(), replica2.getEndSentinel()) , replica2.verticesAdded);
    }

    /**
     * Test: sets are equal after initialization
     */
    @Test
    public void test_equals_sets(){
        replica1.initGraph();
        replica2.initGraph();

        assertTrue(replica1.getStartSentinel().equals(replica2.getStartSentinel()));

        //test all sets within the graph are equal. Vertices added/Vertices removed/Edges added/Edges removed
        assertTrue(replica1.verticesAdded.equals(replica2.verticesAdded));
        assertTrue(replica1.verticesRemoved.equals(replica2.verticesRemoved));
        assertTrue(replica1.edgesAdded.equals(replica2.edgesAdded));
        assertTrue(replica1.edgesRemoved.equals(replica2.edgesRemoved));
    }

    /**
     * Test: equals() method checks all sets of a Graph have successfully merged.
     */
    @Test
    public void test_equals_graph() {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica2.addBetweenVertex(startSentinel, d, endSentinel);

        replica2.addBetweenVertex(startSentinel, c, endSentinel);
        replica2.addBetweenVertex(b, c, endSentinel);

        replica1.addBetweenVertex(startSentinel, c, endSentinel);

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertTrue(replica1.equals(replica2));
    }

    @Test
    public void test_copy() {
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
    public void test_lookupVertex_true() {
        replica1.initGraph();

        replica1.verticesAdded.add(a);

        assertTrue(replica1.lookupVertex(a));
    }

    /**
     * Creating a Vertex, never adding it. Should return false.
     */
    @Test
    public void test_lookupVertex_false() {
        replica1.initGraph();

        Vertex v = new Vertex("v");

        assertFalse(replica1.lookupVertex(v));
    }

    /**
    * Test: testLookupEdge returns the correct boolean value.
     */
    @Test
    public void test_lookupEdge_true() {
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
    public void test_lookupEdge_false() {
        replica1.initGraph();

        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");

        Edge edge1 = new Edge(v, u);

        //replica1.edges.add(edge1);
        assertFalse(replica1.lookupEdge(edge1));
    }

    @Test
    public void test_getStartSentinel() {
        replica1.initGraph();

        Vertex startSentinel = new Vertex("startSentinel");

        assertEquals(startSentinel, replica1.getStartSentinel());
    }

    @Test
    public void test_getEndSentinel() {
        replica1.initGraph();

        Vertex endSentinel = new Vertex("endSentinel");

        assertEquals(endSentinel, replica1.getEndSentinel());
    }

    /**
     * Test:
     * Removals of sentinels.
     * adding duplicate Vertex's
     * adding between Vertex's that don't exist
     *
     */
    @Test
    public void test_duplicate_removals() {
        replica1.initGraph();
        replica2.initGraph();

        replica1.removeVertex(startSentinel);
        replica1.removeVertex(endSentinel);
        replica1.removeVertex(startSentinel);
        replica1.removeVertex(endSentinel);

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica2.addBetweenVertex(startSentinel, a, endSentinel);

        replica2.addBetweenVertex(startSentinel, c, endSentinel);
        replica2.addBetweenVertex(b, c, endSentinel);

        replica1.addBetweenVertex(startSentinel, c, endSentinel);

        replica1.merge(replica2);
        replica2.merge(replica1);

        assertEquals( newHashSet(startSentinel, endSentinel, a, c), replica1.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(startSentinel, c), new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica1.getGraph().edgesAdded);

        assertEquals( newHashSet(startSentinel, endSentinel, a, c), replica2.getGraph().verticesAdded);
        assertEquals( newHashSet(sentinelEdge,
                        new Edge(startSentinel, a), new Edge(startSentinel, c), new Edge(a, endSentinel), new Edge(c, endSentinel)),
                replica2.getGraph().edgesAdded);

        assertTrue(replica1.equals(replica2));
    }



    /**
     * Test: If the printed graph returns the same ArrayList of strings.
     */
    @Test
    public void test_printGraphPaths() {
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);
        replica1.addBetweenVertex(a, c, endSentinel);
        replica1.addBetweenVertex(c, d, endSentinel);

        replica1.addBetweenVertex(startSentinel, f, endSentinel);
        replica1.addBetweenVertex(f, g, endSentinel);

        replica1.merge(replica2);

        replica1.printGraphPaths(startSentinel);

        ArrayList<String> result = new ArrayList<String>();
        result.add("|-/a/b");
        result.add("|-/a/c/d");
        result.add("|-/a/c");
        result.add("|-/f/g");

        assertEquals(replica1.printGraphPaths(startSentinel), result);
    }


    /**
     * Test: Prove that merge order does not matter. A number of replicas can merge in different sequences and the
     * end result will be the same - iff each graph sees additions and removals of each graph directly or indirectly.
     *
     * direct merge - by merging with that graph
     * Or
     * indirect merge - graph 'a' merged with graph 'b' and if graph 'c' merges with 'a' it doesn't have to merge with 'b'
     * unless 'b' has made a change since 'a' merged with 'b'. This is known as an indirect merge.
     */
    @Test
    public void merge_order_equality(){
        Graph<String> replica5 = new Graph<String>();
        Graph<String> replica6 = new Graph<String>();
        Graph<String> replica7 = new Graph<String>();
        Graph<String> replica8 = new Graph<String>();

        replica5.initGraph();
        replica6.initGraph();
        replica7.initGraph();
        replica8.initGraph();

        //uses the method 'addRandomVertexes' to randomly add valid Vertex's to replicas 1 through to 4.
        addRandomVertexes(1000);

        //make copies of the original 4 graphs which have Vertex's in them.
        replica5 = replica1.copy();
        replica6 = replica2.copy();
        replica7 = replica3.copy();
        replica8 = replica4.copy();

        //Use two different merge orders and assert the equality of the Graphs.

        //first merge
        replica1.merge(replica2);
        replica1.merge(replica3);
        replica1.merge(replica4);
        replica2.merge(replica1);
        replica3.merge(replica1);
        replica4.merge(replica1);

        //second merge
        replica6.merge(replica7);
        replica8.merge(replica5);
        replica6.merge(replica8);
        replica7.merge(replica8);
        replica5.merge(replica7);
        replica5.merge(replica6);
        replica7.merge(replica6);
        replica8.merge(replica5);

        assertTrue(replica1.equals(replica5));
        assertTrue(replica2.equals(replica6));
        assertTrue(replica3.equals(replica7));
        assertTrue(replica4.equals(replica8));
    }

    /**
     * Concurrently, adds random vertices to see how well the type scales
     * Results are outputted to a text file
     */
    @Test
    public void test_scalability_test() {

        //changes standard output to create a txt file 'merge_average'. Records the average time to compute the merge function.
        test_setUpPrintStream();

        for (int i = 0; i < 10; i++) {
//            System.out.println("\n**********"+i+"**********");

            addRandomVertexes(100);
            System.out.print(replica1.getGraph().edgesAdded.size() + "," +
                    replica2.getGraph().edgesAdded.size() + "," + replica3.getGraph().edgesAdded.size() + "," + replica4.getGraph().edgesAdded.size() + "," +
                    replica5.getGraph().edgesAdded.size() + "," + replica6.getGraph().edgesAdded.size() + "," + replica7.getGraph().edgesAdded.size() + "," +
                    replica8.getGraph().edgesAdded.size() + "," + replica9.getGraph().edgesAdded.size() + "," + replica10.getGraph().edgesAdded.size() + "," +
                    replica11.getGraph().edgesAdded.size() + "," + replica12.getGraph().edgesAdded.size() + "," + replica13.getGraph().edgesAdded.size() + "," +
                    replica14.getGraph().edgesAdded.size() + "," + replica15.getGraph().edgesAdded.size() + "," + replica16.getGraph().edgesAdded.size() + "," +
                    replica17.getGraph().edgesAdded.size() + "," + replica18.getGraph().edgesAdded.size() + "," + replica19.getGraph().edgesAdded.size() + "," +
                    replica20.getGraph().edgesAdded.size() + "," +

                    (replica1.getGraph().edgesAdded.size() + replica2.getGraph().edgesAdded.size() + replica3.getGraph().edgesAdded.size() + replica4.getGraph().edgesAdded.size() +
                    replica5.getGraph().edgesAdded.size() + replica6.getGraph().edgesAdded.size() + replica7.getGraph().edgesAdded.size() + replica8.getGraph().edgesAdded.size() +
                    replica9.getGraph().edgesAdded.size() + replica10.getGraph().edgesAdded.size() + replica11.getGraph().edgesAdded.size() + replica12.getGraph().edgesAdded.size() +
                    replica13.getGraph().edgesAdded.size() + replica14.getGraph().edgesAdded.size() + replica15.getGraph().edgesAdded.size() + replica16.getGraph().edgesAdded.size() +
                    replica17.getGraph().edgesAdded.size() + replica18.getGraph().edgesAdded.size() + replica19.getGraph().edgesAdded.size() + replica20.getGraph().edgesAdded.size()))
            ;

            //used for debugging in the text file.
//            System.out.println("\nrep1: " + replica1.getGraph().edgesAdded.size());
//            System.out.println("rep2: " + replica2.getGraph().edgesAdded.size());
//            System.out.println("rep3: " + replica3.getGraph().edgesAdded.size());
//            System.out.println("rep4: " + replica4.getGraph().edgesAdded.size());

            //System.out.println("Attempted to add: " + 10000 + "vertices. Total Vertices added: " + (replica1.getGraph().edgesAdded.size() + replica2.getGraph().edgesAdded.size() + replica3.getGraph().edgesAdded.size() + replica4.getGraph().edgesAdded.size()));

            //Record this time using nanoTime()
            long startTime = System.nanoTime();
            //merge all replicas to equivalent states.
            replica1.merge(replica2);replica1.merge(replica3);replica1.merge(replica4);replica1.merge(replica5);replica1.merge(replica6);
            replica1.merge(replica7);replica1.merge(replica8);replica1.merge(replica9);replica1.merge(replica11);replica1.merge(replica12);
            replica1.merge(replica13);replica1.merge(replica14);replica1.merge(replica15);replica1.merge(replica16);replica1.merge(replica17);
            replica1.merge(replica18);replica1.merge(replica19);replica1.merge(replica20);

            replica2.merge(replica1);replica3.merge(replica1);replica4.merge(replica1);replica5.merge(replica1);replica6.merge(replica1);
            replica7.merge(replica1);replica8.merge(replica1);replica9.merge(replica1);replica10.merge(replica1);replica11.merge(replica1);
            replica12.merge(replica1);replica13.merge(replica1);replica14.merge(replica1);replica15.merge(replica1);replica16.merge(replica1);
            replica17.merge(replica1);replica18.merge(replica1);replica19.merge(replica1);replica20.merge(replica1);

            long endTime = System.nanoTime();
            long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.

            System.out.print(",0." + duration + ",");
            //System.out.println("\nThat took: 0." + duration / 1000 + " milli seconds");


            //used for debugging in the text file
//            System.out.println("\nrep1: " + replica1.getGraph().edgesAdded.size());
//            System.out.println("rep2: " + replica2.getGraph().edgesAdded.size());
//            System.out.println("rep3: " + replica3.getGraph().edgesAdded.size());
//            System.out.println("rep4: " + replica4.getGraph().edgesAdded.size());

            assertTrue(replica1.equals(replica2));
            assertTrue(replica2.equals(replica3));
            assertTrue(replica3.equals(replica4));
            assertTrue(replica4.equals(replica1));

            System.out.print(replica1.getGraph().edgesAdded.size());
            System.out.println();
//            System.out.println("********************\n");
        }
    }


    /**
     * Test if merge restores vertices correctly. After a concurrent addRemove
     *
     */
    @Test
    public void test_merge_conflictEdgesRemovals(){
        replica1.initGraph();
        replica2.initGraph();

        replica1.addBetweenVertex(startSentinel, a, endSentinel);
        replica1.addBetweenVertex(a, b, endSentinel);

        replica1.merge(replica2);
        replica2.merge(replica1);

        replica2.removeVertex(a);
        replica1.addBetweenVertex(b, c, endSentinel);



        replica1.merge(replica2);
        replica2.merge(replica1);

        Edge edge = new Edge(a, b);

        assertTrue(!replica1.verticesRemoved.contains(a));
        assertTrue(!replica1.edgesRemoved.contains(edge));
        assertTrue(a.outEdges.contains(edge));
    }

    /**
     * Method: addRandomVertex's
     *
     * Takes 3 ArrayLists containing
     * - 4 graphs ('graphs')
     * - Set of Vertex's including the start and end sentinel. ('vertexes')
     * - Set of randomly generated String Vertex's to use as the 'v' in addBetweenVertex(u, v, w). ('betweenVertexes')
     *
     * First for loop:
     * - Generates a random String to create 'k' number of Vertexes in the 'betweenVertexes' ArrayList
     *
     * Second for loop:
     * - Loops through 'x' number of times on a randomly selected Graph in the 'graphs' ArrayList to try and find a valid
     *  Vertex's u, v, w to add a Node to the graph.
     *  If successfully added, the if statement will catch the newly added Vertex and will add that to the 'vertexes'
     *  ArrayList. So it's possible to use a Vertex 'v' in the before and after positions. Otherwise no added edge will
     *  ever be able to be before or after another newly added edge. This is just to improve re-use of Vertexes
     *
     *  This method is provided to tests above to show scalability and validity of the addBetweenVertex method.
     */
    public void addRandomVertexes(int limit){
        replica1.initGraph();replica2.initGraph();
        replica3.initGraph();replica4.initGraph();replica5.initGraph();replica6.initGraph();replica7.initGraph();replica8.initGraph();
        replica9.initGraph();replica10.initGraph();replica11.initGraph();replica12.initGraph();replica13.initGraph();
        replica14.initGraph();replica15.initGraph();replica16.initGraph();replica17.initGraph();replica18.initGraph();
        replica19.initGraph();replica20.initGraph();

        //initialising ArrayLists that hold the graphs, and Vertex's to be added.
        graphs = new ArrayList<Graph>();
        vertexes = new ArrayList<Vertex>();
        betweenVertexes = new ArrayList<Vertex>();

        graphs.addAll( newHashSet(replica1, replica2, replica3, replica4, replica5, replica6, replica7, replica8, replica9, replica10, replica11, replica12, replica13, replica14, replica15, replica16, replica17, replica18, replica19, replica20));
        vertexes.addAll( newHashSet(startSentinel, endSentinel, a, b, c, d, e, f, g, h, i, j, k, l, o, m, n));
        betweenVertexes.addAll( newHashSet(a, b, c, d, e, f, g, h, i, j, k, l, o, m, n));

        //adds randomly generated Strings to the Vertex set 'k' times.
        for(int k=0; k<200; k++){
            betweenVertexes.add(new Vertex(Long.toHexString(Double.doubleToLongBits(Math.random()))));
        }

        // Random number generator
        Random rn = new Random();

        //for loop to loop through the Vertex's Hashset and randomly tries to insert new Vertex's to one of the graphs.
        for(int x=0; x<limit; x++){
            int num = rn.nextInt(betweenVertexes.size());
            int num2 = rn.nextInt(graphs.size());

            graphs.get(num2).addBetweenVertex(vertexes.get(rn.nextInt(vertexes.size())), betweenVertexes.get(num), vertexes.get(rn.nextInt(vertexes.size())));
            //graphs.get(rn.nextInt(graphs.size())).removeVertex(vertexes.get(rn.nextInt(vertexes.size())));

            // To allow added Vertex's to be the 'u' and 'w' nodes, add them to the VertexSubset which is a HashSet
            // that loops on the between Vertex 'v' in addBetweenVertex(u, v, w)
            if(graphs.get(num2).verticesAdded.contains(betweenVertexes.get(num))){
                vertexes.add(betweenVertexes.get(num));

                //to remove duplicates, add all elements to a Hashset then re-add them to an ArrayList.
                Set<Vertex> hs = new HashSet<Vertex>();
                hs.addAll(vertexes);
                vertexes.clear();
                vertexes.addAll(hs);
            }
        }
    }

    /**
     * Change standard output to print to a txt file. To record the average time it takes to merge.
     */
    public void test_setUpPrintStream() {
        try {
            PrintStream printStream = new PrintStream(new File("average_merge.txt"));
            System.setOut(printStream);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
