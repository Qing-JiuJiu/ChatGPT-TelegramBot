package com.xinqi.util;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * HTTPS请求工具类
 *
 * @author xiaoqqya
 */
public class HttpsClientUtil {

    public static byte[] httpsPost(String urlStr, String data) throws Exception {
        InputStream input;
        try {
            URL url = new URL(urlStr);
            if (urlStr.startsWith("https")) {
                HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) url.openConnection();
                HostnameVerifier ignoreHostnameVerifier = new MyHostnameVerifier();
                SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
                sslContext.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
                httpsUrlConnection.setRequestMethod("POST");
                httpsUrlConnection.setRequestProperty("Content-Type", "application/json");
                httpsUrlConnection.setDoOutput(true);
                DataOutputStream dataOutputStream = new DataOutputStream(httpsUrlConnection.getOutputStream());
                dataOutputStream.writeBytes(data);
                dataOutputStream.flush();
                dataOutputStream.close();
                httpsUrlConnection.setConnectTimeout(10000);
                httpsUrlConnection.setReadTimeout(20000);
                httpsUrlConnection.setHostnameVerifier(ignoreHostnameVerifier);
                httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsUrlConnection.connect();
                input = httpsUrlConnection.getInputStream();
            } else {
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setDoOutput(true);
                DataOutputStream dataOutputStream = new DataOutputStream(httpUrlConnection.getOutputStream());
                dataOutputStream.writeBytes(data);
                dataOutputStream.flush();
                dataOutputStream.close();
                httpUrlConnection.setConnectTimeout(10000);
                httpUrlConnection.setReadTimeout(20000);
                httpUrlConnection.connect();
                input = httpUrlConnection.getInputStream();
            }
            return toByteArray(input);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static byte[] httpsPostChatGpt(String urlStr, String data, String apiKey) throws Exception {
        InputStream input;
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            HostnameVerifier ignoreHostnameVerifier = new MyHostnameVerifier();
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
            httpsUrlConnection.setRequestMethod("POST");
            httpsUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpsUrlConnection.addRequestProperty("Authorization", "Bearer " + apiKey);
            httpsUrlConnection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(httpsUrlConnection.getOutputStream());
            dataOutputStream.writeBytes(data);
            dataOutputStream.flush();
            dataOutputStream.close();
            httpsUrlConnection.setConnectTimeout(10000);
            httpsUrlConnection.setReadTimeout(20000);
            httpsUrlConnection.setHostnameVerifier(ignoreHostnameVerifier);
            httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            httpsUrlConnection.connect();
            input = httpsUrlConnection.getInputStream();
            return toByteArray(input);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 将输入流转换成字节数组
     *
     * @param input 输入流对象
     * @return 字节数组
     * @throws IOException IO异常
     * @author xiaoqqya
     */
    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}

/**
 * 实现X509TrustManager接口，信任所有
 *
 * @author xiaoqqya
 * @version 2021/03/03
 */
class MyX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

/**
 * 实现HostnameVerifier接口，忽略HTTPS主机验证
 *
 * @author xiaoqqya
 * @version 2021/03/03
 */
class MyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
