package com.lld.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.process.BaseProcess;
import com.lld.im.tcp.process.ProcessFactory;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class MessageReceiver {
    private static Integer brokerId;

    public static void init (Integer brokerId) {
        if (Objects.isNull(MessageReceiver.brokerId)) {
            MessageReceiver.brokerId = brokerId;
        }
        doReceiveMessage();
    }


    private static void doReceiveMessage () {
        try {
            String channelName = Constants.RabbitConstants.MessageService2Im + brokerId;
            String queueName = Constants.RabbitConstants.MessageService2Im + brokerId;
            String exchangeName = Constants.RabbitConstants.MessageService2Im;

            Channel channel = MqFactory.getChannel(channelName);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, String.valueOf(brokerId));
            channel.basicConsume(queueName, false, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        String msg = new String(body);
                        log.info(msg);
                        MessagePack messagePack = JSONObject.parseObject(msg, MessagePack.class);
                        BaseProcess messageProcess = ProcessFactory.getMessageProcess();
                        messageProcess.process(messagePack);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        log.error("receive message error, e: ", e);
                        channel.basicNack(envelope.getDeliveryTag(), false, false);
                    }
                }
            });
        } catch (Exception e) {
            log.error("接受消息异常, e: ", e);
        }
    }
}
