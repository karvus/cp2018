package cp.week11;

import jdk.nashorn.internal.ir.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise17
{
    /*
	- Modify Exercise 16 such that:
		* There are a few consumer threads running in parallel to the
		  file finder.
		* Each consumer consumes a Path object from the shared blocking deque
		  at a time.
		* When a consumer consumes a Path, it prints on screen the content of
	      the file.
	*/
    /** Collect all .txt-files of a directory into a queue.
     *
     * @param dir directory in which we look for .txt-files
     * @param paths the thread-safe queue where found .txt files are put
     */

    private static AtomicInteger count = new AtomicInteger(0);

    private static void collectTXTPaths (Path dir, BlockingDeque paths) {
        try {
            Files.list(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void consumePaths (BlockingDeque<Path> paths) {
        Path path = null;
        boolean cancelled = false;
        while (!cancelled) {
            try {
                path = paths.take();
            } catch (InterruptedException e) {
                // interrupted means that we were cancelled, so empty paths and return
                System.out.println("interrupted");
                cancelled = true;
            }
            if (path != null) {
                System.out.println(path);
                count.incrementAndGet();
            }
            path = null;
        }
        while ((path = paths.poll()) != null)
            System.out.println(path);
    }

    private static List<Future<?>> submitConsumersWithExecutor (ExecutorService executor, BlockingDeque paths) {
        List<Future<?>> consumers = new LinkedList<>();
        IntStream.range(0, 4).forEach(i -> consumers.add(executor.submit(() -> consumePaths(paths))));
        return consumers;
    }

    private static void cancelFuturesAndShutdown(List<Future<?>> futures, ExecutorService executor) {
        futures.stream().forEach(f -> f.cancel(true));
        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<Thread> startConsumerThreads (BlockingDeque paths) {
        List<Thread> consumers = new LinkedList<>();

        for (int i = 0; i < 4; i++) {
            consumers.add(new Thread(() -> consumePaths(paths)));
            consumers.get(i).start();
        }
        return consumers;
    }

    private static void stopThreads(List<Thread> threads) {
        threads.forEach(Thread::interrupt);
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise17 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        BlockingDeque<Path> paths = new LinkedBlockingDeque<>();
        ExecutorService producersExecutor = Executors.newWorkStealingPool();

        try {
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isDirectory)
                    .forEach(p -> producersExecutor.submit(() -> collectTXTPaths(p, paths)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService consumerExecutor = Executors.newCachedThreadPool();
        List<Future<?>> futures = submitConsumersWithExecutor(consumerExecutor, paths);

        // shutdown producers and wait for termination
        producersExecutor.shutdown();
        // wait for producers to complete
        try {
            producersExecutor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // stopThreads(threads);
        cancelFuturesAndShutdown(futures, consumerExecutor);

        System.out.println(count);

    }
}
