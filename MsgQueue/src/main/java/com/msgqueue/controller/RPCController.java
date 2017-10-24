package com.msgqueue.controller;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * rpc工作方式如下：
 * 1：当客户端启动时，它创建一个匿名的独占回调队列。
 * 2：对于rpc请求，客户端发送2个属性，一个是replyTo设置回调队列，另一是correlationId为每个队列设置唯一值
 * 3：请求被发送到一个rpc_queue队列中
 * 4：rpc服务器是等待队列的请求，当收到一个请求的时候，他就把消息返回的结果返回给客户端，使请求结束。
 * 5：客户端等待回调队列上的数据，当消息出现的时候，他检查correlationId，如果它和从请求返回的值匹配，就进行响应。
 * Created by mr on 2017/10/10.
 */
public class RPCController {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 1);
    }

    /**
     * 1：建立连接，通道，队列
     * 2：我们可能运行多个服务器进程，为了分散负载服务器压力，我们设置channel.basicQos(1);
     * 3：我们用basicconsume访问队列。然后进入循环，在其中我们等待请求消息并处理消息然后发送响应。
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.137");
//        factory.setPort(61626);
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

        System.out.println("RPCServer Awating RPC request");
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            BasicProperties props = delivery.getProperties();
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().
                    correlationId(props.getCorrelationId()).build();

            String message = new String(delivery.getBody(), "UTF-8");
            int n = Integer.parseInt(message);

            System.out.println("RPCServer fib(" + message + ")");
            String response = "hello " + message;
            channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }

}
