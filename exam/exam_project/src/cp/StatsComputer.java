package cp;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * This implements the m3 part of the project.
 */
public class StatsComputer {

    // This sentinel means that nothing more will be added to a queue.
    private final static NumberFile POISON_PILL = NumberFile.getPoisonPill();

    /**
     * Computes overall statistics about the occurrences of numbers in a directory.
     * <p>
     * This method recursively searches the directory for all numbers in all lines of .txt and .dat files and returns
     * a {@link Stats} object containing the statistics of interest. See the
     * documentation of {@link Stats}.
     */
    static Stats compute(Path dir) {

        // queue of NumberFiles to be consumed (produced in main thread)
        BlockingDeque<NumberFile> NumberFiles = new LinkedBlockingDeque<>();

        // These data-structures are written to by collector-threads during
        // the first phase, and correspond to the Stats items we eventually
        // want to produce.
        ConcurrentMap<Integer, LongAdder> occurrences = new ConcurrentHashMap<>();
        ConcurrentSkipListSet<Total> totals = new ConcurrentSkipListSet<>(
            Comparator.comparing(Total::getSum)
        );

        // Start collectors, so they can begin work as soon as it
        // becomes available.
        ExecutorService collectors = Executors.newCachedThreadPool();
        IntStream.range(0, Runtime.getRuntime().availableProcessors())
            .forEach(i -> collectors.submit(() ->
                collectStats(NumberFiles, occurrences, totals)));

        // Produce *{.txt,.dat}-files, for collectors to consume, finally feeding
        // a poison pill, and waiting for completion of the collectors.
        NumberFile.collectNumberFiles(dir, NumberFile.TXTDAT_MATCHER, NumberFiles);
        NumberFiles.add(POISON_PILL);
        Util.shutdownAndAwait(collectors);

        // Fire up an executor to handle the remaining computation tasks.
        ExecutorService computers = Executors.newCachedThreadPool();

        // Compute the frequency statistics in a different thread.
        Future<FrequencyStats> frequencyStats = computers.submit(() ->
            FrequencyStats.get(occurrences));

        // Compute a sorted list of totals from our SkipListSet of Totals
        // in a different thread.
        Future<List<Path>> futureTotals = computers.submit(() ->
            totals.stream().map(t -> t.file).collect(toList())
        );

        // Create the final Stats object.
        Stats stats;
        try {
            stats = new ExamStats(
                occurrences,
                frequencyStats.get().mostFrequent,
                frequencyStats.get().leastFrequent,
                futureTotals.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        computers.shutdown(); // no need to wait this time, as all results are finalized
        return stats;
    }


    /**
     * Collect statistics about NumberFiles into shared data structures.
     * These are consumers/producers, governed by the "collectors" executor
     * in main thread.
     *
     * @param NumberFiles a BlockingDeque of NumberFiles (input)
     * @param occurrences a map of occurrences of integers in the files (output)
     * @param totals      an ordered set of {@link Total}s
     */
    private static void collectStats(BlockingDeque<NumberFile> NumberFiles,
                                     ConcurrentMap<Integer, LongAdder> occurrences,
                                     ConcurrentSkipListSet<Total> totals) {
        while (!Thread.currentThread().isInterrupted()) {
            NumberFile file;
            try {
                file = NumberFiles.take();
            } catch (InterruptedException e) {
                // We might be cancelled by a shutdown of the collectors executor
                // in the main thread, no sweat.
                break;
            }
            if (file == POISON_PILL) {
                NumberFiles.add(POISON_PILL);
                break;
            }
            file.getNumbers().stream()
                .mapToInt(Integer::intValue)
                .forEach(i ->
                    occurrences.computeIfAbsent(i, k -> new LongAdder()).increment());
            totals.add(new Total(file.path(), file.sum()));
        }
    }

    /**
     * Helper class that contains a {@link Path} and the sum of all numbers in the associated file.
     */
    private static class Total {
        final Path file;
        final int sum;

        Total(Path file, int sum) {
            this.file = file;
            this.sum = sum;
        }

        /**
         * Used as a comparator for the ConcurrentSkipList.
         */
        int getSum() {
            return sum;
        }
    }

    /**
     * Helper class that encapsulates computation of number-frequency-statistics.
     */
    private static class FrequencyStats {

        final int mostFrequent;
        final int leastFrequent;

        FrequencyStats(int mostFrequent, int leastFrequent) {
            this.mostFrequent = mostFrequent;
            this.leastFrequent = leastFrequent;
        }

        /**
         * Take a map of number -> frequency, and return an object with the most- and least frequent number.
         *
         * @param occurrences map of number -> frequency
         * @return an object with the most- and least frequent number
         */
        private static FrequencyStats get(ConcurrentMap<Integer, LongAdder> occurrences) {
            int leastFrequent = -1, lowestFrequency = Integer.MAX_VALUE;
            int mostFrequent = -1, highestFrequency = Integer.MIN_VALUE;

            for (int i : occurrences.keySet()) {
                int frequency = occurrences.get(i).intValue();
                if (frequency > highestFrequency) {
                    mostFrequent = i;
                    highestFrequency = frequency;
                }
                if (frequency < lowestFrequency) {
                    leastFrequent = i;
                    lowestFrequency = frequency;
                }
            }
            return new FrequencyStats(mostFrequent, leastFrequent);
        }
    }
}