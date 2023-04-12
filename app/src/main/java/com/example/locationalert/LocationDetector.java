package com.example.locationalert;

import java.io.InputStream;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;


public class LocationDetector {

    public static LocationDetector singletonDetector;
    private Context context;
    private Bitmap image;

    private double upperLeftLongitude, upperLeftLatitude, bottomRightLongitude, bottomRightLatitude;

    private LocationDetector(Context context, double ulLong, double ulLat, double brLong, double brLat) {

        this.context = context;
        image = BitmapFactory.decodeResource(context.getResources(), R.raw.areas);
        upperLeftLongitude = ulLong;
        upperLeftLatitude = ulLat;
        bottomRightLongitude = brLong;
        bottomRightLatitude = brLat;
    }

    public static void InitSingleton(Context context, double ulLong, double ulLat, double brLong, double brLat) {
        if (singletonDetector == null) {
            singletonDetector = new LocationDetector(context, ulLong, ulLat, brLong, brLat);
        }

    }

    private static int calculateFraction(double a, double a1, double a2, int length) {
        return (int) Math.abs(Math.floor((a - a2) / (a2 - a1) * length)) ;
    }

    private long _getPixel(double longitude, double latitude) {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = calculateFraction(longitude, bottomRightLongitude, upperLeftLongitude, width);
        int y = calculateFraction(latitude, bottomRightLatitude, upperLeftLatitude, height);
//        return new int[]{x ,y,image.getPixel(10,10)};
        Log.i("Pixel", String.format("width: %s from %s and height: %s from %s", x,width,y,height));
        int pixel = image.getPixel(x,y);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        if (redValue == 255 && blueValue ==0){
            return Color.RED;
        }else if (
                blueValue == 255 && redValue==0
        ){
            return Color.BLUE;
        }else {
            return Color.WHITE;
        }
//        return image.getPixel(x, y);
    }

    public static long getLocationColor(double longitude, double latitude) {
        Log.d("TEST", Integer.toString(Color.WHITE));
        if (longitude > singletonDetector.bottomRightLongitude ){
            Log.e("Boundary", "s1 - went east");
            return Color.WHITE;
        }if (longitude < singletonDetector.upperLeftLongitude ){
            Log.e("Boundary", "s2 - went west");
            return Color.WHITE;
        }if (latitude< singletonDetector.bottomRightLatitude ){
            Log.e("Boundary", "s3 - went south");
            return Color.WHITE;
        }if (latitude> singletonDetector.upperLeftLatitude){
            Log.e("Boundary", "s4 - went north");
            return Color.WHITE;
        }
        return singletonDetector._getPixel(longitude, latitude);
    }

}
