package br.com.jus.peticao.inicial.impl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@FunctionalInterface
public interface SSLContextToRequest {

    SSLContext createInstanceProtocol(String protocol) throws KeyManagementException, NoSuchAlgorithmException;

    SSLContextToRequest instanceOf = protocol -> {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                }
        }, new SecureRandom());
        return sslContext;
    };
}
