package crdt.Graph;

import crdt.CRDT;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 TODO: print tree pretty print
 */

public class Graph<T> implements CRDT<Graph<T>> {

    /** Each set contains 2 GSets one for added and one for storing removed elements.
     *  We do not need to store removed Edges but we still use a TwoPhaseSet for simplicity and consistency.
     */
    protected Set<Vertex> verticesAdded, verticesRemoved;
    protected Set<Edge> edgesAdded, edgesRemoved;
    private Vertex startSentinel, endSentinel;


    /**
     * initialize the graph with start and end sentinels. Ensures acyclicity by the addBetween method.
     */
    public void initGraph(){

        //initialize set instances
        verticesAdded = new HashSet<Vertex>();
        verticesRemoved = new HashSet<Vertex>();
        edgesAdded = new HashSet<Edge>();
        edgesRemoved = new HashSet<Edge>();

        //initialize the Vertex set with the sentinels and add the edge between them.
        startSentinel = new Vertex("startSentinel");
        endSentinel = new Vertex("endSentinel");
        verticesAdded.add(startSentinel);
        verticesAdded.add(endSentinel);
        Edge sentinelEdge = new Edge(startSentinel, endSentinel);
        edgesAdded.add(sentinelEdge);
    }

    /**
     * Getter methods for the start and end seninels.
     */
    public Vertex getStartSentinel() { return startSentinel; }
    public Vertex getEndSentinel() {return endSentinel; }

    /**
     * Return a boolean value. True if the vertex is in the added set and not in the removed set.
     * @param vertex The Vertex to lookup if it exists.
     * **/
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
    public void addBetweenVertex(Vertex u, Vertex v, Vertex w) {
        //Checks if u is in the Vertex Set
        if (!lookupVertex(u)) {
            //Precondition failed - First node u does not exist
            return;
        }
        //Checks if w is in the Vertex Set
        if (!lookupVertex(w)) {
            //Precondition failed - Third node w does not exist
            return;
        }
        //Checks if v is a unique new Vertex
        if (verticesAdded.contains(v) || verticesRemoved.contains(v)) {
            //Precondition failed - Vertex to be added already exists, cannot add duplicates
            return;
        }
        if (!edgesAdded.contains(new Edge(u, w))) {
            //Precondition failed - Nodes u and w are more than 1 level apart in the tree
            return;
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
         * This allows Vertex's to have multiple children.
         */
        if(!edge.to.equals(endSentinel)){
            edgesRemoved.add(edge);
        }
        verticesAdded.add(v);

        //add edges from u to v and v to w
        edgesAdded.add(new Edge(u, v));
        edgesAdded.add(new Edge(v, w));
        u.addEdge(v);
        v.addEdge(w);
    }


    /**
     * Removing a Vertex (File or Directory) has the same effect. The sub branch of the tree below
     * will be removed because we do not distinguish between Files and Directories. This is to simplify
     * the problem, and can be introduced later to handle this problem.
     *
     * @param v Vertex to be removed
     */
    public void removeVertex(Vertex v)
    {
        if(!lookupVertex(v)){
            //Precondition failed - Vertex does not exist, cannot remove a Vertex if it does not exist
            return;
        }
        // check if 'v' is either of the sentinels
        if((v.equals(startSentinel)) || (v.equals(endSentinel))){
            //Precondition failed - Cannot remove start or end Sentinel
            return;
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
    }

    /**
     * 1- Concurrent addBetweenVertex and removeVertex causes a conflict.
     *    Removing a Vertex 'a' whilst concurrently trying to merge a Vertex that
     *    is being adding below 'a', such that 'a' is in the parent path of the Vertex 'v'
     *    in addBetweenVertex( v , x, w)
     *    conflict resolution:
     *    If there is a Vertex 'v' in the first replica's added Vertex set and not in replica two's.
     *    AND there are edges such.that. 'v' is either in the 'from' or 'to' position
     *
     *
     * @param graph the graph to merge with.
     */
    public void merge(Graph<T> graph)
    {
        /**
         * Make a copy of the 'removed' Vertices so the 'for loop' can iteratively
         * remove a Vertex within the loop without throwing a ConcurrentModificationException
         * Same for Edges.
          */
        // copy of the this graphs removed Vertices
        Set<Vertex> thisVerticesRemovedCopy2 = new HashSet<Vertex>();
        thisVerticesRemovedCopy2.addAll(verticesRemoved);

        // copy of the arguments graph's removed Vertices
        Set<Vertex> graphVerticesRemovedCopy = new HashSet<Vertex>();
        graphVerticesRemovedCopy.addAll(graph.verticesRemoved);

        // copy of the this graphs Set difference (set minus) of Vertices
        Set<Vertex> thisVerticesSetMinus = new HashSet<Vertex>(verticesAdded);
        thisVerticesSetMinus.removeAll(graph.verticesAdded);

        // copy of the arguments graph's Set difference (set minus) of Vertices
        Set<Vertex> graphVerticesSetMinus = new HashSet<Vertex>(graph.verticesAdded);
        graphVerticesSetMinus.removeAll(verticesAdded);

        // if this graphs Set minus is not empty: enter the if statement.
        if(!thisVerticesSetMinus.isEmpty()) {
            /** for all the edges in this graph loop through to see if any Vertex 'v' in the removed elements of the argument graph
                are in the 'to' or 'from' position. Iff remove them from the removed set. Other word 'restore' those Vertex's    */
            for (Edge e : edgesAdded) {
                for (Vertex v : graphVerticesRemovedCopy) {
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
        /** Similarly as the code above for Vertex's that are in the second arguments graph and not in the current Graph. */
        if(!graphVerticesSetMinus.isEmpty()) {
            for (Edge e : graph.edgesAdded) {
                for (Vertex v : thisVerticesRemovedCopy2) {
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

        //Now that all conflicts are dealt with, merge the sets with Set Union
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
    public Graph<T> getGraph()
    {
        this.verticesAdded.removeAll(verticesRemoved);
        this.edgesAdded.removeAll(edgesRemoved);
        return this;
    }

    /**
     * Make a copy of this.graph */
    public Graph<T> copy() {
        Graph<T> copy = new Graph<T>();
        copy.initGraph();

        copy.verticesAdded.addAll(verticesAdded);
        copy.verticesRemoved.addAll(verticesRemoved);
        copy.edgesAdded.addAll(edgesAdded);
        copy.edgesRemoved.addAll(edgesRemoved);
        return copy;
    }

    public boolean equals(Graph<T> graph){
        return
                verticesAdded.equals(graph.verticesAdded)
                && verticesRemoved.equals(graph.verticesRemoved)
                && edgesAdded.equals(graph.edgesAdded)
                && edgesRemoved.equals(graph.edgesRemoved);
    }

    public void printTree(){
        System.out.println("Vertices: " + verticesAdded);
        System.out.println("Edges: " + edgesAdded);

        for(Vertex v : verticesAdded){
            System.out.println(v + "'s out edges: " + v.outEdges);
            System.out.println(v + "'s in edges: " + v.inEdges);
        }
    }
}