package crdt;

import crdt.Graph.TwoPTwoPGraph;
import crdt.Graph.Vertex;

/***
 create new CRDT counter type
 increment it in a loop, print out to console.

 */
public class main {

    public static void main(String[] args){

        main main1 = new main();
        main1.testingGraph();
    }


    public void testingGraph()
    {
        TwoPTwoPGraph<String> replica1 = new TwoPTwoPGraph<String>();
        TwoPTwoPGraph<String> replica2 = new TwoPTwoPGraph<String>();

        System.out.println("Vertices of replica 1 added: " + replica1.vertices.added.get());

        System.out.println("\nVertices of replica 2 added: " + replica2.vertices.added.get());

        System.out.println("\nare they equal?**************: " + replica1.getStartSentinel().equals(replica2.getStartSentinel()) + "\n");

        replica1.merge(replica2);
        System.out.println("\nmerge:");
        System.out.println("Vertices: " + replica1.getGraph().vertices.added.get());
        System.out.println("Edges: " + replica1.getGraph().edges.added.get());
        System.out.println("\n");

        //trying to add a new Vertex 'v' between the start and end sentinels
        Vertex k = new Vertex("k");
        Vertex m = new Vertex("m");
        //replica1.addBetweenVertex(replica1.getStartSentinel(), v, replica1.getEndSentinel());
        System.out.println((replica1.addBetweenVertex(replica1.getStartSentinel(), k, replica1.getEndSentinel())));
        System.out.println((replica1.addBetweenVertex(k, m, replica1.getEndSentinel())));

        System.out.println("\nmerge:");

        System.out.println(replica1.getGraph().vertices.added.get());
        System.out.println(replica1.getGraph().edges.added.get());

    }
}
