package com.example.locationalert;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.locationalert.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationDataReceiver locationReceiver;

    class LocationDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("LOCATION_ACTION")) {
                String locationData = intent.getStringExtra("LOCATION_VALUE");
                LocationView view = LocationView.parseView(locationData);
                Log.i("LOCATION_VIEW_STRING", view.toString());
                Log.i("LOCATION_DATA", locationData);
                TextView mainText = (TextView) findViewById(R.id.textview_first);
                mainText.setText(locationData);
                ImageView imgView = (ImageView) findViewById(R.id.areasImage);
                int x = view.columnPixel;
                int y = view.rowPixel;
                Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.areas);
                image = image.copy(Bitmap.Config.ARGB_8888, true);
                Log.i("IMAGE_MANIPULATION", "Image generated");
                int i, j;
                if (x > -1 && y > -1) {

                    for (i = Math.max(x - 50, 0); i < Math.min(image.getWidth(), x + 50); i++) {
                        for (j = Math.max(y - 50, 0); j < Math.min(image.getHeight(), y + 50); j++) {
                            image.setPixel(i, j, ContextCompat.getColor(context, R.color.yellow_600));
                        }
                    }
                    Log.i("IMAGE_MANIPULATION", "Image edited");
                }
                imgView.setImageBitmap(image);
                Log.i("IMAGE_MANIPULATION", "Image set");
            }

        }

    }

//    private int notificationId = 0;
//    private long lastColor = Color.WHITE;
//    public final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(final Location location) {
//            long color = LocationDetector.getLocationColor(location.getLongitude(), location.getLatitude());
//            TextView mainText = (TextView) findViewById(R.id.textview_first);
//            mainText.setText(String.format("%s : %s \ncolor is : %s\nis mock %s, %s", location.getLatitude(), location.getLongitude(), color == Color.WHITE ? "White" : color == Color.BLUE ? "Blue" : "RED ", location.isFromMockProvider(), location.getProvider()));
//            Log.i("Color", Long.toString(color, 10));
//            if (color != lastColor) {
//                if (color == Color.BLUE) {
//                    notificationManager.notify(notificationId++, blueAreaBuilder.build());
//                } else if (color == Color.RED) {
//                    notificationManager.notify(notificationId++, redAreaBuilder.build());
//                }
//                lastColor = color;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationReceiver = new LocationDataReceiver();
        registerReceiver(locationReceiver, new IntentFilter("LOCATION_ACTION"));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        boolean accessGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!accessGranted) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            accessGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        Intent LocationServiceIntent = new Intent(this, LocationService.class);

        startService(LocationServiceIntent);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(LocationServiceIntent);
//        } else {
//            startService(LocationServiceIntent);
//        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(locationReceiver);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        registerReceiver(locationReceiver, new IntentFilter("LOCATION_ACTION"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}