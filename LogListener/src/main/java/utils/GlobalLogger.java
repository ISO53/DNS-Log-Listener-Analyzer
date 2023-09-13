package utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalLogger {

    private static final Logger LOGGER = LogManager.getLogger(GlobalLogger.class);

    private static GlobalLogger GLOBAL_LOGGER = null;

    public boolean isDebugging;

    private GlobalLogger() {
        this.isDebugging = false;
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
            System.out.printf("[%s]\t%s\n", level, msg);
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
            System.out.printf("[%s]\t%s\t%s\n", level, msg, e);
        }
    }

    public void setDebugging(boolean debugging) {
        isDebugging = debugging;
    }

    public static GlobalLogger getLoggerInstance() {
        if (GLOBAL_LOGGER == null) {
            GLOBAL_LOGGER = new GlobalLogger();
        }
        return GLOBAL_LOGGER;
    }
}
