package com.factorystream.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Machine> fleet = createFleet(6, new Random(42), 0.02);   // high rate, to see anomalies fast

        for (int tick = 0; tick < 300; tick++) {
            long now = System.currentTimeMillis();
            for (Machine machine : fleet) {
                SensorEvent event = machine.nextReading(now);
                if (machine.machineId().equals("M001")) {
                    System.out.println(mapper.writeValueAsString(event));
                }
            }
            Thread.sleep(100);   // 10 ticks/s, to speed up the test
        }

    }

    static List<Machine> createFleet(int count, Random random, double anomalyRate) {
        MachineType[] types = MachineType.values();
        List<Machine> fleet = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String machineId = String.format("M%03d", i + 1);
            String lineId = "L" + (i * 3 / count + 1);
            MachineType type = types[i % types.length];
            fleet.add(new Machine(machineId, lineId, type, random, anomalyRate));
        }
        return fleet;
    }
}
