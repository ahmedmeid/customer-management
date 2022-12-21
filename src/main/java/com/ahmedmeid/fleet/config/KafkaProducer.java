package com.ahmedmeid.fleet.config;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private StreamBridge streamBridge;

    public KafkaProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(GenericMessage<?> message) {
        streamBridge.send("producer-out-0", message);
    }
}
