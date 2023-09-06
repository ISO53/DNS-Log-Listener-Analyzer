package app;

import rabbitmq.Consumer;
import watcher.DirectoryWatcher;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = LogManager.getLogManager().getLogger(App.class.getName());

    private static final int EXIT = 0;
    private static final int CARRY_ON = 1;
    private static final int THREAD_POOL_SIZE = 5;
    private static final LinkedList<DirectoryWatcher> DIRECTORY_WATCHERS = new LinkedList<>();
    private static final LinkedList<Consumer> CONSUMERS = new LinkedList<>();

    /**
     * Main method
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        writeAsciiArt();

        startListeningLogFiles();

        startListeningQueue();

        mainMenu();

    }

    /**
     * Main menu that gives the user control over the program
     */
    public static void mainMenu() {
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

        exit();
    }

    /**
     * Handles the inputs entered by the user
     *
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
     * Reads the directory paths from the config.txt file and starts listening those directories
     */
    public static void startListeningLogFiles() {
        String dir = getResourcesPath() + File.separator + "config.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(dir))) {
            String line;
            while ((line = reader.readLine()) != null) {
                listenDirectory(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to read file:", e);
        }
    }


    /**
     * Creates a thread pool full of workers (Consumer). These workers listen the rabbitmq queue and when there is a
     * data in the queue they take the data, enrich it sent it to the elastic search.
     */
    public static void startListeningQueue() {
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            Consumer consumer = new Consumer();
            CONSUMERS.add(consumer);
            consumer.startReading();
        }
    }

    /**
     * Starts listening a directory and all the log files in it for changes
     *
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
        addDirIfNotExists(directory);
        System.out.printf("Directory '%s' is now being listened.\n", directory);
    }

    /**
     * Adds the new directory to the configuration file if it does not exist
     *
     * @param directory directory to add
     */
    public static void addDirIfNotExists(String directory) {
        boolean isExist = false;
        String filePath = getResourcesPath() + File.separator + "config.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(directory)) {
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(directory);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to read/write file:", e);
        }

    }

    /**
     * Writes some ascii art to look cool
     */
    public static void writeAsciiArt() {
        String filePath = getResourcesPath() + File.separator + "ascii_art.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to read file:", e);
        }
        System.out.println("");
    }

    /**
     * Returns the path of the config file for the program
     *
     * @return path of the config.txt file
     */
    public static String getResourcesPath() {
        String s = File.separator;
        return System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%s", s, s, s, s);
    }

    /**
     * Gracefully exits the application, stopping all directory watchers and consumers, and releasing associated
     * resources.
     */
    public static void exit() {
        for (DirectoryWatcher directoryWatcher : DIRECTORY_WATCHERS) {
            directoryWatcher.getWatchers().forEach((s, watcher) -> watcher.stop());
            directoryWatcher.stop();
        }

        CONSUMERS.forEach(Consumer::close);
    }
}
