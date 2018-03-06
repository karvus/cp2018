package cp.week10;

import com.sun.xml.internal.txw2.TxwException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.*;

public class Exercise11 {
	/*
	- Write a method that finds all files with a ".txt" suffix in a given directory.
	- The method must visit the directory recursively, meaning
	  that all .txt files in sub-directories must also be found.
	*/

    public static class TXTFileCollector extends SimpleFileVisitor<Path> {

        List<Path> files;
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            if (file.toString().endsWith(".txt")) {
                files.add(file);
            }
            return CONTINUE;
        }

        public List<Path> collect(Path dir) {
            files = new ArrayList<>();
            try {
                Files.walkFileTree(dir, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return files;
        }
    }

    public static void main(String[] args) {
        Path startingDir =  Paths.get("/home/thomas/git/cp2018/exercises/src/cp/week10/txt-files/");
        new TXTFileCollector().collect(startingDir).stream().forEach(System.out::println);
    }
}


