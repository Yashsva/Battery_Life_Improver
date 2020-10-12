package com.example.batterylifeimprover;

public interface ChargingCallBackInterface {

    void onChargingStart();

    void onChargingProgress(int batteryPercent);

    void onChargingCompleted();

    void onChargingStop();
}
