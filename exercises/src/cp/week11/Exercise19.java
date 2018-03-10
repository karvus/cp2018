package cp.week11;

import java.io.BufferedReader;
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
public class Exercise19
{
	/*
	- Modify Exercise 18 such that:
		* If a file starts with the string "SKIP", the consumer does not process
		  it.
		* If a file starts with the string "TERM", the consumer terminates
		  immediately (printing on screen the sum of the lengths of all files
		  visited so far).
		* If a consumer terminates because of "TERM", then the consumer starts
		  another consumer.
	
	- Make sure that all consumers terminate when the shared blocking deque is
	  empty and no more files will be added to it.
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

    private static Path poisonPill = Paths.get("poisonPill");

    private static String getHead(Path path, int n) {
        BufferedReader r = null;
        try {
            r = Files.newBufferedReader(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        char[] cbuf = new char[4];
        try {
            r.read(cbuf, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(cbuf);
    }

    /**
     * consume all paths (.txt-files), respond to cancellation/interruption
     * @param paths a collection of paths to consume
     */
    private static void consumePaths (BlockingDeque<Path> paths, ExecutorService executor)  {

        int length = 0;
        boolean terminated = false;
        boolean poisoned = false;

        while (!terminated && !poisoned) {
            Path path = null;
            try {
                path = paths.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (path == poisonPill) {
                poisoned = true;
                paths.addLast(poisonPill);
            } else {
                String head = getHead(path, 4);
                switch (head) {
                    case "TERM":
                        terminated = true;
                        break;
                    case "SKIP":
                        break;
                    default:
                        try {
                            length += Files.size(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
        System.out.println(length);
        if (terminated) {
            executor.submit(() -> consumePaths(paths, executor));
        }
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
            System.out.println("Usage: java Exercise19 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        BlockingDeque<Path> paths = new LinkedBlockingDeque<>();

        // start consumers, so they can begin work as soon as it is available
        ExecutorService consumerExecutor = Executors.newCachedThreadPool(); // stealing doesn't work for some reason
        List<Future<?>> consumers = submitTasks(consumerExecutor, () -> consumePaths(paths, consumerExecutor), 4);

        ExecutorService producersExecutor = Executors.newWorkStealingPool();

        try {
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isDirectory)
                    .forEach(p -> producersExecutor.submit(() -> collectTXTPaths(p, paths)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        shutdownAndAwait(producersExecutor);

        paths.addLast(poisonPill);

        // consumers.forEach(f -> f.cancel(true));
        shutdownAndAwait(consumerExecutor);
    }
}
