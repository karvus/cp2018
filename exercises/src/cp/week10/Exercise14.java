package cp.week10;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.Deque;
import java.util.concurrent.*;

public class Exercise14
{
    /*
	- Experiment using different kinds of executors, see:
	  https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html
	- Compare running time of at least:
	  * Single threaded executor.
	  * Fixed thread pool with a number of threads equal to number of CPU cores.
	  * Cached thread pool.
	  * Work stealing pool.
	*/

	/** Recursively traverse the filesystem from a point, and return all found subdirectories
     *
     * @param start collect subdirectories from this path
     * @return subdirectories of start
     */
    private static List<Path> getDirs(Path start) {
        List<Path> dirs = null;
        try {
            dirs = Files.walk(start)
                    .filter(p -> Files.isDirectory(p))
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    /** Collect all .txt-files of a directory into a queue.
     *
     * @param dir directory in which we look for .txt-files
     * @param files the thread-safe queue where found .txt files are put*
     */
    private static void collectTXTFiles (Path dir, Deque files) {
        try {
            Files.list(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(files::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long benchmarkExecutor(List<Path> dirs, ExecutorService executor) {
        Deque<Path> files = new ConcurrentLinkedDeque<>();
        long t0 = System.currentTimeMillis();
        dirs.forEach(p -> executor.submit(() -> collectTXTFiles(p, files)));

        executor.shutdown();
        // wait for collectors to complete
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // just print the files for now
        // files.stream().forEach(System.out::println);
        long t = System.currentTimeMillis() - t0;
        return t;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise14 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        List<Path> dirs = getDirs(Paths.get(args[0]));

        // FixedThreadPool
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService producersExecutor = Executors.newFixedThreadPool(cores);
        long fixedThreadPoolMillis = benchmarkExecutor(dirs, producersExecutor);

        // SingleThread
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        long singleThreadMillis = benchmarkExecutor(dirs, singleThreadExecutor);

        // CachedThreadPool
        ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();
        long cachedThreadPoolMillis = benchmarkExecutor(dirs, cachedThreadPoolExecutor);

        // WorkStealingPool
        ExecutorService workStealingPoolExecutor = Executors.newWorkStealingPool();
        long workStealingPoolMillis = benchmarkExecutor(dirs, workStealingPoolExecutor);

        // Print results
        System.out.println("SingleThread: " + singleThreadMillis);
        System.out.println("FixedThreadPool: " + fixedThreadPoolMillis);
        System.out.println("CachedThreadPool: " + cachedThreadPoolMillis);
        System.out.println("WorkStealingPoolMillis: " + workStealingPoolMillis);
    }
}

