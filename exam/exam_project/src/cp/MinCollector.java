package cp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

// Implementation of the m1 functionality of the exam project
public class MinCollector {

    // This sentinel means that nothing more will be added to a queue.
    private final static NumberFile POISON_PILL = NumberFile.getPoisonPill();

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
        Utils.collectNumberFiles(dir, NumberFiles, NumberFile.TXT_MATCHER);

        // At this point, no more paths will be added to the queue, so we feed the poison pill,
        // prompting our consumers to exit.
        NumberFiles.addLast(POISON_PILL);

        Utils.shutdownAndAwait(consumers);

        System.out.printf("MinCollector.collect() computed min-values in %d .txt-files.\n",
                results.size());
        return new LinkedList<>(results);
    }

    // Take CSIFiles, until a poison pill is found, compute min-value for each file, and
    // add results to minValues.
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

    public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/cp2018/exam/data_example/");
        List<Result> results = Exam.m1(dir);
        results.forEach(System.out::println);
    }
}
