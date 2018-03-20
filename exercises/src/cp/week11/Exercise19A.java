package cp.week11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

public class Exercise19A {
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

    final static Path POISON_PILL = Paths.get("POISON_PILL");

    public static void fileFinder(Path path, BlockingDeque<Path> deque) {
        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(subPath -> subPath.toString().endsWith(".txt"))
                    .forEach(deque::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        deque.add(POISON_PILL);
    }

    public static void consume(BlockingDeque<Path> deque, ExecutorService executorService) {
        int charactersRead = 0;
        boolean terminated = false;
        while (!Thread.interrupted()) {
            try {
                Path path = deque.take();

                if (path == null) {
                    continue;
                }

                if (path == POISON_PILL) {
                    deque.add(POISON_PILL);
                    break;
                }

                List<String> lines = Files.readAllLines(path);

                if (lines.get(0).startsWith("SKIP")) {
                    continue;
                }

                if (lines.get(0).startsWith("TERM")) {
                    terminated = true;
                    break;
                }

                System.out.println(lines);

                // Number of charaters in file
                // charactersRead += lines.stream().mapToInt(String::length).sum();

                // Size of file
                charactersRead += Files.size(path);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("I read " + charactersRead + " characters");

        if (terminated) {
            executorService.submit(() -> consume(deque, executorService));
        }
    }

    public static void main(String[] args) {
        BlockingDeque<Path> deque = new LinkedBlockingDeque<>();

        fileFinder(Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week10/data_example"), deque);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(() -> consume(deque, executorService));
        executorService.submit(() -> consume(deque, executorService));
        executorService.submit(() -> consume(deque, executorService));
        executorService.submit(() -> consume(deque, executorService));

        executorService.shutdown();
        try {
            executorService.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
