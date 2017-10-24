package com.msgqueue.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 启动监控管理器：rabbitmq-plugins enable rabbitmq_management
 * 关闭监控管理器：rabbitmq-plugins disable rabbitmq_management
 * 启动rabbitmq：rabbitmq-service start
 * 关闭rabbitmq：rabbitmq-service stop
 * 查看所有的队列：rabbitmqctl list_queues
 * 清除所有的队列：rabbitmqctl reset
 * 关闭应用：rabbitmqctl stop_app
 * 启动应用：rabbitmqctl start_app
 *
 * 用户和权限设置
 * 添加用户：rabbitmqctl add_user username password
 * 分配角色：rabbitmqctl set_user_tags username administrator
 * 新增虚拟主机：rabbitmqctl add_vhost  vhost_name
 * 将新虚拟主机授权给新用户：rabbitmqctl set_permissions -p vhost_name username '.*' '.*' '.*'
 *
 * 角色说明
 * none  最小权限角色
 * management 管理员角色
 * policymaker   决策者
 * monitoring  监控
 * administrator  超级管理员
 */
@Controller
public class ProducterController {

    public final static String QUEUE_NAME="rabbitMQ.test";

    @RequestMapping("/product")
    public String index(Model model){
        try {
            createProduct();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "index";
    }

    /**
     * 注1：queueDeclare第一个参数表示队列名称、
     * 第二个参数为是否持久化（true表示是，队列将在服务器重启时生存）、
     * 第三个参数为是否是独占队列（创建者可以使用的私有队列，断开后自动删除）、
     * 第四个参数为当所有消费者客户端连接断开时是否自动删除队列、
     * 第五个参数为队列的其他参数
     *
     * 注2：basicPublish第一个参数为交换机名称、
     * 第二个参数为队列映射的路由key、
     * 第三个参数为消息的其他属性、
     * 第四个参数为发送信息的主体
     * @throws IOException
     * @throws TimeoutException
     */
    private void createProduct() throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("localhost");
        //factory.setUsername("lp");
        //factory.setPassword("");
        // factory.setPort(2088);
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //  声明一个队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello RabbitMQ";
        //发送消息到队列中
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println("Producer Send +'" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }

    @RequestMapping("/task")
    public String muliTask(Model model){
        try {
            createMoreTask();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "index";
    }

    private static final String TASK_QUEUE_NAME="task_queue";

    /**
     * 多任务
     * @throws IOException
     * @throws TimeoutException
     */
    private void createMoreTask() throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection=factory.newConnection();
        Channel channel=connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);
        //分发信息
        for (int i=0;i<10;i++){
            String message="Hello RabbitMQ"+i;
            channel.basicPublish("",TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
            System.out.println("NewTask send '"+message+"'");
        }
        channel.close();
        connection.close();
    }

}
