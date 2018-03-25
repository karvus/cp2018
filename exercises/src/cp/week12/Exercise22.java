package cp.week12;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

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

    /**
     *  Return the sum of a's in the supplied directory.  Also submits futures that
     *  count all b's in .dat-files inside said directory, to shared.futureBs.
     *
     * @param dir Path of directory to be processed
     * @param shared the shared data structures
     * @return the sum of a's in .txt-files inside dir
     */
	private static int countInDir(Path dir, Shared shared) {
        try {
            return Files.list(dir)
                .filter(p -> Files.isRegularFile(p))
                .mapToInt(p -> {
                    if (p.toString().endsWith(".dat")) {
                        shared.futureBs.add(shared.executor.submit(() ->
                                util.count(p, 'b')));
                        return 0;
                    } else if (p.toString().endsWith(".txt")) {
                        return util.count(p, 'a');
                    } else
                        return 0;
                })
                .sum();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Return the sum of all the supplied futures, all of which resolves to Integers
    private static int sumFutures(Collection<Future<Integer>> futures) {
        return futures.stream()
            .mapToInt(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            })
            .sum();
    }

	public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week12/as/");
        Shared shared = new Shared();
        List<Future<Integer>> futures;

        try {
            futures = Files.walk(dir)
                .filter(Files::isDirectory)
                .map(p -> shared.executor.submit(() -> countInDir(p, shared)))
                .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int nA = sumFutures(futures);
        System.out.println("Occurrences of 'a': " + nA);

        int nB = sumFutures(shared.futureBs);
        System.out.println("Occurrences of 'b': " + nB);

        shared.executor.shutdown();
    }
}
