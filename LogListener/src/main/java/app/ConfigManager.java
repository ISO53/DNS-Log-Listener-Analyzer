package app;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ConfigManager {

    private static final Logger LOGGER = LogManager.getLogManager().getLogger(ConfigManager.class.getName());

    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    private void ConfigManager() {
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
            LOGGER.log(Level.SEVERE, "An error occurred trying to read/write file:", e);
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
            LOGGER.log(Level.SEVERE, "An error occurred trying to read/write file:", e);
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
            LOGGER.log(Level.SEVERE, "An error occurred trying to read file:", e);
        }

        return dirs;
    }
}
