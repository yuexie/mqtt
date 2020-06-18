package com.xie.mqtt.config;

/**
 * @program: mqtt
 * @description:
 * @author: xieyue
 * @create: 2020-06-18 09:27
 **/


import com.alibaba.fastjson.JSONObject;
import com.xie.mqtt.handle.MqttReceiveHandle;
import com.xie.mqtt.service.MqttReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 *  mqtt 推送and接收 消息类
 * **/
@Configuration
@IntegrationComponentScan
@Slf4j
public class MqttConfig {

    private static final byte[] WILL_DATA;

    static {
        WILL_DATA = "offline".getBytes();
    }

    @Autowired
    private MqttReceiveHandle mqttReceiveHandle;

    @Value("${spring.mqtt.username}")
    private String username;

    @Value("${spring.mqtt.password}")
    private String password;

    @Value("${spring.mqtt.url}")
    private String hostUrl;

    @Value("${spring.mqtt.client.id}")
    private String clientId;

    @Value("${spring.mqtt.default.topic}")
    private String defaultTopic;
    //水质设备主题
    @Value("${spring.mqtt.default.water}")
    private String water;

    @Value("${spring.mqtt.completionTimeout}")
    private int completionTimeout;   //连接超时

    //消息驱动
    private MqttPahoMessageDrivenChannelAdapter adapter;

    //订阅的主题列表
    private String listenTopics = "";

    //mqtt消息接收接口
    private MqttReceiveService mqttReceiveService;

    public void setMqttReceiveService(MqttReceiveService mqttReceiveService){
        this.mqttReceiveService = mqttReceiveService;
    }

    /**
     *  MQTT连接器选项
     * **/
    @Bean(value = "getMqttConnectOptions")
    public MqttConnectOptions getMqttConnectOptions1(){
        MqttConnectOptions mqttConnectOptions=new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        mqttConnectOptions.setCleanSession(true);
        // 设置超时时间 单位为秒
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setServerURIs(new String[]{hostUrl});
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
        mqttConnectOptions.setKeepAliveInterval(10);
        // 设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息。
        //mqttConnectOptions.setWill("willTopic", WILL_DATA, 2, false);
        return mqttConnectOptions;
    }

    /**
     * MQTT工厂
     * **/
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions1());
        return factory;
    }

    /**
     * MQTT信息通道（生产者）
     * **/
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT消息处理器（生产者）
     * **/
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =  new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(defaultTopic);
        return messageHandler;
    }

    /**
     * 配置client,监听的topic
     * MQTT消息订阅绑定（消费者）
     * **/
    @Bean
    public MessageProducer inbound() {
        if(adapter == null){
            adapter = new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound1", mqttClientFactory(),
                    defaultTopic);
        }
        String [] topics = listenTopics.split(",");
        for(String topic: topics){
            if(!StringUtils.isEmpty(topic)){
                adapter.addTopic("zkyc/" + topic,1);
            }
        }
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
    /**
     * @Description: 增加监听的topic
     * @Param:
     * @Author:      xieyue
     * @Date:        2020/6/17
     */
    public List<String> addListenTopic(String [] topicArr){
        if(adapter == null){
            adapter = new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound1", mqttClientFactory(),
                    defaultTopic);
        }
        List<String> listTopic = Arrays.asList(adapter.getTopic());
        for(String topic: topicArr){
            if(!StringUtils.isEmpty(topic)){
                if(!listTopic.contains(topic)){
                    adapter.addTopic(topic,1);
                }
            }
        }
        return Arrays.asList(adapter.getTopic());
    }

    /**
     * @Description: 移除一个监听的topic
     * @Param:
     * @Author:      xieyue
     * @Date:        2020/6/17
     */
    public List<String> removeListenTopic(String topic){
        if(adapter == null){
            adapter = new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound1", mqttClientFactory(),
                    defaultTopic);
        }
        List<String> listTopic = Arrays.asList(adapter.getTopic());
        if(listTopic.contains(topic)){
            adapter.removeTopic(topic);
        }
        return Arrays.asList(adapter.getTopic());
    }
    /**
     * MQTT信息通道（消费者）
     * **/
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
    /**
     * MQTT消息处理器（消费者）
     * **/
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println("########### mqtt-sub-msg #############");
                System.out.println(message);
                //处理接收消息
                mqttReceiveHandle.handle(message);

                //String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
                //String msg   = ((String) message.getPayload()).toString();
                //mqttReceiveService.handlerMqttMessage(topic,msg);
            }
        };
    }
}
