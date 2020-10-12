package com.example.batterylifeimprover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Battery_Info  {

    private String technology,health;
    private int temperature,voltage;
    private boolean isBatteryInfo_ready=false;

    private Context context;

    private static final String TAG="battery_info";

    Battery_Info(Context context)
    {
        this.context=context;
    }

    public boolean isBatteryInfo_ready() {
        return isBatteryInfo_ready;
    }

    public String getTechnology() {
        return technology;
    }

    public String getHealth() {
        return health;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public void setBatteryInfo_ready(boolean batteryInfo_ready) {
        isBatteryInfo_ready = batteryInfo_ready;
    }



    void retrieve_battery_info()
    {
        Log.i(TAG,"Inside retrieve_battery_info");
        showToastMessage("Inside retrieve_battery_info");
        IntentFilter batteryFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        //register receiver
        context.registerReceiver(battery_receiver,batteryFilter);

    }

    private BroadcastReceiver battery_receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
            {

                setTechnology(intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY));
                setTemperature(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0));
                setVoltage(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0));
                int health=intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
                setHealth(getHealthString(health));
                setBatteryInfo_ready(true);
                showToastMessage("Unregistering receiver");
                context.unregisterReceiver(battery_receiver);




            }

        }
    };



    private String getHealthString(int health)
    {

        switch (health)
        {
            case BatteryManager.BATTERY_HEALTH_DEAD:    return "Dead";

            case BatteryManager.BATTERY_HEALTH_GOOD:    return "Good";

            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over Voltage";

            case BatteryManager.BATTERY_HEALTH_OVERHEAT:    return "Over Heat";

            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: return "Failure";

            case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";

            case BatteryManager.BATTERY_HEALTH_UNKNOWN: return "Unknown";
        }

        return "";
    }


    //For showing toast message on the screen
    void showToastMessage(String message) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }






}
