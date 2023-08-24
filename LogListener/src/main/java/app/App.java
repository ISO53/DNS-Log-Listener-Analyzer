package app;

import watcher.DirectoryWatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Scanner;

public class App {

    private static final int EXIT = 0;
    private static final int CARRY_ON = 1;
    private static final LinkedList<DirectoryWatcher> DIRECTORY_WATCHERS = new LinkedList<>();

    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {

        writeAsciiArt();

        startListeningLogFiles();

        Scanner scanner = new Scanner(System.in);

        int choice;
        int status;
        do {
            System.out.println("0. Listen new directory");
            System.out.println("1. Show listened directories");
            System.out.println("2. Show listened log files");
            System.out.println("99. Shut Down The Program And Exit");
            System.out.print("-> ");
            choice = scanner.nextInt();
            status = choiceHandler(choice);
        } while (status != EXIT);

        System.out.println("Exiting from program...");

        for (DirectoryWatcher directoryWatcher : DIRECTORY_WATCHERS) {
            directoryWatcher.getWatchers().forEach((s, watcher) -> watcher.stop());
            directoryWatcher.stop();
        }

        System.out.println("Good bye.");
    }

    /**
     * Handles the inputs entered by the user
     * @param choice a number entered by the user to choose between menu options
     * @return status
     */
    public static int choiceHandler(int choice) {

        switch (choice) {
            case 0 -> {
                Scanner scanner = new Scanner(System.in);
                String dirStr = scanner.nextLine();
                listenDirectory(dirStr);
            }
            case 1 -> DIRECTORY_WATCHERS.forEach(directoryWatcher -> System.out.println(directoryWatcher.getDir().getFileName()));
            case 2 -> DIRECTORY_WATCHERS.forEach(directoryWatcher -> directoryWatcher.getWatchers().forEach((s, watcher) -> System.out.println(s)));
            case 99 -> {
                return EXIT;
            }
        }

        return CARRY_ON;
    }

    /**
     * Reads the directory paths from the dirst.txt file and starts listening those directories
     */
    public static void startListeningLogFiles() {
        String s = File.separator;
        String dir = System.getProperty("user.dir") + s + "src" + s + "main" + s + "resources" + s + "dirs.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(dir))) {
            String line;
            while ((line = reader.readLine()) != null) {
                listenDirectory(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts listening a directory and all the log files in it for changes
     * @param directory a file directory to listen
     */
    public static void listenDirectory(String directory) {
        Path dir = Paths.get(directory);

        if (!Files.exists(dir) || !Files.isDirectory(dir) || dir.toString().equals("") || dir.toString().equals(".") || dir.toString().equals("..")) {
            System.out.println("Directory you entered is not valid!");
            return;
        }

        DirectoryWatcher directoryWatcher = new DirectoryWatcher(dir);
        directoryWatcher.start();
        DIRECTORY_WATCHERS.add(directoryWatcher);
        System.out.printf("Directory '%s' is now being listened.\n", directory);
    }

    /**
     * Writes some ascii art to look cool
     * */
    public static void writeAsciiArt() {
        String s = File.separator;
        String filePath = System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%sascii_art.txt", s, s, s, s);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
