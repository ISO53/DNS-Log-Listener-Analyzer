package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import utils.NetworkInfo;
import watcher.LogEntry;

import java.nio.charset.StandardCharsets;

public class Consumer {

    private Channel channel;

    public Consumer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, true, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReading() {
        try {
            DeliverCallback deliverCallback = (s, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                // Enrich the data
                LogEntry logEntry = new LogEntry(message.split(" "));

                NetworkInfo networkInfo = new NetworkInfo(logEntry.getRemoteIp());

                System.out.println("*******************" + logEntry.getQuestionName() + "*******************");
                System.out.print("\tHostname: " + networkInfo.getHostname());
                System.out.print("\tLocal Ip: " + networkInfo.getLocalIp());
                System.out.print("\tMac Address: " + networkInfo.getMacAddress() + "\n");
            };

            channel.basicConsume(RabbitMQConfigConstants.QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
