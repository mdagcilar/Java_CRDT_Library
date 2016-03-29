package crdt.Graph;

import java.util.HashSet;

public class Vertex {

    public final String name;
    public final HashSet<Edge> inEdges, outEdges;


    public Vertex(String name) {
        this.name = name;
        inEdges = new HashSet<Edge>();
        outEdges = new HashSet<Edge>();
    }

    public Vertex addEdge(Vertex node){
        Edge e = new Edge(this, node);
        outEdges.add(e);
        node.inEdges.add(e);
        return this;
    }
    @Override
    public String toString() {
        return name;
    }
}
