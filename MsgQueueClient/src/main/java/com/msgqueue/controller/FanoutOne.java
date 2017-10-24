package com.msgqueue.controller;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by mr on 2017/10/10.
 */
public class FanoutOne {

    private static final String EXCHANGE_NAME = "fan_out";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //产生一个随机的队列名称
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");//对队列进行绑定

        System.out.println("ReceiveLogs1 Waiting for messages");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("ReceiveLogs1 Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);//队列会自动删除
    }

}
