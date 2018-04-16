package cp;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
