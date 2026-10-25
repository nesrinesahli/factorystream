package com.factorystream.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.UUID;

public class App {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        SensorEvent event = new SensorEvent(
                UUID.randomUUID().toString(),
                "M001",
                "L1",
                MachineType.PRESS,
                System.currentTimeMillis(),
                72.4,
                2.1,
                6.0,
                30,
                null
        );

        System.out.println(event);                            // Java toString
        System.out.println(mapper.writeValueAsString(event)); // JSON
    }
}
