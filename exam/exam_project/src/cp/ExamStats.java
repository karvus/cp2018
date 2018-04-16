
package cp;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;


/**
 * Implementation of the Stats interface, for m3
 */
public class ExamStats implements Stats {
    private final Map<Integer, LongAdder> occurrences;
    private final int mostFrequent;
    private final int leastFrequent;
    private final List<Path> byTotals;

    ExamStats(Map<Integer, LongAdder> occurrences, int mostFrequent,
                     int leastFrequent, List<Path> byTotals) {

        this.occurrences = occurrences;
        this.mostFrequent = mostFrequent;
        this.leastFrequent = leastFrequent;
        this.byTotals = byTotals;
    }

    @Override
    public int occurrences(int number) {
        if (occurrences.containsKey(number))
            return occurrences.get(number).intValue();
        else
            return 0;
    }

    @Override
    public int mostFrequent() {
        return mostFrequent;
    }

    @Override
    public int leastFrequent() {
        return leastFrequent;
    }

    @Override
    public List<Path> byTotals() {
        return byTotals;
    }

    @Override
    public String toString() {
        return "ExamStats{" +
                "mostFrequent=" + mostFrequent() +
                ", leastFrequent=" + leastFrequent() +
                ", byTotals=" + byTotals() +
                ", occurrences=" + occurrences +
                '}';
    }
}


