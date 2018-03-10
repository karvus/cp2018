package cp.week11;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise16
{
	/*
	- Rewrite the "file finder" from week 10 such that whenever a file with
	".txt" suffix is found, a Path object representing the file is put on a
	shared BlockingDeque deque. (You can use LinkedBlockingDeque as
	implementation.)
	*/

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

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise16 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService producersExecutor = Executors.newFixedThreadPool(cores);

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
        // just print the files for now
        files.stream().forEach(System.out::println);
    }
}
// /home/thomas/git/cp2018/exercises/src/cp/week10/data_example