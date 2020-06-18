package com.xie.mqtt.service;

/**
 * @program: springandnqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-17 19:39
 **/

public class MqttReceiveServiceImpl implements MqttReceiveService {

    public void handlerMqttMessage(String topic, String msg){
        System.out.println("mqtt-msg-MqttReceiveServiceImpl:");
        System.out.println(msg);
    }

}
