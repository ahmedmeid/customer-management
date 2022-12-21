package com.ahmedmeid.fleet.service;

import com.ahmedmeid.fleet.config.KafkaProducer;
import com.ahmedmeid.fleet.domain.Vehicle;
import com.ahmedmeid.fleet.service.dto.VehicleDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class VehicleCreatedEventPublisherService {

    private final Logger log = LoggerFactory.getLogger(VehicleCreatedEventPublisherService.class);

    private KafkaProducer kafkaProducer;

    public VehicleCreatedEventPublisherService(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void publishEvent(Vehicle vehicle) {
        VehicleDTO dto = VehicleDTO
            .builder()
            .uuid(UUID.randomUUID())
            .vehicleId(vehicle.getVehicleId())
            .vehicleRegNo(vehicle.getVehicleRegNo())
            .deviceId(vehicle.getDeviceId())
            .build();
        log.debug("Request the message : {} to send to new-vehicle-created topic ", dto);
        Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
        MessageHeaders headers = new MessageHeaders(map);
        kafkaProducer.sendMessage(new GenericMessage<>(dto, headers));
    }
}
