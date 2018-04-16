package cp;

import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Implementation of the m1 functionality of the exam project.
 *
 * @author Thomas Stenhaug
 */
public class MinCollector {

    // This sentinel means that nothing more will be added to a queue.
    private final static NumberFile POISON_PILL = NumberFile.getPoisonPill();

    /**
     * This method recursively visits a directory to find all the text
     * files contained in it and its subdirectories.
     * <p>
     * You must consider only files ending with a ".txt" suffix.
     * You are guaranteed that they will be text files.
     * <p>
     * You can assume that each text file contains a (non-empty)
     * comma-separated sequence of
     * numbers. For example: 100,200,34,25
     * There won't be any new lines, spaces, etc., and the sequence never
     * ends with a comma.
     * You are guaranteed that each number will be at least or equal to
     * 0 (zero), i.e., no negative numbers.
     * <p>
     * The search is recursive: if the directory contains subdirectories,
     * these are also searched and so on so forth (until there are no more
     * subdirectories).
     * <p>
     * This method returns a list of results.
     * The list contains a result for each text file that you find.
     * Each {@link Result} stores the path of its text file,
     * and the lowest number (minimum) found inside of the text file.
     *
     * @param dir the directory to search
     * @return a list of results ({@link Result}), each giving the lowest number found in a file
     */
    static List<Result> collect(Path dir) {
        Deque<Result> results = new ConcurrentLinkedDeque<>();
        BlockingDeque<NumberFile> NumberFiles = new LinkedBlockingDeque<>();

        // Start consumers first, so they are ready to begin consuming
        // NumberFiles as soon as these become available.
        ExecutorService consumers = Executors.newWorkStealingPool();
        IntStream.range(0, Runtime.getRuntime().availableProcessors())
            .forEach(i -> consumers.submit(() ->
                collectMinValues(NumberFiles, results)));

        // Gather NumberFiles.  Performed in this (main) thread.
        NumberFile.collectNumberFiles(dir, NumberFile.TXT_MATCHER, NumberFiles);

        // At this point, no more paths will be added to the queue, so we feed the poison pill,
        // prompting our consumers to exit.
        NumberFiles.addLast(POISON_PILL);

        Util.shutdownAndAwait(consumers);

        System.out.printf("MinCollector.collect() computed min-values in %d .txt-files.\n",
            results.size());
        return new LinkedList<>(results);
    }

    /**
     * Take {@link NumberFile}s, until a poison pill is found, compute min-value for each file, and
     * add results to minValues.
     * @param paths queue of paths to collect min-values from
     * @param minValues queue to put the min-values into
     */
    private static void collectMinValues(BlockingDeque<NumberFile> paths, Deque<Result> minValues) {

        // Take paths as long as we're not cancelled
        while (!Thread.currentThread().isInterrupted()) {
            NumberFile file;

            try {
                file = paths.take();
            } catch (InterruptedException e) {
                System.err.println("Warning: collectMinValues shutting down prematurely");
                e.printStackTrace();
                break;
            }

            // We have eaten a poison pill, put it back and exit.
            if (file == POISON_PILL) {
                paths.addLast(POISON_PILL);
                break;
            } else {
                Result result = new ExamResult(file.path(), file.min());
                minValues.add(result);
            }
        }
    }
}
