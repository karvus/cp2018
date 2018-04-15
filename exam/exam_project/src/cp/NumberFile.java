package cp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a text-file with one or more lines of comma-separated integer values.
 */
public class NumberFile {

    /** Matches files ending in ".dat" */
    public static final PathMatcher DAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.dat");

    /** Matches files ending in ".txt" or ".dat" */
    public static final PathMatcher TXTDAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.{txt,dat}");

    /** Matches files ending in ".txt" */
    public static final PathMatcher TXT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.txt");

    private final Path path;
    private ConcurrentLinkedQueue<Integer> numbers;

    NumberFile(Path path) {
        this.path = path;
    }

    // constructor meant for making poison pills
    private NumberFile() {
        this.path = null;
        this.numbers = null;
    }

    /**
     * Return an object suitable for use as a poison pill
     * @return An object suitable as a poison pill
     */
    public static NumberFile getPoisonPill() {
        return new NumberFile();
    }

    /** Recursively collect NumberFiles matching a criteria into a shared datastructure.
     *
     * @param start Path from which to start collecting
     * @param numberFiles The shared structure to collect into (an out-value)
     * @param matcher Criteria from which to filter
     */
    public static void collectNumberFiles(Path start,
                                   BlockingDeque<NumberFile> numberFiles,
                                   PathMatcher matcher) {
        AtomicInteger count = new AtomicInteger();
        try {
            Files.walk(start)
                .parallel()
                .filter(p -> Files.isRegularFile(p) && matcher.matches(p))
                .forEach(p -> {
                    try {
                        numberFiles.add(new NumberFile(p));
                        count.getAndIncrement();
                    } catch (NumberFormatException e) {
                        System.err.printf("Warning: malformed file \"%s\", ignoring.\n", p);
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Collected %d comma separated integer files.\n", count.get());
    }

    Path path() {
        return path;
    }

    /**
     * Return the list of numbers in the file, possibly reading it in, if not already read.
     *
     * @return list of numbers in the file
     */
    // This is never invoked in a concurrent manner in the current codebase, but making it
    // synchronized to be on the safe side, as we change state.
    synchronized public ConcurrentLinkedQueue<Integer> getNumbers() {
        if (numbers == null) {  // we haven't been called before, so do the heavy lifting
            numbers = new ConcurrentLinkedQueue<>();
            try {
                List<String> lines = Files.readAllLines(Objects.requireNonNull(path));
                for (String line : lines) {
                    Arrays.stream(line.split(","))
                        .map(Integer::parseInt)
                        .forEach(numbers::add);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return numbers;
    }

    int min() {
        return Collections.min(getNumbers());
    }

    int max() {
        return Collections.max(getNumbers());
    }

    int sum() {
        return getNumbers().stream().parallel().mapToInt(Integer::intValue).sum();
    }

    int count() {
        return getNumbers().size();
    }
}
