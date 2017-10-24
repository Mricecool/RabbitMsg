package com.msgqueue.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 路由关键字匹配
 * Created by mr on 2017/10/10.
 */
@Controller
public class RouteController {

    private static final String EXCHANGE_NAME = "direct_logs";
    // 路由关键字
    private static final String[] routingKeys = new String[]{"info", "warning", "error"};

    @RequestMapping("/route")
    public String route() {
        try {
            createRoute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "index";
    }

    private void createRoute() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");//注意是direct
        //发送信息
        for (String routingKey : routingKeys) {
            String message = "RoutingSendDirect Send the message level:" + routingKey;
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
            System.out.println("RoutingSendDirect Send" + routingKey + "':'" + message);
        }
        channel.close();
        connection.close();
    }
}
