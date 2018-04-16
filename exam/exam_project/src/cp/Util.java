package cp;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Util {

    static void shutdownAndAwait(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static int computeSum(String line) {
        return Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).sum();
    }
}
