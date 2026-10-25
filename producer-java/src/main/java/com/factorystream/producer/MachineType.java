package com.factorystream.producer;

public enum MachineType {
    //            temp (°C)   vibration (mm/s)  pressure (bar)  cadence (parts/min)
    PRESS(        60.0, 2.0,  3.0, 0.3,         150.0, 3.0,     20, 1),
    WELDING_ROBOT(45.0, 1.5,  1.5, 0.2,         6.0, 0.2,       40, 2),
    PAINT_BOOTH(  25.0, 0.5,  0.5, 0.1,         2.0, 0.1,       10, 1);

    final double tempMean, tempStd;
    final double vibrationMean, vibrationStd;
    final double pressureMean, pressureStd;
    final int cadenceMean, cadenceStd;

    MachineType(double tempMean, double tempStd,
                double vibrationMean, double vibrationStd,
                double pressureMean, double pressureStd,
                int cadenceMean, int cadenceStd) {
        this.tempMean = tempMean;
        this.tempStd = tempStd;
        this.vibrationMean = vibrationMean;
        this.vibrationStd = vibrationStd;
        this.pressureMean = pressureMean;
        this.pressureStd = pressureStd;
        this.cadenceMean = cadenceMean;
        this.cadenceStd = cadenceStd;
    }
}
