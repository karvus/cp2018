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
        try {
            return Files.lines()
            .parallel()
            .mapToInt(l -> count(l, needle))
            .sum();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }
}
