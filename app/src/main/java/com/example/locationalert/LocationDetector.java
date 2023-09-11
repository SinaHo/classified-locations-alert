package com.example.locationalert;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LocationDetector {

    public static LocationDetector singletonDetector;
    private static int k;
    private static int maxK;
    private static boolean found;
    private final Context context;
    private final Bitmap image;
    public HashMap<Long, String> colorDict = new HashMap<Long, String>();
    private double upperLeftLongitude, upperLeftLatitude, bottomRightLongitude, bottomRightLatitude;

    private LocationDetector(Context context) {

        this.context = context;
        image = BitmapFactory.decodeResource(context.getResources(), R.drawable.output);
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

    private static int mostRepeated(int[] arr) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // Iterate over the array and update the frequency count
        for (int num : arr) {
            frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
        }

        int mostRepeatedElement = 0;
        int maxFrequency = 0;

        // Find the element with the maximum frequency
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            int num = entry.getKey();
            int frequency = entry.getValue();

            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                mostRepeatedElement = num;
            }
        }

        return mostRepeatedElement;
    }

    private static double colorDistance(Color color1, Color color2) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Math.sqrt(Math.pow(color1.blue() - color2.blue(), 2) + Math.pow(color1.green() - color2.green(), 2) + Math.pow(color1.red() - color2.red(), 2));
        }
//        return Math.sqrt(Math.pow(color1))
        return 0.0;
    }
    private static double colorDistance(Long color1, Long color2) {
        int red1 = (int) ((color1 >> 16) & 0xFF);
        int green1 = (int) ((color1 >> 8) & 0xFF);
        int blue1 = (int) (color1 & 0xFF);
        int red2 = (int) ((color2 >> 16) & 0xFF);
        int green2 = (int) ((color2 >> 8) & 0xFF);
        int blue2 = (int) (color2 & 0xFF);
        return Math.sqrt(Math.pow(blue1-blue2, 2) + Math.pow(red1-red2, 2) + Math.pow(green1-green2, 2));
    }

    private static boolean colorMatch(Color color1, Long color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return colorMatch(color1, Color.valueOf(color2));
        }
        return false;
    }

    private static boolean colorMatch(Long color2, Color color1) {
        return colorMatch(color1, color2);
    }

    private static boolean colorMatch(Color color1, Color color2) {
        return colorDistance(color1, color2) <= 13;
    }

    private static void handleColor(Color color, HashMap<Long, String> colorDict, ArrayList<Color> validColors) {
        for (long definedColor : colorDict.keySet()) {
            if (colorMatch(definedColor, color)) {
                if (!found) {
                    found = true;
                    maxK = k + 5;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    validColors.add(Color.valueOf(definedColor));
                }
            }
        }
    }

    private static int findNearestDefinedPixel(Bitmap image, int x, int y, HashMap<Long, String> colorDict) {
        long current = (long) image.getPixel(x, y);
        double minDistance = 450;
        long color = 0;
        double d;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (long definedColor : colorDict.keySet()) {
                d = colorDistance((definedColor), (current));
                Log.d("d", String.valueOf(d));
                if (d < minDistance) {
                    color = definedColor;
                    minDistance = d;
                }
            }
        }
        return (int) color;


        ////////////////////////////////////////
//        int height = image.getHeight();
//        int width = image.getWidth();
//        found = false;
//        maxK = 110;
//        Color tempColor;
//        ArrayList<Color> validColors = new ArrayList<>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            for (k = 0; k < maxK; k++) {
//                for (int i = x - (k - 1); i <= x + (k - 1); i++) {
//                    handleColor(Color.valueOf(image.getPixel(i, y - k)), colorDict, validColors);
//                    handleColor(Color.valueOf(image.getPixel(i, y + k)), colorDict, validColors);
//                }
//                for (int j = y - (k - 1); j <= y + (k - 1); j++) {
//                    handleColor(Color.valueOf(image.getPixel(x - k, j)), colorDict, validColors);
//                    handleColor(Color.valueOf(image.getPixel(x + k, j)), colorDict, validColors);
//                }
//                handleColor(Color.valueOf(image.getPixel(x + k, y + k)), colorDict, validColors);
//                handleColor(Color.valueOf(image.getPixel(x - k, y - k)), colorDict, validColors);
//                handleColor(Color.valueOf(image.getPixel(x + k, y - k)), colorDict, validColors);
//                handleColor(Color.valueOf(image.getPixel(x - k, y + k)), colorDict, validColors);
//            }
//        }
//        if (validColors.size() == 0) {
//            return 0;
//        }
//        Integer[] items = new Integer[validColors.size()];
//        items = validColors.toArray(items);
//
//        return mostRepeated(Arrays.stream(validColors.toArray(items)).mapToInt(Integer::intValue).toArray());
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

    private LocationView _getPixel(double longitude, double latitude) {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = calculateFraction(longitude, bottomRightLongitude, upperLeftLongitude, width);
        int y = calculateFraction(latitude, bottomRightLatitude, upperLeftLatitude, height);
        int pixel = image.getPixel(x, y);
        int[] sorroundings = new int[26 * 26];
        for (int i = -13; i < +13; i++) {
            for (int j = -13; j < +13; j++) {
                sorroundings[(i + 13) * 26 + j + 13] = image.getPixel(x + i, y + j);
            }
        }
//        int repeatedPixel = mostRepeated(sorroundings);
        int meanPixel = findNearestDefinedPixel(image, x, y, colorDict);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        LocationView view = new LocationView();
        view.rowPixel = y;
        view.columnPixel = x;
        view.color = meanPixel;
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


}
