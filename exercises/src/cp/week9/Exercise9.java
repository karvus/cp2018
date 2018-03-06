package cp.week9;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise9

{
    /*
	- Make your implementation of producer_consumer/Sequential from Exercise8 thread-safe using synchronized blocks.
	- Question: Does your implementation guarantee that all produced items in the list are also consumed? Why?

	There is still a possibility that t2 runs before t1, so that the list is empty.
	*/
     public static class Sequential
    {
        private static class Product {
            private final String name;
            private final String attributes;

            public Product( String name, String attributes )
            {
                this.name = name;
                this.attributes = attributes;
            }

            public String toString()
            {
                return name + " : " + attributes;
            }
        }

        private static void produce( Deque< Product > list )
        {
            // int stream range to add water bottles and flower bouquets
            synchronized (list) {
                IntStream.range(0, 100).forEach(
                        n -> {
                            list.add(new Product("Water bottle " + n, "Fresh"));
                            list.add(new Product("Flower bouquet " + n, "Roses"));
                        }
                );
            }
        }

        private static void consume( Deque< Product > list )
        {
            synchronized (list) {
                list.forEach( product -> System.out.println( product.toString() ) );
            }
        }
    }
    private static final Deque<Sequential.Product> THE_LIST = new LinkedList<>();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> Sequential.produce( THE_LIST ));
        Thread t2 = new Thread(() -> Sequential.consume( THE_LIST ));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
