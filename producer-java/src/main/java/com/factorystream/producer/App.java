package com.factorystream.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        Config config = Config.fromEnv();
        Random random = config.seed() == null ? new Random() : new Random(config.seed());
        List<Machine> fleet = createFleet(config.machines(), random, config.anomalyRate());

        // Ctrl+C: stop the loop, then wait for main to finish closing the producer
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                mainThread.join();
            } catch (InterruptedException ignored) {
            }
        }));

        try (SensorProducer producer = new SensorProducer(config.bootstrapServers(), config.topic())) {
            System.out.println("Starting with " + config);
            long intervalNanos = (long) (1_000_000_000L / config.ticksPerSecond());
            long start = System.nanoTime();
            long lastReport = start;
            long lastSent = 0;
            long tick = 0;

            while (running) {
                long now = System.currentTimeMillis();
                for (Machine machine : fleet) {
                    producer.send(machine.nextReading(now));
                }
                tick++;

                long elapsed = System.nanoTime() - lastReport;
                if (elapsed >= 10_000_000_000L) {
                    long sent = producer.sentCount();
                    double rate = (sent - lastSent) * 1e9 / elapsed;
                    System.out.printf("sent=%d failed=%d rate=%.0f events/s%n", sent, producer.failedCount(), rate);
                    lastReport = System.nanoTime();
                    lastSent = sent;
                }

                // Sleep until the next tick is due, so slow sends don't make the rate drift
                long sleepNanos = start + tick * intervalNanos - System.nanoTime();
                if (sleepNanos > 0) {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                }
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
