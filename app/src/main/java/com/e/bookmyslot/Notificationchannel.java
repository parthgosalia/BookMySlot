package com.e.bookmyslot;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Notificationchannel extends Application {
    public static final String Channel1 = "Notification Channel";
    public static final String Channel2 = "Silent Channel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        createNotificationChannel2();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    Channel1,
                    "Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription("This is channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }
    private void createNotificationChannel2() {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    Channel2,
                    "Channel2",
                    NotificationManager.IMPORTANCE_MIN
            );
            notificationChannel.setDescription("This is channel 2");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

}
