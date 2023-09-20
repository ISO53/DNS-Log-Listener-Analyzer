package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Level;

public class ConfigManager {

    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    private final Lock lock = new ReentrantLock();

    private ConfigManager() {
    }

    /**
     * Adds the new directory to the configuration file if it does not exist
     *
     * @param directory directory to add
     */
    public void addDirIfNotExists(String directory) {
        boolean isExist = false;
        String filePath = getResourcesPath() + File.separator + "config.txt";
        ArrayList<String> lines = new ArrayList<>();

        // Lock the file so only one thread accesses the file at a time
        lock.lock();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(directory)) {
                        isExist = true;
                    }

                    lines.add(line);
                }

                if (isExist) {
                    return;
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read/write file:", e);
            }

            // Directory not exists. Let's add to the ArrayList then write the ArrayList to the file.
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals("<start_log_files>")) {
                    lines.add(i + 1, directory);
                    break;
                }
            }

            // Write ArrayList to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (String line : lines) {
                    writer.write(line);  // Write the line
                    writer.newLine();    // Add a newline character
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read/write file:", e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the path of the config file for the program
     *
     * @return path of the config.txt file
     */
    public String getResourcesPath() {
        String s = File.separator;
        return System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%s", s, s, s, s);
    }

    /**
     * Reads the directory paths from the config.txt file
     *
     * @return list of directories that read from config.txt file
     */
    public LinkedList<String> getListenableDirectories() {
        LinkedList<String> dirs = new LinkedList<>();

        String dir = getResourcesPath() + File.separator + "config.txt";

        // Lock the file so only one thread accesses the file at a time
        lock.lock();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(dir))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#") || line.startsWith("\n")) {
                        continue;
                    }

                    if (line.equals("<start_log_files>")) {
                        dirs.add(reader.readLine());
                    }

                    if (line.equals("<end_log_files>")) {
                        break;
                    }
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read file:", e);
            }
        } finally {
            lock.unlock();
        }

        return dirs;
    }


    /**
     * The program saves every Watcher's status to the config file every time they change. When program restarts, each
     * Watcher's status read from the file and generated accordingly so no data is loss.
     *
     * @return LinkedList of String[] that each represent status of a Watcher.
     */
    public LinkedList<String[]> getWatchersStatus() {
        LinkedList<String[]> watcherStatus = new LinkedList<>();
        String dir = getResourcesPath() + File.separator + "config.txt";

        // Lock the file so only one thread accesses the file at a time
        lock.lock();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(dir))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#") || line.startsWith("\n")) {
                        continue;
                    }

                    if (line.equals("<start_thread_status>")) {
                        line = reader.readLine();
                        if (!line.equals("<end_thread_status>")) {

                            String firstPart = line.substring(0, line.lastIndexOf(' '));
                            String secondPart = line.substring(line.lastIndexOf(' ') + 1);

                            watcherStatus.add(new String[]{firstPart, secondPart});
                        }
                    }

                    if (line.equals("<end_thread_status>")) {
                        break;
                    }
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read file:", e);
            }
        } finally {
            lock.unlock();
        }

        return watcherStatus;
    }

    /**
     * Save a Watcher's status (last read line) to the config file.
     *
     * @param path File path that belongs to a Watcher.
     */
    public void updateWatcherStatus(String path, long status) {
        boolean isExist = false;
        String filePath = getResourcesPath() + File.separator + "config.txt";
        ArrayList<String> lines = new ArrayList<>();

        // Lock the file so only one thread accesses the file at a time
        lock.lock();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean isPathsStarted = false;

                while ((line = reader.readLine()) != null) {
                    lines.add(line);

                    if (line.equals("<start_thread_status>")) {
                        isPathsStarted = true;
                        continue;
                    }

                    if (line.equals("<end_thread_status>")) {
                        isPathsStarted = false;
                        continue;
                    }

                    if (isPathsStarted) {
                        String firstPart = line.substring(0, line.lastIndexOf(' '));
                        if (firstPart.equals(path)) {
                            isExist = true;
                        }
                    }
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read/write file:", e);
            }

            // Add the status to ArrayList if not exist and update its status.
            if (isExist) {
                boolean isPathsStarted = false;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).equals("<start_thread_status>")) {
                        isPathsStarted = true;
                        continue;
                    }

                    if (lines.get(i).equals("<end_thread_status>")) {
                        isPathsStarted = false;
                        continue;
                    }

                    if (isPathsStarted) {
                        if (lines.get(i).startsWith(path)) {
                            String firstPart = lines.get(i).substring(0, lines.get(i).lastIndexOf(' '));
                            lines.set(i, firstPart + " " + status);
                            break;
                        }
                    }
                }
            } else {
                int size = lines.size();
                for (int i = 0; i < size; i++) {
                    if (lines.get(i).equals("<start_thread_status>")) {
                        lines.add(i + 1, path + " " + status);
                    }
                }
            }

            // Write ArrayList to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (String line : lines) {
                    writer.write(line);  // Write the line
                    writer.newLine();    // Add a newline character
                }
            } catch (IOException e) {
                GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read/write file:", e);
            }

        } finally {
            lock.unlock();
        }
    }

}
