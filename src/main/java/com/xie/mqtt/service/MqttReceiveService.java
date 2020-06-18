package com.xie.mqtt.service;

import org.springframework.stereotype.Service;

/**
 * @program: springandnqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-17 19:34
 **/

@Service
public interface MqttReceiveService {
    void handlerMqttMessage(String topic, String msg);
}
