package cp.week8;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise1
{
    /*
      - Create a Counter class storing an integer (a field called i), with an increment and decrement method.
      - Make Counter Thread-safe (see thread safety in the book chapter readings)
      - Does it make a different to declare i private or public?
    */

    static class Counter
    {
        final Lock lock = new ReentrantLock();
        private volatile int i;

        public void inc () {
            lock.lock();
            i++;
            System.out.println(i);
            lock.unlock();
        }

        public void dec () {
            lock.lock();
            i--;
            System.out.println(i);
            lock.unlock();
        }
    }
    public static void main(String[] args) {
        Counter c = new Counter();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++)
                c.inc();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++)
                c.dec();
        });
        t1.start();
        t2.start();
    }

}


