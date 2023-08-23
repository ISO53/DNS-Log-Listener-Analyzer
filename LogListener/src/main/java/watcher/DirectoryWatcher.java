package watcher;

import java.nio.file.*;
import java.util.HashMap;

public class DirectoryWatcher implements Runnable {

    // String:fileName, watcher.Watcher:watcher (each watcher watches one log file)
    private final HashMap<String, Watcher> watchers = new HashMap<>();
    private final Path dir;
    private final Thread thread;

    private boolean isRunning;

    public DirectoryWatcher(Path dir) {
        this.dir = dir;
        this.isRunning = false;
        this.thread = new Thread(this);
    }

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
                    System.out.println("watcher.DirectoryWatcher has been interrupted. Cleaning up and exiting this thread. " + this.thread.toString());
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
            e.printStackTrace();
        }
    }

    public void start() {
        isRunning = true;
        thread.start();
    }

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
