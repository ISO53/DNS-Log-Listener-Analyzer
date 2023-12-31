package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import utils.GlobalLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.Level;

public class Producer {

    private Channel channel;

    /**
     * Initializes a Producer instance for sending messages to a RabbitMQ queue. It creates a connection to the RabbitMQ
     * server and a channel for message publishing.
     */
    public Producer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, true, false, false, null);
        } catch (IOException | TimeoutException e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to open RabbitMQ channels:", e);
        }
    }

    /**
     * Sends a single message to the RabbitMQ queue.
     * @param message A String representing the message to be sent.
     */
    public void send(String message) {
        try {
            channel.basicPublish("", RabbitMQConfigConstants.QUEUE_NAME, null, message.getBytes());
        } catch (IOException e) {
            GlobalLogger.getLoggerInstance().log(Level.ERROR, "An error occurred trying to send message to RabbitMQ queue:", e);
        }
    }

    /**
     * This method allows you to send multiple messages to RabbitMQ queue in a single operation.
     * @param messages An ArrayList of String objects representing the messages to be sent.
     */
    public void sendChunk(ArrayList<String> messages) {
        messages.forEach(this::send);
    }
}