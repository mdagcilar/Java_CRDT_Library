package crdt.Graph;

import crdt.CRDT;

import java.util.HashSet;
import java.util.Set;


/**
 TODO: print tree pretty print
 TODO: remove vertex should re-attaach an edge
 TODO: concurrent add || remove fix
 TODO: addwins - revert removes
 TODO: removeVertex - must remove all the vertexes below itself.
 TODO: Store removed Edges?
 */
public class Graph_addWins<T> implements CRDT<Graph_addWins<T>> {

    /**
     * Each set contains 2 GSets one for added and one for storing removed elements.
     * We do not need to store removed Edges but we still use a TwoPhaseSet for simplicity and consistency.
     */
    public Set<Vertex> verticesAdded, verticesRemoved;
    public Set<Edge> edgesAdded, edgesRemoved;
    public Vertex startSentinel, endSentinel;


    /**
     * initialize the graph with start and end sentinels. Ensures acyclicity by the addBetween method.
     */
    public void initGraph(){
        verticesAdded = new HashSet<Vertex>();
        verticesRemoved = new HashSet<Vertex>();
        edgesAdded = new HashSet<Edge>();
        edgesRemoved = new HashSet<Edge>();

        //Initialize the Vertex set with the sentinels and add the edge between them.
        startSentinel = new Vertex("startSentinel");
        endSentinel = new Vertex("endSentinel");
        verticesAdded.add(startSentinel);
        verticesAdded.add(endSentinel);
        Edge initSentinelEdge = new Edge(startSentinel, endSentinel);
        edgesAdded.add(initSentinelEdge);
    }

    /** @param vertex The Vertex to lookup if it exists.
     *   Return a boolean value. True if the vertex is in the added set and not in the removed set.
     **/
    public boolean lookupVertex(Vertex vertex)
    {
        return verticesAdded.contains(vertex) && !verticesRemoved.contains(vertex);
    }


    /** @param edge The edge to make a lookup on
     */
    public boolean lookupEdge(Edge edge)
    {
        return edgesAdded.contains(edge) && !edgesRemoved.contains(edge);
    }


    /**
     * Precondition: That vertices u and w exist.
     * Precondition: That 'v' the Vertex to be added between is unique; Not already in the vertices added set, and not in the removed set.
     *
     * Precondition: To ensure acyclicity some form of local properties must be enforced.
     * Otherwise a counter-example would be a concurrent addEdge (u, v) || addEdge(v, u) -> these two operations make the graph cyclic.
     *
     * The Graph must be initialised with left and right sentinels '|-' and '-|' and edge(|-, -|).
     * The only operation for adding a vertex is addBetween in order to maintain the acyclicity property. The first operation must be addBetween(|-, -|).
     * This is a CRDT because addEdge(addBetween) is either concerned with different edges (respect to vertices) in which can
     * they are independent. Or the same edge (with respect to vertices), in which case the execution is idempotent (duplicate delivery doesn't matter)
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
        if (!edgesAdded.contains(new Edge(u, w))) {
            return "Precondition failed - Nodes u and w are more than 1 level apart in the tree";
        }

        /**
         * downstream
         * - remove the initial edge between u and w
         * - add the new edge to each node
         * - add the new Vertex
         * - add the new edges to the EA set
         */
        Edge edge = new Edge(u, w);
        u.outEdges.remove(edge);
        w.inEdges.remove(edge);

        /**
         * Prevents removal of the edge that is from the start and end sentinel and any edge that points to the end.
         * This allows Vertexs to have multiple children.
         */
        if(!edge.to.equals(endSentinel)){
            edgesRemoved.add(edge);
        }
        verticesAdded.add(v);

        //add edges from u to v and v to w
        Edge edge1 = new Edge(u, v);
        Edge edge2 = new Edge(v, w);
        edgesAdded.add(edge1);
        edgesAdded.add(edge2);
        u.addEdge(v);
        v.addEdge(w);

        return "Successfully added node";
    }

    /**
     * Removing a Vertex (File or Directory) has the same effect. The sub branch of the tree below
     * will be removed because we do not distinguish between Files and Directories. This is to simplify
     * the problem, and can be introduced later to handle this problem.
     * @param v Vertex to be removed
     * @return String message to result the reszult of the method.
     */
    public String removeVertex(Vertex v)
    {
        if(!lookupVertex(v)){
            return "Precondition failed - Vertex does not exist, cannot remove a Vertex if it does not exist";
        }
        // check if 'v' is either of the sentinels
        if(v == startSentinel || v == endSentinel){
            return "Precondition failed - Cannot remove start or end Sentinel";
        }

        /**
         * If the Vertex to be removed is one level above the endSentinel.
         * 1 - Add that Vertex to the removed set.
         * 2 - remove the edge between v and End
         * 3 - re-route the edge that was before v to the endSentinel
         */
        if(edgesAdded.contains(new Edge(v, endSentinel))){
            verticesRemoved.add(v);
        }

        /**
         * If the Vertex to be removed has Vertex's below it. We must remove all of them.
         * 1 -
         */

        verticesRemoved.add(v);
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
    public Graph_addWins<T> getGraph()
    {
        this.verticesAdded.removeAll(verticesRemoved);
        this.edgesAdded.removeAll(edgesRemoved);
        return this;
    }


    /**
     * TODO: Preconditions to check relationships with edges don't break. May need to do something re-adding from removed nodes.
     * Apply Set Union on each GSet in the 2Pset
     * @param graph
     */
    public void merge(Graph_addWins<T> graph)
    {
        this.verticesAdded.addAll(graph.verticesAdded);
        this.verticesRemoved.addAll(graph.verticesRemoved);
        this.edgesAdded.addAll(graph.edgesAdded);
        this.edgesRemoved.addAll(graph.edgesRemoved);
    }


    public Graph_addWins<T> copy() {
        Graph_addWins<T> copy = new Graph_addWins<T>();

        copy.verticesAdded = this.verticesAdded;
        copy.verticesRemoved = this.verticesRemoved;
        copy.edgesAdded = this.edgesAdded;
        copy.edgesRemoved = this.edgesRemoved;
        return copy;
    }
}
