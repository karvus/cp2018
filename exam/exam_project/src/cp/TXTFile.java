package cp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

// Represents a text-file with comma-separated integer values.  (That is, a CSI file.)
public class TXTFile {

    private final Path path;
    private ConcurrentLinkedDeque<Integer> numbers;
    private List<String> lines;

    TXTFile(Path path) {
        this.path = path;
    }

    // constructor meant for making poison pills
    private TXTFile() {
        this.path = null;
        this.numbers = null;
    }

    // Return an object suitable for use as a poison pill.
    static TXTFile getPoisonPill() {
        return new TXTFile();
    }

    Path path() {
        return path;
    }

    private List<String> getLines() {
        if (lines == null) {    // means we have been called before, no need to redo
            try {
                lines = Files.readAllLines(Objects.requireNonNull(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    // Return the list of numbers in the file, possibly reading it in, if not already read.
    ConcurrentLinkedDeque<Integer> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentLinkedDeque<>();
            for (String line : getLines()) {
                Arrays.stream(line.split(",")).
                    map(Integer::parseInt).
                    forEach(numbers::add);
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
