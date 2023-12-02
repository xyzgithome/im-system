package com.lld.im.tcp.receiver;

import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MessageReceiver {

    public static void init () {
        startReceiverMessage();
    }


    private static void startReceiverMessage () {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im);

            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im, true, false, false, null);

            channel.queueBind(Constants.RabbitConstants.MessageService2Im, Constants.RabbitConstants.MessageService2Im, "");

            channel.basicConsume(Constants.RabbitConstants.MessageService2Im, false, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // TODO 处理消息服务发来的消息
                    String msg = new String(body);
                    log.info("----------" + msg);
                }
            });
        } catch (Exception e) {
            log.error("接受消息异常, e: ", e);
        }
    }
}
