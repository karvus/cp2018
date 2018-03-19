package cp.week12;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exercise20
{
	/*
	- Write a method that finds all text files (files with a ".txt" suffix) in a given directory.
	- Whenever a text file is found, launch a task in an executor and get a future for the task's result.
	- The task should compute how many times the letter "a" is present in the file, and return it as an integer.
	- The main thread should wait for all futures to complete and print on screen the sum of all results.
	*/

    public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week12/as/");

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future> futures;
        try {
            futures = Files.list(dir)
                .map(p -> executor.submit(() -> util.count(p, 'a')))
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
