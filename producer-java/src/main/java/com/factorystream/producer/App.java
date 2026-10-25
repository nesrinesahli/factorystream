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

        List<Machine> fleet = createFleet(6, new Random(42));

        for (int tick = 0; tick < 3; tick++) {
            long now = System.currentTimeMillis();
            for (Machine machine : fleet) {
                System.out.println(mapper.writeValueAsString(machine.nextReading(now)));
            }
            System.out.println("---");
            Thread.sleep(1000);
        }
    }

    static List<Machine> createFleet(int count, Random random) {
        MachineType[] types = MachineType.values();
        List<Machine> fleet = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String machineId = String.format("M%03d", i + 1);
            String lineId = "L" + (i * 3 / count + 1);
            MachineType type = types[i % types.length];
            fleet.add(new Machine(machineId, lineId, type, random));
        }
        return fleet;
    }
}
