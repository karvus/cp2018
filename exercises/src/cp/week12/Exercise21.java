package cp.week12;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.System.exit;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise21
{
	/*
	- Modify Exercise20 such that each sub-directory is assigned a dedicated executor task to returns the total of "a" for that sub-directory.
	- The end result must be the same (print the total number for the initial directory on screen).
	*/

	private static int countInDir(Path dir, char needle) {
	    int count;
        try {
            count = Files.list(dir)
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                .mapToInt(p-> util.count(p, needle))
                .sum();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return count;
    }

	 public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week12/as/");

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future> futures;
        try {
            futures = Files.walk(dir)
                .filter(Files::isDirectory)
                .map(p -> executor.submit(() -> countInDir(p, 'a')))
                .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        executor.shutdown();

        int sum = 0;
        for (Future f : futures) {
            try {
                sum += (int)f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println(sum);
    }    
}
