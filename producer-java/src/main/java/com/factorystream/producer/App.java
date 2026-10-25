package com.factorystream.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        List<Machine> fleet = createFleet(50, new Random(), 0.002);

        // Ctrl+C: stop the loop, then wait for main to finish closing the producer
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                mainThread.join();
            } catch (InterruptedException ignored) {
            }
        }));

        try (SensorProducer producer = new SensorProducer("localhost:9092", "sensor.raw")) {
            System.out.println("Producing for " + fleet.size() + " machines. Ctrl+C to stop.");
            long tick = 0;
            while (running) {
                long now = System.currentTimeMillis();
                for (Machine machine : fleet) {
                    producer.send(machine.nextReading(now));
                }
                tick++;
                if (tick % 10 == 0) {
                    System.out.println("sent=" + producer.sentCount() + " failed=" + producer.failedCount());
                }
                Thread.sleep(1000);
            }
        }
        System.out.println("Producer closed.");
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
