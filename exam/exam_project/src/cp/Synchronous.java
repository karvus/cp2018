package cp;

// Synchronous solutions, for benchmark reference

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Synchronous {

    static List<Result> m1(Path dir) {
        List<Result> list;
        try {
            list =
                Files.walk(dir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> new ExamResult(p, min(p)))
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't read from file");
        }
        return list;
    }

    private static int min(Path path) {

        String[] numbers;
        try {
            numbers = Files.lines(path)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error: empty file"))
                .split(",");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't read from " + path);
        }
        return
            Arrays.stream(numbers)
                .mapToInt(Integer::parseInt)
                .min()
                .orElseThrow(() ->
                    new RuntimeException("Error: unable to parse comma-separated number line."));
    }

    public static void main(String[] args) {
        m1(Paths.get("/home/thomas/git/cp2018/exam/data_example/"))
            .forEach(System.out::println);

    }

}
