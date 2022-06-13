package com.connectsdk.service.webos;

import android.util.Log;

import com.connectsdk.core.Util;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.Arrays;

public class WebOSTVTrustManager implements X509TrustManager {
    X509Certificate expectedCert;
    X509Certificate lastCheckedCert;

    public void setExpectedCertificate(X509Certificate cert) {
        this.expectedCert = cert;
    }

    public X509Certificate getLastCheckedCertificate () {
        return lastCheckedCert;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        Log.d(Util.T, "Expecting device cert " + (expectedCert != null ? expectedCert.getSubjectDN() : "(any)"));

        if (chain != null && chain.length > 0) {
            X509Certificate cert = chain[0];

            lastCheckedCert = cert;

            if (expectedCert != null) {
                byte [] certBytes = cert.getEncoded();
                byte [] expectedCertBytes = expectedCert.getEncoded();

                Log.d(Util.T, "Device presented cert " + cert.getSubjectDN());

                if (!Arrays.equals(certBytes, expectedCertBytes)) {
                    throw new CertificateException("certificate does not match");
                }
            }
        } else {
            lastCheckedCert = null;
            throw new CertificateException("no server certificate");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}