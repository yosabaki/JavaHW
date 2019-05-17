package ru.ifmo.rain.ustinov.walk;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

public class RecursiveWalk {


    private static void recursiveWalk(Path inputFile, Path outputFile) {
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            if (outputFile.getParent() != null) {
                try {
                    Files.createDirectories(outputFile.getParent());
                } catch (SecurityException e) {
                    System.err.println("Permission denied to output file: " + e.getMessage());
                    return;
                } catch (IOException e) {
                    System.err.println("Can't create output file: " + e.getMessage());
                    return;
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                String path = "";
                int lineNumber = 0;
                while (path != null) {
                    try {
                        path = reader.readLine();
                        lineNumber++;
                    } catch (IOException e) {
                        System.err.println("Error appeared while writing into output file: " + e.getMessage());
                        break;
                    }
                    if (path == null) continue;
                    Stream<Path> s;
                    try {
                        s = Files.walk(Paths.get(path));
                    } catch (InvalidPathException | IOException e) {
                        System.err.println("Invalid path to file: " + e.getMessage() + " at line[" + lineNumber + ']');
                        writeHash(path, writer);
                        continue;
                    }
                    s.filter(p -> Files.isRegularFile(p)).forEach(p -> writeHash(p.toString(), writer));
                    s.close();
                }
            } catch (IOException e) {
                System.err.println("Can't write into output file: " + e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Permission denied to input file: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Can't find input file: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Can't read from input file: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Permission denied to input file" + e.getMessage());
        }
    }


    private static void writeHash(String path, BufferedWriter writer) {
        try {
            writer.write(RecursiveWalk.getHash(path) + ' ' + path);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Can't write into output file: " + e.getMessage());
        }
    }

    private static int FNV1Hash(byte[] buf, int length, int fnv0) {
        int hval = fnv0;
        for (int i = 0; i < length; i++) {
            hval = (hval * 0x01000193) ^ (buf[i] & 0xff);
        }
        return hval;
    }

    private static String getHash(String path) {
        int hash = 0x811c9dc5; // first value of FNV1 hash function
        byte[] bytes = new byte[1 << 10]; // 1024 bytes
        try (InputStream inputStream = new FileInputStream(path)) {
            int length;
            while ((length = inputStream.read(bytes)) >= 0) {
                hash = FNV1Hash(bytes, length, hash);
            }
        } catch (IOException e) {
            hash = 0;
        }
        return String.format("%08x", hash);
    }


    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: RecursiveWalk inputfile outputfile");
        } else {
            Path inputFile, outputFile;
            try {
                inputFile = Paths.get(args[0]);
                outputFile = Paths.get(args[1]);
            } catch (InvalidPathException e) {
                System.err.println("Invalid " + (e.getInput().equals(args[0]) ? "input" : "output") + " file: " + e.getMessage());
                return;
            }
            recursiveWalk(inputFile, outputFile);
        }
    }
}
