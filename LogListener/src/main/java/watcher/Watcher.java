package watcher;

import rabbitmq.Producer;
import rabbitmq.RabbitMQConfigConstants;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;

public class Watcher implements Runnable {

    private final long SLEEP_TIME_MILLIS = 100;
    private final Thread thread;
    private final String path;
    private final Producer producer;

    private long lastReadLine = -1;
    private boolean isRunning;
    private boolean isSleeping;
    private boolean isExit;

    public Watcher(String path) {
        this.path = path;
        this.thread = new Thread(this);
        this.isRunning = false;
        this.isSleeping = false;
        this.isExit = false;
        this.producer = new Producer();
    }

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

            // Read the file chunk at a time and store it in the static QUEUE variable
            readAndStore();
        }

        System.out.println("Watcher has been interrupted. Cleaning up and exiting this thread. " + this.thread.toString());
    }

    public void start() {
        isRunning = true;
        thread.start();
    }

    public void stop() {
        isSleeping = false;
        isExit = true;
    }

    public void wakeUp() {
        isSleeping = false;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
                if (linesRead != lastReadLine) {
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

            lock.release();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
