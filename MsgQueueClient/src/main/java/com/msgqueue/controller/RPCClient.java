package com.msgqueue.controller;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * 1：建立一个连接和通道，并声明了一个唯一的“回调”队列的答复
 * 2：我们订阅回调队列，这样就可以得到RPC的响应
 * 3：定义一个call方法用于发送当前的回调请求
 * 4：生成一个唯一的correlationid，然后通过while循环来捕获合适的回应
 * 5：我们请求信息，发送2个属性，replyTo 和correlationId
 * 6：然后就是等待直到有合适的回应到达
 * 7：while循环是做一个非常简单的工作，对于每一个响应消息，它检查是否有correlationid然后进行匹配。然后是就进行响应。
 * 8：最后把响应返回到客户端。
 * Created by mr on 2017/10/10.
 */
public class RPCClient {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    private QueueingConsumer consumer;

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    public String call(String message) throws IOException, InterruptedException {
        String response;
        String corrID = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties().builder()
                .correlationId(corrID).replyTo(replyQueueName).build();
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrID)) {
                response = new String(delivery.getBody(), "UTF-8");
                break;
            }
        }
        return response;
    }

    public void close() throws Exception {
        connection.close();
    }

    public static void main(String[] args) throws Exception {
        RPCClient rpcClient = null;
        String response;
        try {
            rpcClient = new RPCClient();
            System.out.println("RPCClient  Requesting fib(20)");
            response = rpcClient.call("20");
            System.out.println("RPCClient  Got '" + response + "'");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rpcClient != null) {
                rpcClient.close();
            }
        }
    }

}
