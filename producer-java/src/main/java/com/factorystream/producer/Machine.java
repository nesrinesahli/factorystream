package com.factorystream.producer;

import java.util.Random;
import java.util.UUID;

public class Machine {
    private final String machineId;
    private final String lineId;
    private final MachineType type;
    private final Random random;

    public Machine(String machineId, String lineId, MachineType type, Random random) {
        this.machineId = machineId;
        this.lineId = lineId;
        this.type = type;
        this.random = random;
    }

    public String machineId() {
        return machineId;
    }

    public SensorEvent nextReading(long eventTime) {
        return new SensorEvent(
                UUID.randomUUID().toString(),
                machineId,
                lineId,
                type,
                eventTime,
                round(gaussian(type.tempMean, type.tempStd)),
                round(gaussian(type.vibrationMean, type.vibrationStd)),
                round(gaussian(type.pressureMean, type.pressureStd)),
                (int) Math.round(gaussian(type.cadenceMean, type.cadenceStd)),
                null
        );
    }

    private double gaussian(double mean, double std) {
        return mean + random.nextGaussian() * std;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
