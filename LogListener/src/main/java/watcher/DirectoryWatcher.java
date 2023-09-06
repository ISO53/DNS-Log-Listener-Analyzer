package watcher;

import java.nio.file.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DirectoryWatcher implements Runnable {

    private static final Logger LOGGER = LogManager.getLogManager().getLogger(DirectoryWatcher.class.getName());

    // String:fileName, Watcher:watcher (each watcher watches one log file)
    private final HashMap<String, Watcher> watchers = new HashMap<>();
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

            System.out.println("Started watching log files on " + dir);

            while (isRunning) {
                WatchKey key;

                try {
                    key = watchService.take(); // Blocking
                } catch (InterruptedException e) {
                    System.out.println("DirectoryWatcher has been interrupted. Cleaning up and exiting this thread. " + this.thread.toString());
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


                    if (watchers.containsKey(absolutePath)) {
                        // There is a watcher for this log file, wake him up
                        watchers.get(absolutePath).wakeUp();
                        continue;
                    }

                    // There is no watcher for this file, create one
                    Watcher watcher = new Watcher(absolutePath);
                    watchers.put(absolutePath, watcher);
                    watcher.start();
                    watcher.wakeUp();
                }

                key.reset();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to monitor a directory:", e);
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

    public HashMap<String, Watcher> getWatchers() {
        return watchers;
    }
}
