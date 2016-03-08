import crdt.Counters.PNCounter;

/***
 create new CRDT counter type
 increment it in a loop, print out to console.

 */
public class main {

    public static void main(String[] args){

        main main1 = new main();
        main1.testingPNCounter();
    }


    /*
    A method for testing the expected output of a PNCounter.
     */
    public void testingPNCounter()
    {
        PNCounter<String> replica1 = new PNCounter<String>();
        PNCounter<String> replica2 = new PNCounter<String>();

        replica1.increment("hostname1");System.out.println("Replica1 value =" + replica1.value());
        replica1.increment("hostname1");System.out.println("Replica1 value =" + replica1.value());


        replica2.increment("hostname2");System.out.println("\nReplica2 value =" + replica2.value());
        replica2.increment("hostname2");System.out.println("Replica2 value =" + replica2.value());

        replica1.merge( replica2 );
        System.out.println("\nReplica1 value after merge = " + replica1.value());
    }

}
