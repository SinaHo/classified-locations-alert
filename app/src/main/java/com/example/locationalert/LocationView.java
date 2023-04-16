package com.example.locationalert;

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

    public static LocationView parseView(String stringView) {
        String[] parts = stringView.split(":");
        if (parts.length != 3) return new LocationView(-1, -1, -1);
        return new LocationView(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Long.parseLong(parts[2]));
    }
}
