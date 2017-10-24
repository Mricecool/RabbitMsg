package com.msgqueue.controller;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by mr on 2017/10/10.
 */
public class WorkOne {

    private static final String TASK_QUEUE_NAME = "task_queue";

    /**
     * 注：channel.basicQos(1);保证一次只分发一个 。
     * autoAck是否自动回复，如果为true的话，每次生产者只要发送信息就会从内存中删除，
     * 那么如果消费者程序异常退出，那么就无法获取数据，我们当然是不希望出现这样的情况，
     * 所以才去手动回复，每当消费者收到并处理信息然后在通知生成者。最后从队列中删除这条信息。
     * 如果消费者异常退出，如果还有其他消费者，那么就会把队列中的消息发送给其他消费者，
     * 如果没有，等消费者启动时候再次发送。
     * @param args
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println("Worker1  Waiting for messages");

        //每次从队列获取的数量
        channel.basicQos(1);

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Worker1  Received '" + message + "'");
                try {
                    //throw  new Exception();
                    doWork(message);
                }catch (Exception e){
                    channel.abort();
                }finally {
                    System.out.println("Worker1 Done");
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        boolean autoAck=false;
        //消息消费完成确认
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
    }
    private static void doWork(String task) {
        try {
            Thread.sleep(1000); // 暂停1秒钟
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
