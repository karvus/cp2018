package cp.week9;

import java.util.Deque;
import java.util.LinkedList;


// Exercise7
// 	- Modify producer_consumer/Sequential to use for-loops instead of IntStream::forEach

public class Sequential
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

	private static final Deque<Product> THE_LIST = new LinkedList<>();

	private static void produce( Deque< Product > list )
	{
	    for (int i = 0; i < 100; i++) {
	        list.add(new Product("Water bottle " + i, "Fresh"));
            list.add(new Product( "Flower bouquet " + i, "Roses" ) );
        }
	}

	private static void consume( Deque< Product > list )
    {
        int size = list.size();
        for (int i =0; i < size; i++) {
            System.out.println(list.remove().toString());
        }
	}

    public static void main(String[] args) {
        produce( THE_LIST );
        consume( THE_LIST );
    }
}

