package crdt.Graph;

import crdt.CRDT;
import crdt.sets.TwoPhaseSet;

import java.util.LinkedHashSet;
import java.util.Set;

/**
TODO: add sentinels in edge set
 todo: removeVertex method
 */
public class TwoPTwoPGraph<T> implements CRDT<TwoPTwoPGraph<T>> {

    private TwoPhaseSet<Vertex> vertices;
    private TwoPhaseSet<Edge> edges;


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
            return "Invalid add - First node u does not exist";
        }
        //Checks if w is in the Vertex Set
        if (!lookupVertex(w)) {
            return "Invalid add - Third node w does not exist";
        }
        //Checks if v is a unique new Vertex
        if (lookupVertex(v)) {
            return "Invalid add - Second node v already exists, cannot add duplicates";
        }

        if (!u.outEdges.contains(w)) {
            return "Invalid add - Nodes u and w are more than 1 level apart in the tree";
        }


        //downstream

        //remove the initial edge between u and w
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
     * Removing a Vertex (File or Directory)
     * @param v Vertex to be removed
     * @return String message to result the result of the method.
     */
    public String removeVertex(Vertex v)
    {
        if(!lookupVertex(v)){
            return "Vertex does not exist - cannot remove a Vertex if it doesn't exist";
        }


        return "Successfully removed Vertex";
    }

    public TwoPhaseSet<T> get2PSetMinus()
    {
        TwoPhaseSet<T> twoPhaseSet = new TwoPhaseSet<T>(v)
    }

    public void merge(TwoPTwoPGraph<T> graph)
    {
        vertices.merge(graph.vertices);
        edges.merge(graph.edges);
    }

    public TwoPTwoPGraph<T> copy() {
        TwoPTwoPGraph<T> copy = new TwoPTwoPGraph<T>();

        copy.vertices = vertices.copy();  //copy() is the GSet's method copy.
        copy.edges = edges.copy();
        return copy;
    }
}
