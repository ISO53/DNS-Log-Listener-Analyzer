package app;

import rabbitmq.Consumer;
import utils.ConfigManager;
import utils.GlobalLogger;
import utils.Terminator;
import watcher.DirectoryWatcher;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import watcher.Watcher;

public class App {

    private static final int EXIT = 0;
    private static final int CARRY_ON = 1;
    private static final int THREAD_POOL_SIZE = 50;
    private static final LinkedList<Consumer> CONSUMERS = new LinkedList<>();
    private static final Terminator terminator = new Terminator();

    /**
     * Main method
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        init();

        writeAsciiArt();

        startListeningLogFiles();

        startPreviouslyRunningWatchers();

        startListeningQueue();

        mainMenu();

    }

    /**
     * Initializes objects that can be used in the main function.
     */
    private static void init() {
        terminator.start();
    }

    /**
     * Main menu that gives the user control over the program
     */
    private static void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        int status;

        do {
            System.out.println("0. Listen new directory");
            System.out.println("1. Show listened directories");
            System.out.println("2. Show listened log files");
            System.out.println("3. Toggle debugging");
            System.out.println("4. Set the maximum wait time to be used when the program closes");
            System.out.println("99. Shut Down The Program And Exit");
            System.out.print("-> ");
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NoSuchElementException e) {
                choice = 100;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number next time.");
                choice = 100;
            }
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
    private static int choiceHandler(int choice) {

        switch (choice) {
            case 0 -> {
                Scanner scanner = new Scanner(System.in);
                String dirStr = scanner.nextLine();
                listenDirectory(dirStr);
                scanner.close();
            }
            case 1 ->
                    DirectoryWatcher.DIRECTORY_WATCHERS.forEach(directoryWatcher -> System.out.println(directoryWatcher.getDir().getFileName()));
            case 2 ->
                    DirectoryWatcher.WATCHERS.values().forEach(watcher -> System.out.println(watcher.getPath()));
            case 3 -> {
                Scanner scanner = new Scanner(System.in);
                String debuggingChoice;

                if (GlobalLogger.getLoggerInstance().isDebugging()) {
                    System.out.println("This action will disable debugging. Log messages will no longer printed to console. Do you wish to continue? (Y/N)\n-> ");
                    debuggingChoice = scanner.nextLine();
                    if (debuggingChoice.equalsIgnoreCase("Y")) {
                        GlobalLogger.getLoggerInstance().setDebugging(false);
                    }
                } else {
                    System.out.println("This action will enable debugging. Log messages will be printed to console. Do you wish to continue? (Y/N)\n-> ");
                    debuggingChoice = scanner.nextLine();
                    if (debuggingChoice.equalsIgnoreCase("Y")) {
                        GlobalLogger.getLoggerInstance().setDebugging(true);
                    }
                }

                scanner.close();
            }
            case 4 -> {
                Scanner scanner = new Scanner(System.in);
                int maxTimeout = 0;

                System.out.print("Select the amount of time that must be waited before the program is forcibly closed. Must be greater than 5 seconds!\n-> ");
                try {
                    maxTimeout = Integer.parseInt(scanner.nextLine());
                    scanner.close();
                } catch (NoSuchElementException | NumberFormatException e) {
                    System.out.println("Please enter a valid number next time.");
                    scanner.close();
                    break;
                } catch (Terminator.InvalidMaxTimeoutException e) {
                    System.out.println("You must enter a number greater than 5!");
                    scanner.close();
                    break;
                }

                terminator.setMaxTimeoutSeconds(maxTimeout);
                System.out.println("The time to wait before closing the program is set to " + maxTimeout + " seconds.");
            }
            case 99 -> {
                return EXIT;
            }
            default -> System.out.println("You have entered an incorrect input! Try again.");
        }

        return CARRY_ON;
    }

    /**
     * Creates a thread pool full of workers (Consumer). These workers listen the rabbitmq queue and when there is a
     * data in the queue they take the data, enrich it sent it to the elastic search.
     */
    private static void startListeningQueue() {
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
    private static void listenDirectory(String directory) {
        Path dir = Paths.get(directory);

        if (!Files.exists(dir) || !Files.isDirectory(dir) || dir.toString().isEmpty() || dir.toString().equals(".") || dir.toString().equals("..")) {
            GlobalLogger.getLoggerInstance().log(Level.INFO, "Directory is not valid! " + directory);
            return;
        }

        DirectoryWatcher directoryWatcher = new DirectoryWatcher(dir);
        directoryWatcher.start();
        DirectoryWatcher.DIRECTORY_WATCHERS.add(directoryWatcher);
        ConfigManager.CONFIG_MANAGER.addDirIfNotExists(directory);
        GlobalLogger.getLoggerInstance().log(Level.INFO, "Directory '%s' is now being listened. " + directory);
    }

    /**
     * Gets the directories from ConfigManager and starts listening those directories
     */
    private static void startListeningLogFiles() {
        for (String directory : ConfigManager.CONFIG_MANAGER.getListenableDirectories()) {
            listenDirectory(directory);
        }
    }

    /**
     * If there are Watchers running when the program closes, the program saves the status of these Watchers in the
     * config file. The next time the program runs, it first checks the config file and starts the Watchers from where
     * they left off, if there are any.
     */
    private static void startPreviouslyRunningWatchers() {
        for (String[] watchersStatus : ConfigManager.CONFIG_MANAGER.getWatchersStatus()) {
            Watcher watcher = new Watcher(watchersStatus[0], Long.parseLong(watchersStatus[1]));
            DirectoryWatcher.WATCHERS.put(watcher.getPath(), watcher);
        }
    }

    /**
     * Writes some ascii art to look cool
     */
    private static void writeAsciiArt() {
        String filePath = ConfigManager.CONFIG_MANAGER.getResourcesPath() + File.separator + "ascii_art.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read file:", e);
        }

        System.out.println();
    }

    /**
     * Gracefully exits the application, stopping all directory watchers and consumers, and releasing associated
     * resources.
     */
    private static void exit() {

        // Wake the terminator up in case of timeout when program tries to close.
        terminator.wakeUp();

        // Stop each watcher
        DirectoryWatcher.WATCHERS.values().forEach(Watcher::stop);

        // Then stop each directory watcher
        DirectoryWatcher.DIRECTORY_WATCHERS.forEach(DirectoryWatcher::stop);

        // Then stop each consumer (They listen RabbitMQ queue and write to ElasticSearch)
        CONSUMERS.forEach(Consumer::close);
    }
}
