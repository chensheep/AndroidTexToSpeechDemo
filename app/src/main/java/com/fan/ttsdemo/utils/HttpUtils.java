package com.fan.ttsdemo.utils;

import android.accounts.NetworkErrorException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtils {

    private final static String TAG = "HttpUtils";

    private static final boolean DBG = true;

    private static Map<String, String> allowedHostMap = Collections.unmodifiableMap(new HashMap<String, String>(10, 1.0f) {
        {
            put("iot.cht.com.tw", "legal");
        }
    });

    private static class EmptyX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private static class PrivateHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            String res = allowedHostMap.get(hostname);
            if (res != null && res.compareTo("legal") == 0) {
                return true;
            }
            return false;
        }
    }

    public static String post(String url, String body) {
        if (DBG) {Log.d(TAG, "post " + url + "body " + body);}
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

    public static String postWithSkipHttpsCertAndCheckHostname(String url, String body) {
        if (DBG) {Log.d(TAG, "post " + url + "body " + body);}
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

            if (conn instanceof HttpsURLConnection) {
                Log.d(TAG, "https!");
                SSLContext sc = SSLContext.getInstance("TLS");
                TrustManager[] tm = new TrustManager[]{new EmptyX509TrustManager()};
                sc.init(null, tm, new SecureRandom());
                if (sc != null) {
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                    ((HttpsURLConnection) conn).setHostnameVerifier(new PrivateHostnameVerifier());
                }
            }

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

            if (conn instanceof HttpsURLConnection) {
                Log.d(TAG, "https!");
                SSLContext sc = SSLContext.getInstance("TLS");
                TrustManager[] tm = new TrustManager[]{new EmptyX509TrustManager()};
                sc.init(null, tm, new SecureRandom());
                if (sc != null) {
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                    ((HttpsURLConnection) conn).setHostnameVerifier(new PrivateHostnameVerifier());
                }
            }

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
