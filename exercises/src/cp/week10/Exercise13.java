package cp.week10;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.Deque;
import java.util.concurrent.*;

public class Exercise13
{
	/*
	- Modify Exercise12 to use an Executor instead of
	  manually-controlled threads.
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
            System.out.println("Usage: java Exercise13 <directory>"); // set this up in IDEA run config
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
