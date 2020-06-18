package com.xie.mqtt.service;

/**
 * @program: mqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-18 10:46
 **/

public class MqttReceiveServiceWater implements MqttReceiveService {

    public void handlerMqttMessage(String topic, String msg){
        System.out.println("mqtt-msg-MqttReceiveServiceWater:");
        System.out.println(msg);
    }
}
