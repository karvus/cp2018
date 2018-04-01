package cp;

import java.nio.file.Path;

public class ExamResult implements Result {

    private final Path path;
    private final int number;

    public ExamResult(Path path, int result) {
        this.path = path;
        this.number = result;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public int number() {
        return number;
    }

    @Override
    public String toString() {
        return "ExamResult{" +
                "path=" + path() +
                ", number=" + number() +
                '}';
    }
}
