package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import utils.NetworkInfo;
import watcher.LogEntry;

import java.nio.charset.StandardCharsets;

public class Consumer {

    private Channel chanel;

    public Consumer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);

        try {
            Connection connection = factory.newConnection();
            chanel = connection.createChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReading() {
        try {
            chanel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, true, false, false, null);

            DeliverCallback deliverCallback = (s, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                // Enrich the data

                LogEntry logEntry = new LogEntry(message.split(" "));

                NetworkInfo networkInfo = new NetworkInfo(logEntry.getRemoteIp());

                System.out.println(networkInfo.getHostname());
                System.out.println(networkInfo.getLocalIp());
                System.out.println(networkInfo.getMacAddress());
            };

            chanel.basicConsume(RabbitMQConfigConstants.QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
