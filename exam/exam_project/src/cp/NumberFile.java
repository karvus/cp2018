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
import java.util.concurrent.ConcurrentLinkedDeque;

// Represents a text-file with one or more comma-separated integer values.
public class NumberFile {

    public static final PathMatcher DAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.dat");
    public static final PathMatcher TXTDAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.{txt,dat}");
    public static final PathMatcher TXT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.txt");

    private final Path path;
    private ConcurrentLinkedDeque<Integer> numbers;
    private List<String> lines;

    NumberFile(Path path) {
        this.path = path;
    }

    // constructor meant for making poison pills
    private NumberFile() {
        this.path = null;
        this.numbers = null;
    }

    // Return an object suitable for use as a poison pill.
    static NumberFile getPoisonPill() {
        return new NumberFile();
    }

    Path path() {
        return path;
    }

    // Return the list of numbers in the file, possibly reading it in, if not already read.
    ConcurrentLinkedDeque<Integer> getNumbers() {
        if (numbers == null) {  // we haven't been called before
            numbers = new ConcurrentLinkedDeque<>();
            try {
                lines = Files.readAllLines(Objects.requireNonNull(path));
                for (String line : lines) {
                    Arrays.stream(line.split(",")).
                        map(Integer::parseInt).
                        forEach(numbers::add);
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
