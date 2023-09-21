package utils;

import org.apache.logging.log4j.Level;

/**
 * The `Terminator` class is a utility class that allows for graceful program termination with an optional timeout.
 * It provides a mechanism for waiting until a termination signal is received, and if not received within a specified
 * maximum timeout, it forces the program to shut down.
 * <p>
 * <br>
 * Usage:
 * - Create an instance of the `Terminator` class.<p>
 * - Start the `Terminator` using the `start()` method.<p>
 * - Signal the `Terminator` to wake up and initiate program termination by calling the `wakeUp()` method.<p>
 * - Optionally, set a maximum timeout for program termination using the `setMaxTimeoutSeconds()` method.<p>
 * - If a termination signal is not received within the specified timeout, the program will force shut down.<p>
 * <p>
 * <br>
 * Example:
 * ```
 * Terminator terminator = new Terminator();
 * terminator.setMaxTimeoutSeconds(15); // Set a maximum timeout of 15 seconds
 * terminator.start();
 * <p>
 * // ... (program logic)
 * <p>
 * terminator.wakeUp(); // Send a termination signal
 * ```
 */
public class Terminator implements Runnable {

    private int maxTimeoutSeconds;
    private boolean isSleeping;
    private final Thread thread;

    public Terminator() {
        this.maxTimeoutSeconds = 10;
        this.isSleeping = true;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
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

    public void start() {
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

    public static class InvalidMaxTimeoutException extends RuntimeException {
        private InvalidMaxTimeoutException(String message) {
            super(message);
        }
    }

}
