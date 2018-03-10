package cp.week11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.concurrent.*;

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
    private static void collectTXTFiles (Path dir, Deque paths) {
        try {
            Files.list(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise17 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        ExecutorService producersExecutor = Executors.newWorkStealingPool();

        Deque<Path> files = new ConcurrentLinkedDeque<>();
        try {
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isDirectory)
                    .forEach(p -> producersExecutor.submit(() -> collectTXTFiles(p, files)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        producersExecutor.shutdown();
        // wait for collectors to complete
        try {
            producersExecutor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
