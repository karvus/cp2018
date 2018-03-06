package cp.week9;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.IntStream;


/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise8
{
	/*
	- Modify producer_consumer/Sequential::main such that the produce and consume methods are run in two parallel threads.
	- Wait for the two threads to finish execution.
	- Question: Is the code thread-safe? Motivate your answer.
	*/

	/*
	 * No, it is not thread safe, in the sense that the queue might be exhausted by consumer before the
	 * producer is done.  In particular, it might be that no elements are consumed, because t2 reads the
	 * queue before the producer thread as produced anything.  In running I only saw the latter behavior,
	 * but I reckon it is possible, that a "half full" list might be consumed as well.
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
            IntStream.range( 0, 100 ).forEach(
                    n -> {
                        list.add( new Product( "Water bottle " + n, "Fresh" ) );
                        list.add( new Product( "Flower bouquet " + n, "Roses" ) );
                    }
            );
        }

        private static void consume( Deque< Product > list )
        {
            list.forEach( product -> System.out.println( product.toString() ) );
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
