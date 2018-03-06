package cp.week8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise5
{
	/*
	- Apply the technique for fixing Listing 4.14 to Listing 4.15 in the book, but to the following:
	- Create a thread-safe Counter class that stores an int and supports increment and decrement.
	- Create a new thread-safe class Point, which stores two Counter objects.
	- The two counter objects should be public.
	- Implement the method boolean areEqual() in Point, which returns true if the two counters store the same value.
	- Question: Is the code you obtained robust with respect to client-side locking (see book)?
				Would it help if the counters were private?
	*/

    // thread-safe counter class
    public static class Counter {
        private int count = 0;

        public synchronized void inc () {
            count++;
        }
        public synchronized void dec () {
            count--;
        }

        public synchronized int get () {
            return count;
        }
    }

    public static class Point {
        public Counter x = new Counter();
        public Counter y = new Counter();

        public synchronized boolean areEqual() {
            return (x.get() == y.get());
        }
    }
    // it is not robust, because x and y can be changed "outside", so that the point can be in an inconsistent state.
    // changing them to private helps.
}
