package watcher;

import rabbitmq.Producer;
import rabbitmq.RabbitMQConfigConstants;
import utils.GlobalLogger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import org.apache.logging.log4j.Level;

public class Watcher implements Runnable {

    private final long SLEEP_TIME_MILLIS = 100;
    private final Thread thread;
    private final String path;
    private final Producer producer;

    private long lastReadLine = -1;
    private boolean isRunning;
    private boolean isSleeping;
    private boolean isExit;

    /**
     * Initializes a Watcher instance for monitoring changes in a specified log file. It sets the file path, creates a
     * thread for watching, and initializes other internal variables and a Producer for sending log data to RabbitMQ.
     *
     * @param path A String representing the path to the log file to be monitored.
     */
    public Watcher(String path) {
        this.path = path;
        this.thread = new Thread(this);
        this.isRunning = false;
        this.isSleeping = false;
        this.isExit = false;
        this.producer = new Producer();
    }

    /**
     * The run method is executed when the Watcher is started as a separate thread. It continuously monitors the
     * specified log file for changes, reads and stores log entries, and sends them to RabbitMQ via the associated
     * Producer. The thread can be controlled using flags like isRunning, isSleeping, and isExit.
     */
    @Override
    public void run() {

        while (isRunning) {

            while (isSleeping) {
                // There is no change on the file, sleep...
                sleep();
            }

            // Check if the watcher thread should be terminated or not
            if (isExit) {
                break;
            }

            // File has changed, make the "isSleeping" true again in case of another wakeUp call
            isSleeping = true;

            // Read the file chunk at a time and store it in RabbitMQ Queue
            readAndStore();
        }

        GlobalLogger.getLoggerInstance().log(Level.INFO, "Watcher has been interrupted. Cleaning up and exiting this thread. " + this.path);
    }

    /**
     * Starts the Watcher thread for monitoring the log file. It sets the isRunning flag to true and initiates the
     * thread to begin monitoring and processing log entries.
     */
    public void start() {
        isRunning = true;
        thread.start();
    }

    /**
     * Stops the Watcher. It sets the isSleeping flag to false and the isExit flag to true, indicating that the thread
     * should terminate gracefully.
     */
    public void stop() {
        isSleeping = false;
        isExit = true;
    }

    /**
     * Wakes up the Watcher from a sleeping state, allowing it to continue monitoring the log file for changes. It sets
     * the isSleeping flag to false.
     */
    public void wakeUp() {
        isSleeping = false;
    }

    /**
     * Puts the Watcher thread to sleep for a defined duration (SLEEP_TIME_MILLIS) when there are no changes in the log
     * file. It is used to reduce CPU usage during idle periods.
     */
    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException e) {
            GlobalLogger.getLoggerInstance().log(Level.INFO, "An error occurred trying to sleep the thread:", e);
        }
    }

    /**
     * Reads log entries from the monitored log file, stores them in a collection, and sends them to RabbitMQ in chunks.
     * It uses file locking to ensure exclusive access to the log file while reading. This method manages the reading,
     * processing, and sending of log entries.
     */
    private void readAndStore() {
        FileLock lock = null;

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");

            FileChannel fileChannel = randomAccessFile.getChannel();

            lock = fileChannel.lock(0, Long.MAX_VALUE, true);

            // Read from the file while its locked
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            int linesRead = 0;
            ArrayList<String> logEntries = new ArrayList<>(RabbitMQConfigConstants.CHUNK_SIZE);

            while ((line = reader.readLine()) != null) {

                // Go to the last read line
                if (lastReadLine != -1 && linesRead != lastReadLine) {
                    linesRead++;
                    continue;
                }

                linesRead++;
                lastReadLine++;
                logEntries.add(line);

                if (linesRead >= RabbitMQConfigConstants.CHUNK_SIZE) {
                    // We read a chunk of string, time to use it
                    linesRead = 0;

                    producer.sendChunk(logEntries);

                    logEntries.clear();
                }
            }

            reader.close();

            if (!logEntries.isEmpty()) {
                producer.sendChunk(logEntries);
                logEntries.clear();
            }

            lock.release();
        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.ERROR, "An error occurred trying to read file:", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    GlobalLogger.getLoggerInstance().log(Level.ERROR, "An error occurred trying to release file lock:", e);
                }
            }
        }
    }

    private void updateStatusOnConfigFile() {

    }
}
