package com.example.batterylifeimprover;

public class ChargingStatus {
    private int chargingPercent;
    private boolean isCharging;

    public int getChargingPercent() {
        return chargingPercent;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    public void setChargingPercent(int chargingPercent) {
        this.chargingPercent = chargingPercent;
    }
}
