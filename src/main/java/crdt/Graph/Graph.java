package crdt.Graph;

import crdt.CRDT;
import crdt.sets.TwoPhaseSet;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 TODO: print tree pretty print
 TODO: remove vertex should re-attaach an edge
 TODO: should you be able to permanently remove an Edge?
 TODO: concurrent add || remove fix
 TODO: change constructor to init method so getGraph() and co
 */
public class Graph<T> implements CRDT<Graph<T>> {

    /**
     * Each set contains 2 GSets one for added and one for storing removed elements.
     * We do not need to store removed Edges but we still use a TwoPhaseSet for simplicity and consistency.
     */
    public TwoPhaseSet<Vertex> vertices;
    public Set<Edge> edges;
    public Vertex startSentinel, endSentinel;

    /**
     * initialize the graph with start and end sentinels. Ensures acyclicity by the addBetween method.
     */
    public void initGraph(){
        vertices = new TwoPhaseSet<Vertex>();
        edges = new HashSet<Edge>();

        //Initialize the Vertex set with the sentinels and add the edge between them.
        startSentinel = new Vertex("startSentinel");
        endSentinel = new Vertex("endSentinel");
        vertices.added.add(startSentinel);
        vertices.added.add(endSentinel);
        Edge initSentinelEdge = new Edge(startSentinel, endSentinel);
        edges.add(initSentinelEdge);
    }

    /** @param vertex The Vertex to lookup if it exists.
     *   Return a boolean value. True if the vertex is in the added set and not in the removed set.
     **/
    public boolean lookupVertex(Vertex vertex)
    {
        return vertices.added.contains(vertex) && !vertices.removed.contains(vertex);
    }


    /** @param edge The edge to make a lookup on
     */
    public boolean lookupEdge(Edge edge)
    {
        return edges.contains(edge);
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
        if (!edges.contains(new Edge(u, w))) {
            return "Precondition failed - Nodes u and w are more than 1 level apart in the tree";
        }

        /**
         * downstream
         * - remove the initial edge between u and w
         * - add the new edge to each node
         * - add the new Vertex
         * - add the new edges to the EA set
         */

        Edge e1 = new Edge(u, w);
        u.outEdges.remove(e1);
        w.inEdges.remove(e1);

        edges.remove(e1);

        u.addEdge(v);
        v.addEdge(w);

        vertices.added.add(v);

        //add edges from u to v and v to w
        Edge edge1 = new Edge(u, v);
        Edge edge2 = new Edge(v, w);
        edges.add(edge1);
        edges.add(edge2);

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
    public Graph<T> getGraph()
    {
        Graph<T> graph = new Graph<T>();
        return graph.copy();
    }


    /**
     * TODO: Preconditions to check relationships with edges don't break. May need to do something re-adding from removed nodes.
     * Apply Set Union on each GSet in the 2Pset
     * @param graph
     */
    public void merge(Graph<T> graph)
    {
        this.vertices.added.addAll(graph.vertices.added.get());
        this.vertices.removed.addAll(graph.vertices.removed.get());
        this.edges.addAll(graph.edges);
    }


    public Graph<T> copy() {
        Graph<T> copy = new Graph<T>();

        copy.vertices = this.vertices;
        copy.edges = this.edges;
        return copy;
    }
}
