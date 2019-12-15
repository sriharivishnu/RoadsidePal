//package com.magnitudestudios.sriharivishnu.roadsidepal;
//
//import android.os.StrictMode;
//import android.util.Log;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//
//public class Reader {
//    private static String TAG = "Reader";
//    private static String API_KEY = "0d220e1e8b4f49a89b827355c614766d";
//
//    public static ArrayList<Place> search(double lat, double lng, int radius) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//        ArrayList<Place> resultList = null;
//
//        HttpURLConnection conn = null;
//        StringBuilder jsonResults = new StringBuilder();
//        try {
//            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place");
//            sb.append("/nearbysearch");
//            sb.append("/json?");
//            sb.append("location=" + String.valueOf(lat) + "," + String.valueOf(lng));
//            sb.append("&radius=" + String.valueOf(radius));
//            sb.append("&type=restaurant");
//            sb.append("&key=" + API_KEY);
//
//            URL url = new URL(sb.toString());
//            conn = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//
//            int read;
//            char[] buff = new char[1024];
//            while ((read = in.read(buff)) != -1) {
//                jsonResults.append(buff, 0, read);
//            }
//        } catch (MalformedURLException e) {
//            Log.e(TAG, "Error processing Places API URL", e);
//            return resultList;
//        } catch (IOException e) {
//            Log.e(TAG, "Error connecting to Places API", e);
//            return resultList;
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//
//        try {
//            // Create a JSON object hierarchy from the results
//            JSONObject jsonObj = new JSONObject(jsonResults.toString());
//            JSONArray predsJsonArray = jsonObj.getJSONArray("results");
//
//            // Extract the descriptions from the results
//            resultList = new ArrayList<Place>(predsJsonArray.length());
//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                Place place = new Place();
//                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
//                place.name = predsJsonArray.getJSONObject(i).getString("name");
//                resultList.add(place);
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "Error processing JSON results", e);
//        }
////
//        return resultList;
//    }
//}
