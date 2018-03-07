package cp.week10;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

public class Exercise12 {
	/*
	- Modify Exercise11 such that each sub-directory is visited
	  by a dedicated thread.
	*/

    /** Recursively traverse the filesystem from a point, and return all found subdirectories
     *
     * @param start collect subdirectories from this path
     * @return subdirectories of start
     */
    private static List<Path> getDirs(Path start) {
        List<Path> dirs = null;
        try {
            dirs = Files.walk(start)
                    .filter(p -> Files.isDirectory(p))
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    /** Collect all .txt-files of a directory into a queue.
     *
     * @param dir directory in which we look for .txt-files
     * @param files the thread-safe queue where found .txt files are put
     * @param doneSignal the countdown-latch we signal when we're done
     */
    private static void collectTXTFiles (Path dir, Deque files, CountDownLatch doneSignal) {
        try {
            Files.list(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(files::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doneSignal.countDown();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Exercise12 <directory>"); // set this up in IDEA run config
            System.exit(1);
        }

        List<Path> dirs = getDirs(Paths.get(args[0]));
        CountDownLatch doneSignal = new CountDownLatch(dirs.size());
        Deque<Path> files = new ConcurrentLinkedDeque<>();
        dirs.forEach(p -> new Thread(() -> collectTXTFiles(p, files, doneSignal)).start());

        // wait for collectors to complete
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // just print the files for now
        files.stream().forEach(System.out::println);
    }
}

