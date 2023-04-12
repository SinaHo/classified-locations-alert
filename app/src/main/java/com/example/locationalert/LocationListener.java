package com.example.locationalert;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class LocationListener extends Service {
    public LocationListener() {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}