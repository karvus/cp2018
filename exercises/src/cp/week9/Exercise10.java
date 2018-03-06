package cp.week9;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.IntStream;
import java.util.Random;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise10
{
	/*
	- Modify producer_consumer/Sequential from Exercise9 such that such that the producer randomly decides
	  the number of elements that it will puts in the list.
	- Hint: use java.util.Random. Documentation: https://docs.oracle.com/javase/7/docs/api/java/util/Random.html
	- Make it so the number of elements produced by the producer cannot exceed 10000.
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
                int count = new Random().nextInt(10000);

                IntStream.range(0, count).forEach(
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
