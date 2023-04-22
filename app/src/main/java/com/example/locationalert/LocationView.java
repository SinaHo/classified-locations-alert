package com.example.locationalert;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class LocationView {
    public int columnPixel, rowPixel;
    public long color;

    public LocationView(int column, int row, long color) {
        this.columnPixel = column;
        this.rowPixel = row;
        this.color = color;
    }

    public LocationView() {
    }

    public String toString() {
        return String.format("%s:%s:%s", this.columnPixel, this.rowPixel, this.color);
    }

    public Bitmap setLocationMarkerOnImage(Bitmap _image, Context context) {
        int x = this.columnPixel;
        int y = this.rowPixel;
        Bitmap image = _image.copy(Bitmap.Config.ARGB_8888, true);
        int i, j;
        if (x > -1 && y > -1) {
            for (i = Math.max(x - 100, 0); i < Math.min(image.getWidth(), x + 100); i++) {
                for (j = Math.max(y - 100, 0); j < Math.min(image.getHeight(), y + 100); j++) {
                    if (Math.pow(i - x, 2) + Math.pow(j - y, 2) < 10000) {
                        image.setPixel(i, j, ContextCompat.getColor(context, R.color.yellow_600));

                    }
                }
            }
        }
        return image;
    }

    public static LocationView parseView(String stringView) {
        String[] parts = stringView.split(":");
        Log.i("LOCATION", stringView);
        if (parts.length != 3) return new LocationView(-1, -1, -1);
        return new LocationView(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Long.parseLong(parts[2]));
    }
}
