package com.fan.ttsdemo.utils;

import android.accounts.NetworkErrorException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    private final static String TAG = "HttpUtils";

    public static String post(String url, String body) {
            HttpURLConnection conn = null;
        try {
            byte[] postData = body.getBytes("UTF-8");
            int postDataLength = postData.length;
            Log.d(TAG, "POST " + url);
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write( postData );

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                Log.d(TAG, "Reponse code is 200, read inputstream ...");
                InputStream is = conn.getInputStream();
                String response = getStringFromInputStream(is);
                return response;
            } else {
                Log.d(TAG, "response status is " + responseCode);
                throw new NetworkErrorException("response status is "+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    public static String get(String url) {
        HttpURLConnection conn = null;
        try {
            Log.d(TAG, "GET " + url);
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                Log.d(TAG, "Reponse code is 200, read inputstream ...");
                InputStream is = conn.getInputStream();
                String response = getStringFromInputStream(is);
                return response;
            } else {
                Log.d(TAG, "response status is " + responseCode);
                throw new NetworkErrorException("response status is "+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    private static String getStringFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            Log.d(TAG, "length = " + len);
            os.write(buffer, 0, len);
        }
        is.close();
        Log.d(TAG, "os = " + os);
        String state = os.toString();
        os.close();
        return state;
    }
}
