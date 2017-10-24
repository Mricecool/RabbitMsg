package com.msgqueue.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 分发
 * Created by mr on 2017/10/10.
 */
@Controller
public class FanoutController {
    //交换机名称
    private static final String EXCHANGE_NAME = "fan_out";

    @RequestMapping("/fanout")
    public String fanout(){
        try {
            createFanout();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "index";
    }

    private void createFanout() throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection=factory.newConnection();
        Channel channel=connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");//fanout表示分发，所有的消费者得到同样的队列信息
        //分发信息
        for (int i=0;i<5;i++){
            String message="Hello World"+i;
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes());
            System.out.println("EmitLog Sent '" + message + "'");
        }
        channel.close();
        connection.close();
    }
}
