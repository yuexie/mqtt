package com.xie.mqtt.controller;

import com.xie.mqtt.config.MqttConfig;
import com.xie.mqtt.service.MqttGateway;
import com.xie.mqtt.service.MqttReceiveServiceImpl;
import com.xie.mqtt.service.MqttReceiveServiceWater;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @program: mqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-18 09:37
 **/

@RestController
public class MqttController {

    @Resource
    private MqttGateway mqttGateway;

    @Resource
    private MqttConfig mqttConfig;


    @GetMapping("/add")
    public String addTopic(){
        String [] topics = {"aaa","bbb"};
        List<String> list = mqttConfig.addListenTopic(topics);
        mqttConfig.setMqttReceiveService(new MqttReceiveServiceImpl());

        return list.toString();
    }

    @GetMapping("/pub")
    public String pubTopic(){
        String topic = "cli-topic";
        String msg   = "client msg at: " + String.valueOf(System.currentTimeMillis());
        mqttGateway.sendToMqtt(topic, msg);

        return "OK";

    }

    @GetMapping("/del")
    public String delTopic(String topic){
        List<String> list = mqttConfig.removeListenTopic(topic);
        return list.toString();
    }

    @GetMapping("/start")
    public String startWater(){
        String [] topics = {"zkyc/water"};
        List<String> list = mqttConfig.addListenTopic(topics);

        return list.toString();
    }

    @GetMapping("/stop")
    public String stopWater(){
        List<String> list = mqttConfig.removeListenTopic("zkyc/water");
        return list.toString();
    }

}
