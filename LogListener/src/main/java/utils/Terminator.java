package utils;

import org.apache.logging.log4j.Level;

public class Terminator implements Runnable {

    private int maxTimeoutSeconds;
    private boolean isRunning;
    private boolean isSleeping;
    private Thread thread;

    public Terminator() {
        this.maxTimeoutSeconds = 10;
        this.isRunning = false;
        this.isSleeping = true;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        while (isRunning) {

            // Waiting for signal. Signal will be sent when the user try to close the program.
            while (isSleeping) {
                // Wait for 1 second each time to decrease CPU usage
                wait(1);
            }

            // User send signal to close the program, wait for 'MaxTimeoutSeconds' and then force shut the program.
            wait(maxTimeoutSeconds - 5);

            // Write the last 5 seconds
            System.out.println("The program will force shut within 5 seconds.");

            for (int i = 0; i < 5; i++) {
                System.out.println(5 - i);
                wait(1);
            }

            System.out.println("Good bye...");

            System.exit(1);
        }
    }

    public void start() {
        isRunning = true;
        thread.start();
    }

    public void wakeUp() {
        isSleeping = false;
    }

    public void wait(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "Terminator class interrupted while sleeping.", e);
        }
    }

    public void setMaxTimeoutSeconds(int maxTimeoutSeconds) {
        if (maxTimeoutSeconds <= 5) {
            throw new InvalidMaxTimeoutException("Max timeout must be 5 seconds or more.");
        }

        this.maxTimeoutSeconds = maxTimeoutSeconds;
    }

    public class InvalidMaxTimeoutException extends RuntimeException {
        private InvalidMaxTimeoutException(String message) {
            super(message);
        }
    }

}
