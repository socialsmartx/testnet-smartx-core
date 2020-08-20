package com.smartx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;

/**
 @author Tenpay
 @version 1.0
 @date 2010-7-5
 @since jdk1.5 */
public class HttpClientUtil {
    /**
     @param allUrl
     @param connectTimeout
     @param charset
     @return
     @throws KeyManagementException
     @throws NoSuchAlgorithmException
     @throws NoSuchProviderException
     @throws IOException               */
    public static String httpClientCall(String allUrl, int connectTimeout, String charset) {
        try {
            String ret = null;
            String readTimeout_s = "10000";//StringUtils.trimToNull(ReloadableAppConfig.appConfig.get("notify_readtimeout"));
            int readTimeout = 8000;    // 超时时间2s
            if (readTimeout_s != null) {
                readTimeout = Integer.valueOf(readTimeout_s);
            }
            ret = httpClientCall(allUrl, connectTimeout, readTimeout, charset);
            return ret;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
        }
        return null;
    }

    public static String httpClientCallException(String allUrl, int connectTimeout, String charset) throws Exception
    {
            String ret = null;
            String readTimeout_s = "10000";//StringUtils.trimToNull(ReloadableAppConfig.appConfig.get("notify_readtimeout"));
            int readTimeout = 8000;    // 超时时间2s
            if (readTimeout_s != null) {
                readTimeout = Integer.valueOf(readTimeout_s);
            }
            ret = httpClientCall(allUrl, connectTimeout, readTimeout, charset);
            return ret;
    }

    private static String httpClientPostCall(String allUrl, int connectTimeout, int readTimeout, String charset) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        URL url = new URL(allUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSimpleSSLSocketFactory());
        }
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.getResponseCode();
        return readContents(connection, charset);
    }
    public static String httpClientPost(String allUrl, int connectTimeout, String charset) {
        try {
            String ret = null;
            String readTimeout_s = "10000";//StringUtils.trimToNull(ReloadableAppConfig.appConfig.get("notify_readtimeout"));
            int readTimeout = 8000;    // 超时时间2s
            if (readTimeout_s != null) {
                readTimeout = Integer.valueOf(readTimeout_s);
            }
            ret = httpClientPostCall(allUrl, connectTimeout, readTimeout, charset);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     @param allUrl
     @param connectTimeout
     @param charset
     @return
     @throws KeyManagementException
     @throws NoSuchAlgorithmException
     @throws NoSuchProviderException
     @throws IOException               */
    public static String httpClientCall(String allUrl, int connectTimeout, String charset, AtomicInteger httpResponseCode) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        String ret = null;
        String readTimeout_s = "10000";//StringUtils.trimToNull(ReloadableAppConfig.appConfig.get("notify_readtimeout"));
        int readTimeout = 3000;
        if (readTimeout_s != null) {
            readTimeout = Integer.valueOf(readTimeout_s);
        }
        ret = httpClientCall(allUrl, connectTimeout, readTimeout, charset, httpResponseCode);
        return ret;
    }
    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        AtomicInteger httpResponseCode = new AtomicInteger();
        try {
            System.out.println(isSuccess(httpClientCall("http://cb.qq.com/cashpay/pay_notify.php?attach=sAttach%253D100.1214.01.000.00%2526iUin%253D749265965&bank_type=BL&discount=0&fee_type=1&input_charset=UTF-8&notify_id=4dOIpW9hS9YipTYs51YKw40XUqfyuFXpIwkrP939r8pIKgHPLzmSj1Ytafem1fNKc7PdK84eOJdjesS_yKBEhmYkNjwDRhgz&out_trade_no=20121120160000250749265965OtftAc&partner=1000014501&product_fee=1&sign_type=MD5&time_end=20121120160123&total_fee=1&trade_mode=1&trade_state=0&transaction_id=1000014501201211200188428751&transport_fee=0&sign=8EAD5DA6478F6DA4E5C71484D51AD8C4", 5000, 5000, "GBK", httpResponseCode)));
        } catch (Exception e) {
            // TODO: handle exception
        }
        System.out.println(httpResponseCode);
    }
    protected static boolean isSuccess(String merChantRet) {
        return merChantRet != null && merChantRet.toLowerCase().startsWith("success");
    }
    /**
     @param allUrl
     @param connectTimeout
     @param readTimeout
     @param charset
     @return
     @throws KeyManagementException
     @throws NoSuchAlgorithmException
     @throws NoSuchProviderException
     @throws IOException               */
    private static String httpClientCall(String allUrl, int connectTimeout, int readTimeout, String charset) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        URL url = new URL(allUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSimpleSSLSocketFactory());
        }
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.getResponseCode();
        return readContents(connection, charset);
    }
    /**
     @param allUrl
     @param connectTimeout
     @param readTimeout
     @param charset
     @return
     @throws KeyManagementException
     @throws NoSuchAlgorithmException
     @throws NoSuchProviderException
     @throws IOException               */
    private static String httpClientCall(String allUrl, int connectTimeout, int readTimeout, String charset, AtomicInteger httpResponseCode) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        URL url = new URL(allUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSimpleSSLSocketFactory());
        }
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        return readContents(connection, charset, httpResponseCode);
    }
    /**
     @param connection
     @param charset
     @return
     @throws IOException
     */
    private static String readContents(HttpURLConnection connection, String charset) throws IOException {
        StringBuffer buff = new StringBuffer(100);
        InputStream is = null;
        BufferedReader in = null;
        try {
            connection.connect();
            is = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(is, charset));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                buff.append(inputLine + "\n");
            }
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                connection.disconnect();
            } catch (Exception e) {
            }
        }
        //return StringUtils.trimToNull(buff.toString());
        return StringUtils.trim(buff.toString());
    }
    /**
     @param connection
     @param charset
     @return
     @throws IOException
     */
    private static String readContents(HttpURLConnection connection, String charset, AtomicInteger httpResponseCode) throws IOException {
        StringBuffer buff = new StringBuffer(100);
        InputStream is = null;
        BufferedReader in = null;
        try {
            connection.connect();
            if (httpResponseCode != null) {
                httpResponseCode.set(connection.getResponseCode());
            }
            is = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(is, charset));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                buff.append(inputLine + "\n");
            }
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                connection.disconnect();
            } catch (Exception e) {
            }
        }
        //return StringUtils.trimToNull(buff.toString());
        return StringUtils.trim(buff.toString());
    }
    /**
     @return
     @throws NoSuchAlgorithmException
     @throws NoSuchProviderException
     @throws KeyManagementException
     */
    private static SSLSocketFactory getSimpleSSLSocketFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        TrustManager[] tm = {new TrustAnyTrustManager()};
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }
}
class TrustAnyTrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}
