package cp;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
@SuppressWarnings("WeakerAccess")
public class Exam {
    /**
     * See {@link MinCollector#collect} for the implementation of this
     */
    public static List<Result> m1(Path dir) {
        return MinCollector.collect(dir);
    }

    /**
     * See {@link LineMinFinder#find} for the implementation of this
     */
    public static Result m2(Path dir, int min) {
        return LineMinFinder.find(dir, min);
    }

    /**
     * See {@link StatsComputer#compute} for implementation of this
     */
    public static Stats m3(Path dir) {
        return StatsComputer.compute(dir);
    }
}
