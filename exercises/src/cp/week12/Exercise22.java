package cp.week12;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise22
{
	/*
	- Modify Exercise21 such that you also look for text files with a ".dat" suffix, not just ".txt"
	- When you find a ".dat" file, launch an executor task that computes how many times the letter "b" is present in the file.
	- At the end, the program should print *separately*: The number of "a", and the number of "b".
	*/

	// Class that encapsulates the shared data in the program.
    static class Shared {
        final ConcurrentLinkedQueue<Future<Integer>> futureBs = new ConcurrentLinkedQueue<>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

	static void countInto(Path file, char c, AtomicInteger ai) {
        ai.addAndGet(util.count(file, c));
    }

    private static int countOrMaybeAddFuture(Path file, Shared shared) {
        if (file.toString().endsWith(".dat")) {
            shared.futureBs.add(shared.executor.submit(() -> util.count(file, 'b')));
            return 0;
        } else if (file.toString().endsWith(".txt")) {
            return util.count(file, 'a');
        } else
            return 0;
    }

	private static int countInDir(Path dir, char needle, Shared shared) {
        int count;
        try {
            count = Files.list(dir)
                .filter(p -> Files.isRegularFile(p))
                .mapToInt(p -> countOrMaybeAddFuture(p, shared))
                .sum();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return count;
    }


	public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week12/as/");

        Shared shared = new Shared();
        List<Future> futures;
        try {
            futures = Files.walk(dir)
                .filter(Files::isDirectory)
                .map(p -> shared.executor.submit(() -> countInDir(p, 'a', shared)))
                .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int aSum = 0;
        for (Future f : futures) {
            try {
                aSum += (int)f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Occurrences of 'a': " + aSum);

        int bSum = 0;
        for (Future f : shared.futureBs) {
            try {
                bSum += (int)f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Occurrences of 'b': " + bSum);

        shared.executor.shutdown();


    }
}
