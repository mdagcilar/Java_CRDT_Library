package crdt.Graph;

import crdt.CRDT;
import crdt.sets.TwoPhaseSet;


/**
 TODO: print tree? pretty print
 TODO: unique elements
 */
public class TwoPTwoPGraph<T> implements CRDT<TwoPTwoPGraph<T>> {

    /**
     * Each set contains 2 GSets one for added and one for storing removed elements.
     * We do not need to store removed Edges but we still use a TwoPhaseSet for simplicity
     */
    public TwoPhaseSet<Vertex> vertices;
    public TwoPhaseSet<Edge> edges;
    public Vertex startSentinel, endSentinel;


    public TwoPTwoPGraph() {
        vertices = new TwoPhaseSet<Vertex>();
        edges = new TwoPhaseSet<Edge>();

        /**
         * Initialize the Vertex set with the sentinels and add the edge between them.
         */
        startSentinel = new Vertex("startSentinel");
        endSentinel = new Vertex("endSentinel");
        vertices.added.add(startSentinel);
        vertices.added.add(endSentinel);
        Edge initSentinelEdge = new Edge(startSentinel, endSentinel);
        edges.added.add(initSentinelEdge);
//        System.out.println(startSentinel.inEdges.size());
//        System.out.println(startSentinel.outEdges.size());
//        System.out.println(endSentinel.inEdges.size());
//        System.out.println(endSentinel.outEdges.size());
    }

    /** @param vertex The Vertex to lookup if it exists.
     *   Return a boolean value. True if the vertex is in the added set and not in the removed set.
     **/
    public boolean lookupVertex(Vertex vertex)
    {
        return vertices.added.contains(vertex) && !vertices.removed.contains(vertex);
    }


    /** @param edge The edge to make a lookup on
     *  Return true iff
    - v and u are both in the vertex added set and not in the removed vertex set.
    - v and u are both in the edge set and not in the removed edge set
     */
    public boolean lookupEdge(Edge edge)
    {
        return edges.added.contains(edge);
    }


    /**
        Precondition: That vertices u and v exist.
        Perform and lookup on both values and if the lookup returns true.

        Precondition: To ensure acyclicity some form of local properties must be enforced.
                      Otherwise a counter-example would be a concurrent addEdge (u, v) || addEdge(v, u) -> these two operations
                      make the graph cyclic!
                 Fix; An edge may be added only if it oriented in the same direction as an existing path. That is, the new edge can
                      only strength the partial order defined by the DAG. The DAG must be initialised with left and right
                      sentinels |- and -| and edge(|-, -|). The only operation for adding a vertex is addBetween in order
                      to maintain the DAG property. The first operation must be addBetween(|-, -|).

                      This is a CRDT because addEdge(addBetween) either concern different edges (respect to vertices) in which can
                      they are independent. Or the same edge (with respect to vertices), in which can the execution is idempotent (duplicate delivery doesn't matter)

                      precondition: Need to perform some check that ensures (u, v, w) that u is before w, so v can be added between. (u, v) & (v, w)
                 Fix; Check that the second node w is in the 'outEdges' Hashset of u.
     */
    public String addBetweenVertex(Vertex u, Vertex v, Vertex w) {
        //Checks if u is in the Vertex Set
        if (!lookupVertex(u)) {
            return "Precondition failed - First node u does not exist";
        }
        //Checks if w is in the Vertex Set
        if (!lookupVertex(w)) {
            return "Precondition failed - Third node w does not exist";
        }
        //Checks if v is a unique new Vertex
        if (lookupVertex(v)) {
            return "Precondition failed - Second node v already exists, cannot add duplicates";
        }

        if (!u.outEdges.contains(w)) {
//            System.out.println(u.inEdges.size());
//            System.out.println(u.outEdges.size());
//            System.out.println(w.inEdges.size());
//            System.out.println(w.outEdges.size());
            return "Precondition failed - Nodes u and w are more than 1 level apart in the tree";
        }

        /**
         * downstream
         * - remove the initial edge between u and w
         * - add the new edge to each node
         * - add the new Vertex
         * - add the new edges to the EA set
         * return success message to the user
         */

        Edge e1 = new Edge(u, w);
        u.outEdges.remove(e1);
        w.inEdges.remove(e1);

        u.addEdge(v);
        v.addEdge(w);

        vertices.added.add(v);

        //add edges from u to v and v to w
        Edge edge1 = new Edge(u, v);
        Edge edge2 = new Edge(v, w);
        edges.added.add(edge1);
        edges.added.add(edge2);

        return "Successfully added node";
    }

    /**
     * Removing a Vertex (File or Directory) has the same effect. The sub branch of the tree below
     * will be removed because we do not distinguish between Files and Directories. This is to simplify
     * the problem, and can be introduced later to handle this problem.
     * @param v Vertex to be removed
     * @return String message to result the result of the method.
     */
    public String removeVertex(Vertex v)
    {
        if(!lookupVertex(v)){
            return "Precondition failed - Vertex does not exist, cannot remove a Vertex if it does not exist";
        }
        // check if 'v' is the sentinels
        if(v == startSentinel || v == endSentinel){
            return "Precondition failed - Cannot remove start or end Sentinel";
        }
        vertices.removed.add(v);
        return "Successfully removed Vertex";
    }

    public Vertex getStartSentinel()
    {
        return startSentinel;
    }
    public Vertex getEndSentinel()
    {
        return endSentinel;
    }

    /**
     * Get the Vertices (VA - VR)
     * Retain VR, EA and ER.
     * @return The current representation of the Graph, only the correct Vertices & Edges remain
     */
    public TwoPTwoPGraph<T> getGraph()
    {
        TwoPTwoPGraph<T> twoPTwoPGraph = new TwoPTwoPGraph<T>();

        /**
         * TODO: Could make use of copy(). Remove all from vertices.added and then re-add with getSetMinus()
         * twoPTwoPGraph.vertices.added.get().removeAll();
         */
        twoPTwoPGraph.vertices.added.addAll(this.vertices.added.get());
        twoPTwoPGraph.vertices.added.addAll(this.vertices.removed.get());
        twoPTwoPGraph.edges.added.addAll(this.edges.added.get());
        twoPTwoPGraph.edges.added.addAll(this.edges.removed.get());
        return twoPTwoPGraph;
    }

    /**
     * TODO: Preconditions to check relationships with edges don't break. May need to do something re-adding from removed nodes.
     * Apply Set Union on each GSet in the 2Pset
     * @param graph
     */
    public void merge(TwoPTwoPGraph<T> graph)
    {
        this.vertices.added.addAll(graph.vertices.added.get());
        this.vertices.removed.addAll(graph.vertices.removed.get());
        this.edges.removed.addAll(graph.edges.removed.get());
        this.edges.removed.addAll(graph.edges.removed.get());
    }

    public TwoPTwoPGraph<T> copy() {
        TwoPTwoPGraph<T> copy = new TwoPTwoPGraph<T>();

        copy.vertices = vertices.copy();  //copy() is the GSet's method copy.
        copy.edges = edges.copy();
        return copy;
    }
}
