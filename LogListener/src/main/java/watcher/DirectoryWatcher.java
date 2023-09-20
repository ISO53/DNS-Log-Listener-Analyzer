package watcher;

import utils.GlobalLogger;

import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.Level;

public class DirectoryWatcher implements Runnable {

    public static final LinkedList<DirectoryWatcher> DIRECTORY_WATCHERS = new LinkedList<>();

    // String:fileName, Watcher:watcher (each watcher watches one log file)
    public static final HashMap<String, Watcher> WATCHERS = new HashMap<>();
    private final Path dir;
    private final Thread thread;

    private boolean isRunning;

    /**
     * Initializes a DirectoryWatcher instance for monitoring a specified directory. It sets the directory path and
     * initializes the internal data structures. The watcher is initially not running.
     *
     * @param dir The Path object representing the directory to be monitored.
     */
    public DirectoryWatcher(Path dir) {
        this.dir = dir;
        this.isRunning = false;
        this.thread = new Thread(this);
    }

    /**
     * The run method is executed when the DirectoryWatcher is started as a separate thread. It continuously monitors
     * the specified directory for changes in log files and creates or wakes up Watcher instances for individual log
     * files.
     */
    @Override
    public void run() {

        try {
            // Create a watch service to monitor changes on a directory
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Assign that watch service to a directory
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            GlobalLogger.getLoggerInstance().log(Level.INFO, "Started watching log files on " + dir);

            while (isRunning) {
                WatchKey key;

                try {
                    key = watchService.take(); // Blocking
                } catch (InterruptedException e) {
                    GlobalLogger.getLoggerInstance().log(Level.INFO, "DirectoryWatcher has been interrupted. Cleaning up and exiting this thread. " + dir);
                    watchService.close();
                    break;
                }

                if (key == null) {
                    continue;
                }

                // Handle events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // This file has been changed
                    Path changedFile = (Path) event.context();

                    String absolutePath = ((Path) key.watchable()).resolve(changedFile).toAbsolutePath().toString();

                    if (WATCHERS.containsKey(absolutePath)) {
                        // There is a watcher for this log file, wake him up
                        WATCHERS.get(absolutePath).wakeUp();
                        continue;
                    }

                    // There is no watcher for this file, create one
                    Watcher watcher = new Watcher(absolutePath);
                    WATCHERS.put(absolutePath, watcher);
                    watcher.start();
                    watcher.wakeUp();
                }

                key.reset();
            }
        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.INFO, "An error occurred trying to monitor a directory:", e);
        }
    }

    /**
     * Starts the DirectoryWatcher. It sets the watcher to a running state and initiates the thread to begin monitoring
     * the directory for log file changes.
     */
    public void start() {
        isRunning = true;
        thread.start();
    }

    /**
     * Stops the DirectoryWatcher. It sets the watcher to a non-running state and interrupts the thread, allowing it to
     * gracefully exit. Any ongoing monitoring is terminated.
     */
    public void stop() {
        isRunning = false;
        thread.interrupt();
    }

    public Path getDir() {
        return dir;
    }

    public Thread getThread() {
        return thread;
    }
}
