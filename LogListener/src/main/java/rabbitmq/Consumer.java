package rabbitmq;

import com.rabbitmq.client.*;
import elastic.ElasticClient;
import utils.GlobalLogger;
import utils.NetworkInfo;
import watcher.LogEntry;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;

public class Consumer {

    private Channel channel;
    private Connection connection;

    /**
     * Initializes a Consumer instance for receiving messages from a RabbitMQ queue and forwarding them to Elasticsearch
     * for processing. It establishes a connection to both RabbitMQ and Elasticsearch.
     */
    public Consumer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, true, false, false, null);
        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to open RabbitMQ channel:", e);
        }
    }

    /**
     * Initiates the message consumption process from the RabbitMQ queue. It sets up a message handler
     * (DeliverCallback) to process incoming messages.
     */
    public void startReading() {
        try {
            DeliverCallback deliverCallback = (s, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                // Enrich the data
                LogEntry logEntry = new LogEntry(message.split(" "));

                NetworkInfo networkInfo = new NetworkInfo(logEntry.getRemoteIp());

                logEntry.setLocalIp(networkInfo.getLocalIp());
                logEntry.setHostAddress(networkInfo.getHostname());
                logEntry.setMacAddress(networkInfo.getMacAddress());

                ElasticClient.elasticClient.send(logEntry);

                // Acknowledge the message after processing
                try {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (AlreadyClosedException e) {
                    GlobalLogger.getLoggerInstance().log(Level.WARN, "An error occurred trying to close a RabbitMQ channel that has  already been closed :", e);
                }
            };

            channel.basicConsume(
                    RabbitMQConfigConstants.QUEUE_NAME,
                    RabbitMQConfigConstants.AUTO_ACKNOWLEDGE_MESSAGES,
                    deliverCallback,
                    consumerTag -> {
                    });

        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to read data from RabbitMQ:", e);
        }
    }

    /**
     * Closes and releases resources associated with the Consumer. It closes the RabbitMQ channel and the
     * elasticClient to ensure proper resource cleanup.
     */
    public void close() {
        try {
            // Close the RabbitMQ channel
            if (channel != null && channel.isOpen()) {
                channel.close();
            }

            // Close the RabbitMQ connection
            if (connection != null && connection.isOpen()) {
                connection.close();
            }

            // Close the ElasticClient
            ElasticClient.elasticClient.close();

        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to close RabbitMQ and ElasticSearch channels:", e);
        }
    }
}
