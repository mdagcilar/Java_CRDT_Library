package crdt.Graph;

import crdt.CRDT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Graph<T> implements CRDT<Graph<T>> {

    /**
     *  Each set contains 2 Sets one for added and one for storing removed elements.
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
     * This is a CRDT because addBetween is either concerned with different vertices in which case
     * they are independent. Or the same vertex, in which case the execution is idempotent (duplicate delivery doesn't matter)
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



        // If all preconditions are true, the vertex can be safely added.
        // 1 - remove the initial edge between (u, w)
        // 2 - add the two new edges (u, v) and (v, w)
        // 3 - add the new Vertex (v)
        // 4 - add the new edges to the EA set
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
     * Removing a Vertex has the effect of removing the sub branch of the tree, below
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

            }else if((e.to.equals(v))){
                edgesRemoved.add(e);
                v.inEdges.remove(e);
            }
        }
    }

    /**
     * 1- Concurrent addBetweenVertex and removeVertex causes a conflict.
     *    Removing a Vertex 'a' whilst concurrently trying to merge a Vertex that is
     *    adding below 'a', such that 'a' is in the parent path of the Vertex 'v'
     *    in addBetweenVertex( v , x, w)
     *    conflict resolution:
     *
     *    If a vertex {v}, has been removed in one of the replicas and there is an edge
     *    in the other replica, such that {v} is in either of the two positions (from, to).
     *    This means that the other replica relies on that vertex and we restore it.
     *
     *
     * @param graph the graph to merge with.
     */
    public void merge(Graph<T> graph) {
        /**
         * Make a copy of the 'removed' Vertices so the 'for loop' can iteratively
         * remove a Vertex within the loop without throwing a ConcurrentModificationException
         * Same for Edges.
         */
        // copy of the this graphs removed Vertices
        Set<Vertex> replica1VerticesRemoved = new HashSet<Vertex>();
        replica1VerticesRemoved.addAll(verticesRemoved);

        // copy of the arguments graph's removed Vertices
        Set<Vertex> replica2VerticesRemoved = new HashSet<Vertex>();
        replica2VerticesRemoved.addAll(graph.verticesRemoved);

        // this.verticesAdded - graph.verticesAdded
        // copy of the this graphs Set difference (set minus) of Vertices
        Set<Vertex> rep1VerticesSetMinusRep2 = new HashSet<Vertex>(verticesAdded);
        rep1VerticesSetMinusRep2.removeAll(graph.verticesAdded);

        // graph.verticesAdded - this.verticesAdded
        // copy of the arguments graph's Set difference (set minus) of Vertices
        Set<Vertex> rep2VerticesSetMinusRep2 = new HashSet<Vertex>(graph.verticesAdded);
        rep2VerticesSetMinusRep2.removeAll(verticesAdded);

        // if this graphs Set minus is not empty: enter the if statement.
        if (!rep1VerticesSetMinusRep2.isEmpty()) {

            //iterate through all the edges in replica1
            for (Edge e : edgesAdded) {

                //loop through vertices in the removed set of replica2
                for (Vertex v : replica2VerticesRemoved) {
                    if (e.from.equals(v)) {

                        //restore vertex v
                        graph.verticesRemoved.remove(v);
                        graph.edgesRemoved.remove(e);
                        v.outEdges.add(e);
                    }
                    if (e.to.equals(v)) {

                        //restore vertex v
                        graph.verticesRemoved.remove(v);
                        graph.edgesRemoved.remove(e);
                        v.inEdges.add(e);
                    }
                }
            }
        }
        /** Similarly as the code above for Vertex's that are in the second arguments graph and not in the current Graph. */
        if (!rep2VerticesSetMinusRep2.isEmpty()) {
            for (Edge e : graph.edgesAdded) {
                for (Vertex v : replica1VerticesRemoved) {
                    if (e.from.equals(v)) {

                        //restore vertex v
                        verticesRemoved.remove(v);
                        edgesRemoved.remove(e);
                        v.outEdges.add(e);
                    }
                    if (e.to.equals(v)) {

                        //restore vertex v
                        verticesRemoved.remove(v);
                        edgesRemoved.remove(e);
                        v.inEdges.add(e);
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
     * Make a copy of this.graph
     */
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

    /**
     * Takes a Vertex, loops through all the edges in this graph. Add the 'to' edge in Edge(from, to)
     * to the String path. the recursively find edges connected to that Vertex using the recursive method
     * findPaths().
     *
     * @param x the Vertex to start the path finding from. Usually the start sentinel.
     * @return an ArrayList of all the paths in the Graph.
     */
    public ArrayList<String> printGraphPaths(Vertex x){
        ArrayList<String> paths = new ArrayList<String>();

        if(x.equals(startSentinel)) {
            for (Edge e : getGraph().edgesAdded) {
                if (e.from.equals(startSentinel) && !(e.to.equals(endSentinel))) {
                    String path = "|-/" + e.to.toString();
                    findPaths(e.to, path, paths);
                }
            }
        }
        return paths;
    }

    /**
     * findPaths recursively finds edges connected to the Vertex 'v'.
     * @param v the vertex to recursively loop through the outEdges of.
     * @param path the String that  holds the concatenated strings to form the path.
     * @param paths ArrayList of paths
     */
    public void findPaths(Vertex v, String path, ArrayList<String> paths){
        for(Edge e2 : v.outEdges){
            if(e2.to.equals(endSentinel))
                return;

            String newPath = path + "/" + e2.to.toString();
            if(!e2.to.outEdges.isEmpty())
                findPaths(e2.to, newPath, paths);
            paths.add(newPath);
        }
    }
}
