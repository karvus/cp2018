package cp.week11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Exercise19 {
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

    // token that no more elements will be queued
    private static Path POISON_PILL = Paths.get("POISON_PILL");

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Exercise19 <directory>");
            System.exit(1);
        }
        Path start = Paths.get(args[0]);

        BlockingDeque<Path> txtFiles = new LinkedBlockingDeque<>();

        // start consumers, first so they can begin work as soon as it becomes available
        ExecutorService consumerExecutor = Executors.newCachedThreadPool();

        IntStream.range(0, Runtime.getRuntime().availableProcessors())
                .forEach(i -> consumerExecutor.submit(() ->
                        sumFileSizes(txtFiles, consumerExecutor)));

        // in this (main) thread, start producing .txt-file paths
        collectTXTPaths(start, txtFiles);

        // at this point, no more paths will be added to the queue
        txtFiles.addLast(POISON_PILL);

        shutdownAndAwait(consumerExecutor);
    }

    // sum sizes of files, until eating a poison pill, possibly submitting a new task to executor
    private static void sumFileSizes(BlockingDeque<Path> files, ExecutorService executor) {

        int nBytes = 0; // the sum of bytes in files we've seen

        while (!Thread.currentThread().isInterrupted()) {
            Path txtFile;
            try {
                txtFile = files.take();
            } catch (InterruptedException e) {
                // just exit if we're interrupted
                e.printStackTrace();
                break;
            }

            if (txtFile == POISON_PILL) {
                files.addLast(POISON_PILL);
                break;
            }

            String head = getHead(txtFile, 4);

            // when we see a file starting with TERM, submit new task and exit directly
            if (head.equals("TERM")) {
                try {
                    executor.submit(() -> sumFileSizes(files, executor));
                } catch (RejectedExecutionException e) {
                    // might happen if another thread took the poison pill in between.
                    System.out.println("Tried to submit a task, but got rejected.");
                }
                break;
            }

            if (head.equals("SKIP"))
                continue;

            // otherwise, add the size of the file to our running sum
            try {
                nBytes += Files.size(txtFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // we're exiting normally
        System.out.println("Counted " + nBytes + " bytes.");
    }

    private static void collectTXTPaths(Path dir, BlockingDeque<Path> paths) {
        try {
            Files.walk(dir)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                    .forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // return the first n characters of file
    private static String getHead(Path file, int n) {
        String head;
        try {
            head = Files.newBufferedReader(file).readLine().substring(0, n);
        } catch (IOException e) {
            e.printStackTrace();
            // this should probably throw a runtime-exception
            head = "";
        }
        return head;
    }

    // shutdown executor, and await its termination
    private static void shutdownAndAwait(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
