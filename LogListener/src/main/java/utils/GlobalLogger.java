package utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GlobalLogger {

    private static final Logger LOGGER = LogManager.getLogManager().getLogger(GlobalLogger.class.getName());

    public static final GlobalLogger GLOBAL_LOGGER = new GlobalLogger();

    public boolean isDebugging;

    private GlobalLogger() {
        this.isDebugging = false;

        try {
            FileHandler fileHandler = new FileHandler(ConfigManager.CONFIG_MANAGER.getResourcesPath() + File.separator + "logs.log");
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString());
        }
    }

    /**
     * This method logs a message with the specified severity level. If the debugging is enabled (disabled by default)
     * it also logs the message to the terminal.
     *
     * @param level represents the importance or severity of a log message
     * @param msg   a String representation of the log message
     */
    public void log(Level level, String msg) {
        LOGGER.log(level, msg);
        if (isDebugging) {
            System.out.printf("[%s]\t%s\n", level.getName(), msg);
        }
    }

    /**
     * This method logs a message and the exception with the specified severity level. If the debugging is enabled
     * (disabled by default) it also logs the message to the terminal.
     *
     * @param level represents the importance or severity of a log message
     * @param msg   a String representation of the log message
     * @param e an Exception object that represents an error send as parameter for logging
     */
    public void log(Level level, String msg, Exception e) {
        LOGGER.log(level, msg, e);
        if (isDebugging) {
            System.out.printf("[%s]\t%s\t%s\n", level.getName(), msg, e);
        }
    }

    public void setDebugging(boolean debugging) {
        isDebugging = debugging;
    }
}
