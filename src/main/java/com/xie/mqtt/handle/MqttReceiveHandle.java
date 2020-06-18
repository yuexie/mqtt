package com.xie.mqtt.handle;

/**
 * @program: mqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-18 09:28
 **/

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * mqtt客户端消息处理类
 * **/
@Slf4j
@Component
public class MqttReceiveHandle {

    public void handle(Message<?> message){
        log.info("主题：{}，QOS:{}，消息接收到的数据：{}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC),
                message.getHeaders().get(MqttHeaders.RECEIVED_QOS),
                message.getPayload());
    }
}
