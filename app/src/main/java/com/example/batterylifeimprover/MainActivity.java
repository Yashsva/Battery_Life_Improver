package com.example.batterylifeimprover;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection, ChargingCallBackInterface {

    private TextView txtBatteryLevel, txtChargingStatus,txtHealth,txtTemperature,txtTechnology,txtVoltage;

    private static final String TAG = "battery";

    private Service_ChargingBR service_chargingBR;

    // store the current  charging status
    private ChargingStatus chargingStatus=new ChargingStatus();

    private Battery_Info battery_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showToastMessage("onCreate");
        txtChargingStatus = findViewById(R.id.txtBatteryChargingStatus);
        txtBatteryLevel = findViewById(R.id.txtBatteryLevel);

        txtTechnology=findViewById(R.id.txtTechnology);
        txtHealth=findViewById(R.id.txtHealth);
        txtTemperature=findViewById(R.id.txtTemperature);
        txtVoltage=findViewById(R.id.txtVoltage);

        txtChargingStatus.setText("Not Charging");
        txtBatteryLevel.setVisibility(View.GONE);


        bindToChargingService();

        battery_info=new Battery_Info(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        showToastMessage("OnStart Method Called");
        if(battery_info.isBatteryInfo_ready())
        {
            updateBatteryInfoOnUI();
        }
        else
        {
            battery_info.retrieve_battery_info();
            updateBatteryInfoOnUI();

        }

    }

    //For showing toast message on the screen
    void showToastMessage(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindToChargingService() {
        Intent intent = new Intent(this, Service_ChargingBR.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Log.i(TAG, "Service Connected");
        showToastMessage("Service Connected");
        service_chargingBR = ((Service_ChargingBR.Binder_Charging) service).getChargingService();

        service_chargingBR.setChargingCallBack(this);

        //start charging service(for indefinite time)
        if (service_chargingBR != null) {
            service_chargingBR.startObservingChargingStatus();

            chargingStatus=service_chargingBR.getChargingStatus();
            updateChargingStatus();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        Log.i(TAG, "Service Disconnected");
        showToastMessage("Service Disconnected");


    }

    @Override
    public void onChargingStart() {

        showToastMessage("Charging Started");
        chargingStatus.setCharging(true);
        updateChargingStatus();


    }

    @Override
    public void onChargingProgress(int batteryPercent) {
        chargingStatus.setChargingPercent(batteryPercent);
        updateChargingStatus();
    }

    @Override
    public void onChargingCompleted() {

        showToastMessage("Charging Completed");
        updateChargingStatus();

    }

    @Override
    public void onChargingStop() {

        chargingStatus.setCharging(false);
        showToastMessage("Charging Stopped");
        updateChargingStatus();

    }


    public void updateChargingStatus() {
        if (chargingStatus.isCharging()) {
            if ((chargingStatus.getChargingPercent()) >= 95)   // update 95 to user specified maximum  battery Level
            {
                txtChargingStatus.setText("Charging");
                txtBatteryLevel.setVisibility(View.GONE);

            } else {
                txtChargingStatus.setText("Charging");
                txtBatteryLevel.setText(chargingStatus.getChargingPercent() + "%");
                txtBatteryLevel.setVisibility(View.VISIBLE);
            }
        } else {
            txtChargingStatus.setText("Not Charging");
            txtBatteryLevel.setVisibility(View.GONE);

        }
    }

    public void updateBatteryInfoOnUI()
    {

            showToastMessage("Updating UI");
            txtVoltage.setText(Integer.toString(battery_info.getVoltage()));
            txtTemperature.setText(Integer.toString(battery_info.getTemperature()));
            txtHealth.setText(battery_info.getHealth());
            txtTechnology.setText(battery_info.getTechnology());


    }


}
