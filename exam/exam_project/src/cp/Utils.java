package cp;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static final PathMatcher TXT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.txt");
    public static final PathMatcher DAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.dat");
    public static final PathMatcher TXTDAT_MATCHER =
        FileSystems.getDefault().getPathMatcher("glob:**.{txt,dat}");

    static void shutdownAndAwait(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void collectCSIFiles(Path start,
                                BlockingDeque<TXTFile> txtFiles,
                                PathMatcher matcher)
    {
        AtomicInteger count = new AtomicInteger();
        try {
            Files.walk(start)
                .parallel()
                .filter(p -> Files.isRegularFile(p) && matcher.matches(p))
                .forEach(p -> {
                    try {
                        txtFiles.add(new TXTFile(p));
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

    static int computeSum(String line) {
        return Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).sum();
    }
}
