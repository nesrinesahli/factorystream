package com.factorystream.producer;

public record SensorEvent(
        String eventId,
        String machineId,
        String lineId,
        MachineType machineType,
        long eventTime,
        double temperature,
        double vibration,
        double pressure,
        int cadence,
        String errorCode
) {}
