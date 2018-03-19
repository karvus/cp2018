package cp.week12;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class util {
    private static int count(String haystack, char needle) {
	    int count = 0;
        for (char c : haystack.toCharArray()) {
            if (c == needle)
                count++;
        }
        return count;
    }

    static int count(Path file, char needle) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return lines.stream()
            .parallel()
            .mapToInt(l -> count(l, needle))
            .sum();
    }
}
