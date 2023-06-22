package com.example.locationalert;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

public class LocationDetector {

    public static LocationDetector singletonDetector;
    private Context context;
    private Bitmap image;

    public HashMap<Long, String> colorDict = new HashMap<Long, String>();

    private double upperLeftLongitude, upperLeftLatitude, bottomRightLongitude, bottomRightLatitude;

    private LocationDetector(Context context) {

        this.context = context;
        image = BitmapFactory.decodeResource(context.getResources(), R.drawable.ahar);
    }

    private void parseGeolocationXML(Context context) {
        XmlResourceParser parser = context.getResources().getXml(R.xml.imagegeolocation);
        int eventType;
        String attrName = "";
        String attrValue = "";
        do {
            try {
                eventType = parser.getEventType();
            } catch (XmlPullParserException exp) {
                Log.e("XML", exp.toString());
                break;
            }
            if (eventType == XmlResourceParser.START_TAG) {
                if (parser.getName().equals("attr")) {
                    attrName = parser.getAttributeValue(null, "name");
                }
            } else if (eventType == XmlResourceParser.TEXT) {
                String text = parser.getText().trim();
                if (!text.isEmpty()) {
                    attrValue = text;
                }
            } else if (eventType == XmlResourceParser.END_TAG) {
                if (parser.getName().equals("attr")) {
                    switch (attrName) {
                        case "upperLeftLatitude":
                            upperLeftLatitude = Double.parseDouble(attrValue);
                            break;
                        case "upperLeftLongitude":
                            upperLeftLongitude = Double.parseDouble(attrValue);
                            break;
                        case "bottomRightLatitude":
                            bottomRightLatitude = Double.parseDouble(attrValue);
                            break;
                        case "bottomRightLongitude":
                            bottomRightLongitude = Double.parseDouble(attrValue);
                            break;

                    }
                }
            }
            try {
                eventType = parser.next();
            } catch (IOException exp) {
                Log.e("XML", exp.toString());
                break;
            } catch (XmlPullParserException exp) {
                Log.e("XML", exp.toString());
                break;
            }

        } while (eventType != XmlResourceParser.END_DOCUMENT);

    }

    private void parseColorsDict(Context context) {
        XmlResourceParser parser = context.getResources().getXml(R.xml.colordict);
        int eventType;
        String attrName = "";
        String attrValue = "";
        do {
            try {
                eventType = parser.getEventType();
            } catch (XmlPullParserException exp) {
                Log.e("XML", exp.toString());
                break;
            }
            if (eventType == XmlResourceParser.START_TAG) {
                if (parser.getName().equals("item")) {
                    attrName = parser.getAttributeValue(null, "color");
                }
            } else if (eventType == XmlResourceParser.TEXT) {
                String text = parser.getText().trim();
                if (!text.isEmpty()) {
                    attrValue = text;
                }
            } else if (eventType == XmlResourceParser.END_TAG) {
                if (parser.getName().equals("item")) {
                    colorDict.put((long) Color.parseColor(attrName), attrValue);
                }
            }
            try {
                eventType = parser.next();
            } catch (IOException exp) {
                break;
            } catch (XmlPullParserException exp) {
                Log.e("XML", exp.toString());
                break;
            }

        } while (eventType != XmlResourceParser.END_DOCUMENT);
    }

    public static void InitSingleton(Context context) {
        if (singletonDetector == null) {
            singletonDetector = new LocationDetector(context);
            singletonDetector.parseGeolocationXML(context);
            singletonDetector.parseColorsDict(context);
        }

    }

    private static int calculateFraction(double a, double a1, double a2, int length) {
        return (int) Math.abs(Math.floor((a - a2) / (a2 - a1) * length));
    }

    private LocationView _getPixel(double longitude, double latitude) {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = calculateFraction(longitude, bottomRightLongitude, upperLeftLongitude, width);
        int y = calculateFraction(latitude, bottomRightLatitude, upperLeftLatitude, height);
        int pixel = image.getPixel(x, y);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        LocationView view = new LocationView();
        view.rowPixel = y;
        view.columnPixel = x;
        view.color = pixel;
//        if (redValue == 255 && blueValue == 0) {
//            view.color = Color.RED;
//        } else if (
//                blueValue == 255 && redValue == 0
//        ) {
//            view.color = Color.BLUE;
//        } else {
//            view.color = Color.WHITE;
//        }
        return view;
    }

    private static boolean checkBoundary(double longitude, double latitude) {
        if (longitude > singletonDetector.bottomRightLongitude) {
            Log.e("Boundary", "s1 - went east");
            return false;
        }
        if (longitude < singletonDetector.upperLeftLongitude) {
            Log.e("Boundary", "s2 - went west");
            return false;
        }
        if (latitude < singletonDetector.bottomRightLatitude) {
            Log.e("Boundary", "s3 - went south");
            return false;
        }
        if (latitude > singletonDetector.upperLeftLatitude) {
            Log.e("Boundary", "s4 - went north");
            return false;
        }
        return true;
    }


    public static long getLocationColor(double longitude, double latitude) {
        if (!checkBoundary(longitude, latitude)) return Color.WHITE;
        return singletonDetector._getPixel(longitude, latitude).color;
    }

    public static LocationView getLocationView(double longitude, double latitude) {
        if (!checkBoundary(longitude, latitude)) {
            return new LocationView(-1, -1, Color.WHITE);
        }
        return singletonDetector._getPixel(longitude, latitude);
    }


}
