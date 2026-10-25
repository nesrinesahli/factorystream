package com.factorystream.producer;

import java.util.Random;
import java.util.UUID;

public class Machine {
    private enum State { NORMAL, DRIFTING, BROKEN }

    private static final int DRIFT_TICKS = 60;      // drift lasts 1 min before failure
    private static final double DRIFT_STEP = 0.1;   // +0.1 sigma per tick, so +6 sigma at failure
    private static final int MIN_REPAIR_TICKS = 30;
    private static final int MAX_REPAIR_TICKS = 120;

    private final String machineId;
    private final String lineId;
    private final MachineType type;
    private final Random random;
    private final double anomalyRate;

    private State state = State.NORMAL;
    private int ticksLeft;
    private double drift;          // offset in standard deviations
    private String errorCode;

    public Machine(String machineId, String lineId, MachineType type, Random random, double anomalyRate) {
        this.machineId = machineId;
        this.lineId = lineId;
        this.type = type;
        this.random = random;
        this.anomalyRate = anomalyRate;
    }

    public String machineId() {
        return machineId;
    }

    public SensorEvent nextReading(long eventTime) {
        boolean spike = updateState();

        double temperature = gaussian(type.tempMean, type.tempStd, drift);
        double vibration = gaussian(type.vibrationMean, type.vibrationStd, drift);
        double pressure = gaussian(type.pressureMean, type.pressureStd, 0);
        int cadence = (int) Math.round(gaussian(type.cadenceMean, type.cadenceStd, 0));

        if (spike) {
            vibration *= 4;
        }
        if (state == State.BROKEN) {
            vibration = 0;
            cadence = 0;
        }

        return new SensorEvent(
                UUID.randomUUID().toString(),
                machineId,
                lineId,
                type,
                eventTime,
                round(temperature),
                round(vibration),
                round(pressure),
                cadence,
                errorCode
        );
    }

    // Advances the state machine by one tick. Returns true if this reading gets a vibration spike.
    private boolean updateState() {
        switch (state) {
            case NORMAL -> {
                double roll = random.nextDouble();
                if (roll < anomalyRate) {
                    startDrift();
                } else if (roll < anomalyRate * 3) {
                    return true;
                } else if (roll < anomalyRate * 3.2) {
                    breakDown("E02");
                }
            }
            case DRIFTING -> {
                drift += DRIFT_STEP;
                if (--ticksLeft == 0) {
                    breakDown("E01");
                }
            }
            case BROKEN -> {
                if (--ticksLeft == 0) {
                    repair();
                }
            }
        }
        return false;
    }

    private void startDrift() {
        state = State.DRIFTING;
        ticksLeft = DRIFT_TICKS;
        log("NORMAL -> DRIFTING");
    }

    private void breakDown(String code) {
        log(state + " -> BROKEN (" + code + ")");
        state = State.BROKEN;
        ticksLeft = MIN_REPAIR_TICKS + random.nextInt(MAX_REPAIR_TICKS - MIN_REPAIR_TICKS + 1);
        errorCode = code;
    }

    private void repair() {
        state = State.NORMAL;
        drift = 0;
        errorCode = null;
        log("BROKEN -> NORMAL (repaired)");
    }

    private void log(String message) {
        System.err.println("[" + machineId + "] " + message);
    }

    private double gaussian(double mean, double std, double offset) {
        return mean + (random.nextGaussian() + offset) * std;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
