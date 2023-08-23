package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Producer {

    private Channel channel;

    public Producer() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfigConstants.HOST_NAME);

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfigConstants.QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        try {
            channel.basicPublish("", RabbitMQConfigConstants.QUEUE_NAME, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendChunk(ArrayList<String> messages) {
        try {
            for (String message : messages) {
                channel.basicPublish("", RabbitMQConfigConstants.QUEUE_NAME, null, message.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}