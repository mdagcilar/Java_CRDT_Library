package crdt.Graph;

import crdt.CRDT;

import java.util.HashSet;
import java.util.Set;

/**
 TODO: print tree pretty print
 TODO: concurrent add || remove fix - need to in merge method addwins concept
 */


public class Graph_addWins<T> implements CRDT<Graph_addWins<T>> {

    /** Each set contains 2 GSets one for added and one for storing removed elements.
     *  We do not need to store removed Edges but we still use a TwoPhaseSet for simplicity and consistency.
     */
    public Set<Vertex> verticesAdded, verticesRemoved;
    public Set<Edge> edgesAdded, edgesRemoved;
    private Vertex startSentinel, endSentinel;


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

    /**
     * Getter methods for the start and end seninels.
     */
    public Vertex getStartSentinel() { return startSentinel; }
    public Vertex getEndSentinel() {return endSentinel; }

    /** @param vertex The Vertex to lookup if it exists.
     *   Return a boolean value. True if the vertex is in the added set and not in the removed set.**/
    public boolean lookupVertex(Vertex vertex) {
        return verticesAdded.contains(vertex) && !verticesRemoved.contains(vertex); }

    /** @param edge The edge to make a lookup on */
    public boolean lookupEdge(Edge edge) {
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
        if (verticesRemoved.contains(v) || verticesAdded.contains(v) ) {
            return "Precondition failed - Vertex to be added already exists, cannot add duplicates";
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
        if((v.equals(startSentinel)) || (v.equals(endSentinel))){
            return "Precondition failed - Cannot remove start or end Sentinel";
        }

        /** Vertex should be remove. Could have sub vertex that also need to be removed.
         * 1 - Add that Vertex to the removed Vertices set.
         * 2 - Add the Edge between v and the endSentinel to the remove edge set.
         * 3 - Check all the edges in the added edge set if they contain 'v' as a 'from' or a 'to'
         *  edge. If an edge exists with the removed vertex 'v' in the 'from' position of an edge.
         *
         *  from position - Remove that edge and recursively remove the Vertex in the 'to' position.
         *  in position - as long as the edge is not the startSentinel - endSentinel, remove that edge. (the edge
         *  directly above the removed Vertex).
         */

        verticesRemoved.add(v);
        edgesRemoved.add(new Edge(v, endSentinel));
        v.outEdges.remove(new Edge(v, endSentinel));

        //any edge with v in it's 'to' or 'from' position; remove that edge and vertex.
        for(Edge e : edgesAdded){
            if(e.from.equals(v)){
                edgesRemoved.add(e);
                v.outEdges.remove(e);

                //recursively remove the node below.
                removeVertex(e.to);
            //prevents removal of the 'start and end' sentinel.
            }else if((e.to.equals(v)) && !(e.equals(new Edge(startSentinel, endSentinel)))){
                edgesRemoved.add(e);
                v.inEdges.remove(e);

                //if a Vertex has an edge to this Vertex, remove it from it's edge and let the Vertex stay in the Vertex Set\.
                if(!(e.from.equals(startSentinel))){
                    e.from.outEdges.remove(e);
                }
            }
        }
        return "Successfully removed Vertex";
    }

    /**
     * 1- Concurrent addBetweenVertex and removeVertex causes a conflict.0
     *    Removing a Vertex 'a' whilst concurrently trying to merge a Vertex that
     *    is being adding below 'a', such that 'a' is in the parent path of the Vertex 'v'
     *    in addBetweenVertex( v , x, w
     *    Resolve this by:
     *    1- Is there an Edge in the edgesRemoved set which a new Vertex 'x' relies on.
     *
     *
     * @param graph the graph to merge with
     */
    public void merge(Graph_addWins<T> graph)
    {
        // Make a copy of the 'removed' Vertices so the for loop can iteratively remove a Vertex within the loop
        // without throwing a ConcurrentModificationException
        Set<Vertex> verticesRemovedCopy = new HashSet<Vertex>();
        verticesRemovedCopy.addAll(graph.verticesRemoved);

        Set<Vertex> verticesRemovedCopy2 = new HashSet<Vertex>();
        verticesRemovedCopy2.addAll(verticesRemoved);

        Set<Vertex> verticesSetMinus = new HashSet<Vertex>(verticesAdded);
        verticesSetMinus.removeAll(graph.verticesAdded);

        Set<Vertex> verticesSetMinus2 = new HashSet<Vertex>(graph.verticesAdded);
        verticesSetMinus2.removeAll(verticesAdded);

        /**
         * TODO: How do you identify when a Vertex has been added as a Vertex has been removed. Then re-add removed vertices & edges
         *
         * What's the difference in sets when you do a simple removal of a Vertex
         * And when you do a remove add
         *
         *
         */

        if(!verticesSetMinus.isEmpty()) {
            for (Edge e : edgesAdded) {
                for (Vertex v : verticesRemovedCopy) {
                    if (e.from.equals(v)) {
                        graph.verticesRemoved.remove(v);
                        v.outEdges.add(e);
                        graph.edgesRemoved.remove(e);
                    }
                    if (e.to.equals(v)) {
                        graph.verticesRemoved.remove(v);
                        v.inEdges.add(e);
                        graph.edgesRemoved.remove(e);
                    }
                }
            }
        }

        if(!verticesSetMinus2.isEmpty()) {
            for (Edge e : graph.edgesAdded) {
                for (Vertex v : verticesRemovedCopy2) {
                    if (e.from.equals(v)) {
                        verticesRemoved.remove(v);
                        v.outEdges.add(e);
                        edgesRemoved.remove(e);
                    }
                    if (e.to.equals(v)) {
                        verticesRemoved.remove(v);
                        v.inEdges.add(e);
                        edgesRemoved.remove(e);
                    }
                }
            }
        }

        this.verticesAdded.addAll(graph.verticesAdded);
        this.verticesRemoved.addAll(graph.verticesRemoved);
        this.edgesAdded.addAll(graph.edgesAdded);
        this.edgesRemoved.addAll(graph.edgesRemoved);
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
     * Make a copy of this.graph */
    public Graph_addWins<T> copy() {
        Graph_addWins<T> copy = new Graph_addWins<T>();
        copy.initGraph();

        copy.verticesAdded.addAll(verticesAdded);
        copy.verticesRemoved.addAll(verticesRemoved);
        copy.edgesAdded.addAll(edgesAdded);
        copy.edgesRemoved.addAll(edgesRemoved);
        return copy;
    }
}