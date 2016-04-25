package crdt.Graph;

public class Edge {

    public Vertex from;
    public Vertex to;
    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    /**
     * check to see whether the class of the argument is equal (or compatible) to the
     * implementing class before casting it.
     */
    @Override
    public boolean equals(Object obj) {
        Edge e = (Edge)obj;
        return e.from.toString().equals(from.toString()) && e.to.toString().equals(to.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return (this.from.toString() + " - " + this.to.toString());
    }



}
