package com.example.locationalert;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class LocationService extends Service {
//    public LocationService() {
//
//    }

    private final int LOCATION_REFRESH_TIME = 400; // 5 seconds to update
    private final int LOCATION_REFRESH_DISTANCE = 1; // 1 meters to update

    private void createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0x1", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder redAreaBuilder;
    private NotificationCompat.Builder blueAreaBuilder;
    private int notificationId = 0;


    public final LocationListener mLocationListener = new LocationListener() {
        private long lastColor;


        @Override
        public void onLocationChanged(final Location location) {
            LocationView view = LocationDetector.getLocationView(location.getLongitude(), location.getLatitude());
            long color = view.color;
            sendDataToActivity(view);
            Log.i("Color", Long.toString(color, 10));
            if (color != lastColor) {
                if (color == Color.BLUE) {
                    blueAreaBuilder.setWhen(System.currentTimeMillis());
                    notificationManager.notify(notificationId++, blueAreaBuilder.build());
                } else if (color == Color.RED) {
                    redAreaBuilder.setWhen(System.currentTimeMillis());
                    notificationManager.notify(notificationId++, redAreaBuilder.build());
                }
                lastColor = color;
            }
        }
    };

    private void sendDataToActivity(LocationView view)
    {
        Intent sendView = new Intent();
        sendView.setAction("LOCATION_ACTION");
        sendView.putExtra( "LOCATION_VALUE",view.toString());
        sendBroadcast(sendView);
    }
    @Override
    public void onCreate() {
        Log.i("SERVICE_LC", "Created");
        notificationManager = NotificationManagerCompat.from(this);
        LocationDetector.InitSingleton(this, 46.326797, 38.055580, 46.328095, 38.055017);
        redAreaBuilder = new NotificationCompat.Builder(this, "0x1")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("RED AREA!")
                .setContentText("You have just entered the red area.")
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                .setAutoCancel(false);
        blueAreaBuilder = new NotificationCompat.Builder(this, "0x1")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("BLUE AREA!")
                .setContentText("You have just entered the blue area.")
                .setAutoCancel(true);
        NotificationCompat.Builder staticNotifications = new NotificationCompat.Builder(this, "0x1")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(false)
                .setContentTitle("App is running on foreground")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true);
        startForeground(9999, staticNotifications.build());
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean accessGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (accessGranted) {
            mLocationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        createNotificationChannel();
        Log.i("SERVICE_LC", "Started");


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SERVICE_LC", "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}