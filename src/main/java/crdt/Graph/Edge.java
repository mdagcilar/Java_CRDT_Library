package crdt.Graph;

public class Edge {

    public final Vertex from;
    public final Vertex to;
    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }
    @Override
    public boolean equals(Object obj) {
        Edge e = (Edge)obj;
        return e.from == from && e.to == to;
    }
}
