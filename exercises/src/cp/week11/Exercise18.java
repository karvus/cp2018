package cp.week11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise18
{
	/*
	- Modify Exercise 17 such that:
		* When a consumer terminates, it prints on screen the sum of the lengths
		  of all files it read.
	*/
	 private static void collectTXTPaths (Path dir, BlockingDeque paths) {
        try {
            Files.list(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * consume all paths (.txt-files), respond to cancellation/interruption
     * @param paths a collection of paths to consume
     */
    private static void consumePaths (BlockingDeque<Path> paths) {
        Path path = null;
        boolean cancelled = false; // are we cancelled/interrupted?
        int length = 0;

        while (!cancelled) {
            try {
                path = paths.take();
            } catch (InterruptedException e) {
                // interrupted means that we were cancelled, so empty paths and return
                cancelled = true;
            }
            if (path != null) {
                try {
                    length += Files.size(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            path = null;
        }
        // make sure the Deque is empty before exiting
        while ((path = paths.poll()) != null) {
            try {
                length += Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(length);
    }

    /**
     * Submit n tasks to executor, and return the list of futures
     * @param executor the executor to submit tasks to
     * @param task the task to submit
     * @param n the number of tasks to submit
     * @return List of futures, representing each tasks submitted
     */
    private static List<Future<?>> submitTasks (ExecutorService executor, Runnable task, int n) {
        List<Future<?>> consumers = new LinkedList<>();
        IntStream.range(0, n).forEach(i -> consumers.add(executor.submit(task)));
        return consumers;
    }

    private static void shutdownAndAwait(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise18 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        BlockingDeque<Path> paths = new LinkedBlockingDeque<>();

        // start consumers, so they can begin work as soon as it is available
        ExecutorService consumerExecutor = Executors.newCachedThreadPool(); // stealing doesn't work for some reason
        List<Future<?>> consumers = submitTasks(consumerExecutor, () -> consumePaths(paths), 4);

        ExecutorService producersExecutor = Executors.newWorkStealingPool();

        try {
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isDirectory)
                    .forEach(p -> producersExecutor.submit(() -> collectTXTPaths(p, paths)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        shutdownAndAwait(producersExecutor);

        consumers.forEach(f -> f.cancel(true));
        shutdownAndAwait(consumerExecutor);
    }
}
