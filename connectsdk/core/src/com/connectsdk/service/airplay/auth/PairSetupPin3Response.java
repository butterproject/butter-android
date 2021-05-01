package com.connectsdk.service.airplay.auth;

/**
 * Created by Martin on 19.05.2017.
 */
class PairSetupPin3Response {
    public final byte[] EPK;
    public final byte[] AUTH_TAG;

    public PairSetupPin3Response(byte[] epk, byte[] authTag) {
        this.EPK = epk;
        this.AUTH_TAG = authTag;
    }
}
