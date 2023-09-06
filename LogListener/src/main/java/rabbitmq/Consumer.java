package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import elastic.ElasticClient;
import utils.NetworkInfo;
import watcher.LogEntry;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Consumer {

    private static final Logger LOGGER = LogManager.getLogManager().getLogger(Consumer.class.getName());

    private Channel channel;
    private final ElasticClient elasticClient;

    /**
     * Initializes a Consumer instance for receiving messages from a RabbitMQ queue and forwarding them to Elasticsearch
     * for processing. It establishes a connection to both RabbitMQ and Elasticsearch.
     */
    public Consumer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);
        elasticClient = new ElasticClient();

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, true, false, false, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to open RabbitMQ channel:", e);
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

                elasticClient.send(logEntry);
            };

            channel.basicConsume(RabbitMQConfigConstants.QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to read data from RabbitMQ:", e);
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

            // Close the ElasticClient
            if (elasticClient != null) {
                elasticClient.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to close RabbitMQ and ElasticSearch channels:", e);
        }
    }

}
