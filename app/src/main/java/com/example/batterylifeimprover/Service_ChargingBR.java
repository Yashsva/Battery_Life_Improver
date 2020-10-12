package com.example.batterylifeimprover;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class Service_ChargingBR extends Service {

    private static final String TAG = "battery";

    //Unique notification ID
    private final int NOTIFICATION_ID = 1001;
    //notification channel ID
    private final String NOTIFICATION_CHANNEL_ID="ChargingService101";


    private int maxChargingLevelPercent = 91;

    //To save charging status
    private ChargingStatus chargingStatus = new ChargingStatus();


    //Interface for communication with UI
    private ChargingCallBackInterface chargingCallBack;

    //Binder to bind the UI with the service
    private Binder_Charging binder_charging = new Binder_Charging();

    private BroadcastReceiver_Charging chargingStatusBR = new BroadcastReceiver_Charging();
    private BroadcastReceiver_Charging batteryLevelBR = new BroadcastReceiver_Charging();

    @Override
    public void onCreate() {
        super.onCreate();

        //display notification
        Notification notification=getNotification("Battery","Not Charging");

         /*display notification and make our service run in
         foreground( responsible for running the service even after closing of app)*/
        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder_charging;
    }

    // callback setter
    public void setChargingCallBack(ChargingCallBackInterface chargingCallBack) {
        this.chargingCallBack = chargingCallBack;
    }

    //setting the maximum charging level percent
    public void setMaxChargingLevelPercent(int maxChargingLevelPercent) {
        this.maxChargingLevelPercent = maxChargingLevelPercent;
    }

    //get the current charging status
    public ChargingStatus getChargingStatus() {
        return chargingStatus;
    }

    //Class for bind the UI with the service instance
    public class Binder_Charging extends Binder {
        public Service_ChargingBR getChargingService() {
            return Service_ChargingBR.this;
        }
    }

    // start watching whether charging/Not charging ?
    public void startObservingChargingStatus() {
        IntentFilter filter_chargingStatus = new IntentFilter();
        filter_chargingStatus.addAction(Intent.ACTION_POWER_CONNECTED);
        filter_chargingStatus.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(chargingStatusBR, filter_chargingStatus);
    }

    //start watching battery level after charging started
    private void startObservingBatteryLevel() {
        IntentFilter filter_batteryLevel = new IntentFilter();
        filter_batteryLevel.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelBR, filter_batteryLevel);

        //update notification
        updateNotification();

        //play audio for charging started
        playChargingStartAudio();
    }

    //stop watching battery level
    private void stopObservingBatteryLevel() {
        unregisterReceiver(batteryLevelBR);

        //update notification
        updateNotification();



    }

    //update the foreground process notification
    private  void updateNotification()
    {
        Log.i(TAG,"Updating Notification");
        //title and text for notification
        String title="",text="Tap to view info";
        if(chargingStatus.isCharging())
        {
            if((chargingStatus.getChargingPercent())>=maxChargingLevelPercent)
            {
                title="Charging Complete ! ";
                text="Please unplug the charger";
            }
            else
            {
                title="Charging ";

            }
        }
        else
        {
            title="Not Charging";
        }

        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification=getNotification(title,text);

        notificationManager.notify(NOTIFICATION_ID,notification);
    }

    //make a notification to show battery charging status
    private Notification getNotification(String title,String text)
    {
        //Pending intent to launch our activity if user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);


        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(Service_ChargingBR.this,NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)    //the status text
                    .setContentTitle(title)     //the label of the entry
                    .setContentText(text)   // set contents of the entry
                    .setContentIntent(contentIntent) // intent to send when entry is clicked
                    .setOnlyAlertOnce(true) // to alert the user
                    .build();
        }
        else
        {
            notification = new Notification.Builder(Service_ChargingBR.this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)    //the status text
                    .setContentTitle(title)     //the label of the entry
                    .setContentText(text)   // set contents of the entry
                    .setContentIntent(contentIntent) // intent to send when entry is clicked
                    .setOnlyAlertOnce(true) // to alert the user
                    .build();
        }

        return notification;
    }

    //play audio for charging start
    private void playChargingStartAudio()
    {
        //set the volume to maximum value
        final AudioManager audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        final int currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);

        MediaPlayer mediaPlayer=MediaPlayer.create(this,R.raw.charging_started);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
            }
        });
        mediaPlayer.start();
    }

    //play audio when battery level goes above maxChargingLevelPercent
    private void playChargingCompletedAudio()
    {
        //set the volume to maximum value
        final AudioManager audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        final int currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);

        MediaPlayer mediaPlayer=MediaPlayer.create(this,R.raw.charging_completed);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
            }
        });
        mediaPlayer.start();
    }


    //listen for charging/not charging / current battery level state
    private class BroadcastReceiver_Charging extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();


            switch (action) {
                case (Intent.ACTION_POWER_CONNECTED):
                    Log.i(TAG, "Power Connected");

                    chargingCallBack.onChargingStart();

                    //charging started -> change charging status's isCharging to true
                    chargingStatus.setCharging(true);
                    //reset charging level
                    chargingStatus.setChargingPercent(0);

                    startObservingBatteryLevel();



                    break;

                case (Intent.ACTION_POWER_DISCONNECTED):

                    Log.i(TAG, "Power Disconnected");

                    //charging stopped -> change charging status's isCharging to false
                    chargingStatus.setCharging(false);

                    chargingCallBack.onChargingStop();

                    stopObservingBatteryLevel();



                    break;

                case (Intent.ACTION_BATTERY_CHANGED):

                    Log.i(TAG, "Power Level Changed");

                    int chargingType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int batteryLevelPercentage = -1;

                    if (level >= 0 && scale >= 0) {
                        batteryLevelPercentage = (level * 100) / scale;

                        chargingCallBack.onChargingProgress(batteryLevelPercentage);

                        //change charging status's charging percent
                        chargingStatus.setChargingPercent(batteryLevelPercentage);

                        Log.i(TAG, "Battery Level Remaining : " + batteryLevelPercentage + "%");

                        if (batteryLevelPercentage >= maxChargingLevelPercent) {
                            Log.i(TAG, "Charging Completed");
                            chargingCallBack.onChargingCompleted();

                            //play audio for charging completed
                            playChargingCompletedAudio();

                            //notify for charging complete
                            updateNotification();
                        }

                    }

                    break;
            }
        }


    }


}
