package cp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("m1");
        System.out.println("=============================================");
        Path dir = Paths.get("/home/thomas/git/cp2018/exam/data_example");
        List<Result> results = cp.Exam.m1(dir);
        System.out.println(results);
        System.out.println();

        System.out.println("m2");
        System.out.println("=============================================");

        Result result = Exam.m2(dir, 40580387);
        System.out.println(result);
        System.out.println();

        System.out.println("m3");
        System.out.println("=============================================");
        Stats stats = Exam.m3(dir);
        System.out.println(stats);
    }
}
