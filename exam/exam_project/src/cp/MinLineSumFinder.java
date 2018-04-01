package cp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MinLineSumFinder {

    /**
     * This method recursively visits a directory for text files with suffix
     * ".dat" (notice that it is different than the one before)
     * contained in it and its subdirectories.
     * <p>
     * You must consider only files ending with a .dat suffix.
     * You are guaranteed that they will be text files.
     * <p>
     * Each .dat file contains some lines of text,
     * separated by the newline character "\n".
     * You can assume that each line contains a (non-empty)
     * comma-separated sequence of
     * numbers. For example: 100,200,34,25
     * <p>
     * This method looks for a .dat file that contains a line whose numbers,
     * when added together (total), amount to at least (>=) parameter min.
     * Once this is found, the method can return immediately
     * (without waiting to analyse also the other files).
     * The return value is a result that contains:
     * - path: the path to the text file that contains the line that respects the condition;
     * - number: the line number, starting from 1 (e.g., 1 if it is the first line, 3 if it is the third, etc.)
     */

    private static final Path POISON_PILL = Paths.get("POISON_PILL");

    static Result find(Path dir, int min) {

        // the future that will hold the result.  This is used by both main thread and consumers.
        CompletableFuture<Result> futureResult = new CompletableFuture<>();

        // the deque that will be filled by main thread, and consumed by findFile tasks
        BlockingDeque<Path> paths = new LinkedBlockingDeque<>();

        // start consumers
        ExecutorService fileConsumers = Executors.newCachedThreadPool();
        ExecutorService lineConsumers = Executors.newWorkStealingPool();

        int nConsumers = Runtime.getRuntime().availableProcessors();
        IntStream.range(0, nConsumers).forEach(i ->
            fileConsumers.submit(() ->
                consumeFiles(paths, min, futureResult, lineConsumers))
        );

        try {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(dir)
                .filter(p -> Files.isRegularFile(p) && Utils.DAT_MATCHER.matches(p))
                .allMatch(p-> paths.add(p) && !futureResult.isDone());
        } catch (IOException e) {
            e.printStackTrace();
        }

        paths.add(POISON_PILL);

        // Wait for futureResult to be completed in one of the consumers.
        Result result = null;
        try {
            result = futureResult.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("Error: couldn't find a file with a line that sums up to " + min +
                " within reasonable time.");
        }

        // We got the result; forcefully shut down any remaining consumers, and return final result.
        fileConsumers.shutdownNow();
        lineConsumers.shutdownNow();
        return result;
    }

    private static void consumeFiles(BlockingDeque<Path> paths,
                                     int min,
                                     CompletableFuture<Result> result,
                                     ExecutorService lineConsumers) {
        while (!result.isDone()) {
            try {
                Path path = paths.take();
                if (path == POISON_PILL)
                    break;
                List<String> lines = Files.readAllLines(path);
                int i = 1;
                for (String line : lines) {
                    final int lineNumber = i;
                    if (result.isDone())
                        break;
                    lineConsumers.submit(() -> consumeLine(line, min, lineNumber, result, path));
                    i++;
                }
            } catch (InterruptedException e) {
                // It's expected that we're cancelled early by a shutdown from the main thread
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void consumeLine(String line, int min, int lineNumber, CompletableFuture<Result> result, Path path) {
        if (Utils.computeSum(line) >= min) {
            // System.out.printf("%s: %d: %s", path, lineNumber, line);
            result.complete(new ExamResult(path, lineNumber));
        }
    }

    // ===================================================================================

    public static void main(String[] args) {
        Path dir = Paths.get("/home/thomas/git/dm519-exam/data_example/");
        Path testFile = Paths.get("/home/thomas/git/dm519-exam/data_example/numbers_1.txt");
        // System.out.println(fileMatches(testFile,100, 5));
        // System.out.println(FileStats.get(testFile));
        // Result result = find(dir,  40579386);
        Result result = find(dir, 40580387); // seems to be thi
        System.out.println(result);
    }
}