import crdt.Counters.PNCounter;
import crdt.sets.TwoPhaseSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/***
 create new CRDT counter type
 increment it in a loop, print out to console.

 */
public class main {

    public static void main(String[] args){

        main main1 = new main();
        //main1.testingPNCounter();
        main1.testingTwoPhaseSet();

        Set<String> test = new HashSet<String>();

        test.add("Julia");

        String a = "Julia";

        System.out.println(test.size());
        test.remove(a);
        System.out.println(test.size());

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

    /*
    A method for testing the expected output of a TwoPhaseSet.
     */
    public void testingTwoPhaseSet()
    {
        TwoPhaseSet<String> replica1 = new TwoPhaseSet<String>();
        TwoPhaseSet<String> replica2 = new TwoPhaseSet<String>();

        replica1.add("a");
        replica1.add("b");
        replica1.add("c");

        replica1.merge(replica2);
        replica2.merge(replica1);

        System.out.println("**********First**********");

        System.out.println("replica1 elements = " + replica1.getSetMinus());
        System.out.println("replica2 elements = " + replica2.getSetMinus());


        replica1.remove("b");

        replica2.add("d");
        replica2.add("e");

        replica2.remove("d");

        replica1.merge(replica2);
        replica2.merge(replica1);
        System.out.println("/n**********Second**********");
        System.out.println("replica1 elements = " + replica1.getSetMinus());
        System.out.println("replica2 elements = " + replica2.getSetMinus());
    }
}
