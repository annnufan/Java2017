package ru.ifmo.ctddev.kopeliovich.walk;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Kopeliovich Anna on 10.02.2017.
 */
public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Run with <input file> <output file> in args");
            return;
        }
        try {
            Path pathInput = Paths.get(args[0]);
            try {
                Path pathOutput = Paths.get(args[1]);
                try (BufferedReader inputFile = Files.newBufferedReader(pathInput)) {
                    try (BufferedWriter output = Files.newBufferedWriter(pathOutput)) {
                        try {
                            String str;
                            while ((str = inputFile.readLine()) != null) {
                                try{
                                    Path path = Paths.get(str);
                                    try {
                                        processPath(path, output);
                                    } catch (IOException e) {
                                        outputHashInformation(ERROR_HASH, path.toString(), output);
                                        System.err.println("Problem with file from input \"" + path + "\": " + e.getMessage());
                                    }
                                } catch (InvalidPathException e) {
                                    outputHashInformation(ERROR_HASH, str, output);
                                    System.err.println("Invalid path for file or dir in input \"" + str + "\": " + e.getMessage());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Problem with read from input file \"" + pathInput + "\": " + e.getMessage());
                        }
                    } catch (IOException e) {
                        System.err.println("Can't write to output file \"" + pathOutput + "\": " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("Problem with input file. Can't open or file is not in UTF-8. \"" + pathInput + "\": " + e.getMessage());
                }
            } catch (InvalidPathException e) {
                System.err.println("Problem with output file. Path not valid -- we can't make this file. \"" + args[1] + "\": " + e.getMessage());
            }
        } catch (InvalidPathException e) {
            System.err.println("Problem with input file. Path not valid. \"" + args[0] + "\": " + e.getMessage());
        }
    }

    private static void processPath(Path path, BufferedWriter output) throws IOException {
        if (Files.isDirectory(path)) {
            for (Path filePath : Files.newDirectoryStream(path)) {
                processPath(filePath, output);
            }
        } else {
            outputHashInformation(fileFNV(path), path.toString(), output);
        }
    }

    private static void outputHashInformation(int hash, String path, BufferedWriter output) throws IOException {
        output.write(String.format("%08x %s", hash, path));
        output.newLine();
    }

    private static int ERROR_HASH = 0;

    private static int fileFNV(Path path) {
        if (Files.notExists(path)) {
            return ERROR_HASH;
        }
        try (InputStream inputStream = Files.newInputStream(path)) {
            try (FNVFilteredStream stream = new FNVFilteredStream(inputStream)) {
                if (stream.read() != -1) {
                    byte[] data = new byte[1024];
                    while (stream.read(data) != -1) {}
                }
                return stream.getHash();

            }
        } catch (IOException e) {
            return ERROR_HASH;
        }
    }
}
