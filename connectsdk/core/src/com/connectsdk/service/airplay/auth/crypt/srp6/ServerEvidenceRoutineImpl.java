package com.connectsdk.service.airplay.auth.crypt.srp6;

import com.nimbusds.srp6.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Martin on 18.05.2017.
 */
class ServerEvidenceRoutineImpl implements ServerEvidenceRoutine {
    private final SRP6ClientSession srp6ClientSession;

    public ServerEvidenceRoutineImpl(SRP6ClientSession srp6ClientSession) {
        this.srp6ClientSession = srp6ClientSession;
    }

    @Override
    public BigInteger computeServerEvidence(SRP6CryptoParams cryptoParams, SRP6ServerEvidenceContext ctx) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(cryptoParams.H);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not locate requested algorithm", e);
        }

        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.A));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.M1));
        digest.update(srp6ClientSession.getSessionKeyHash());

        return new BigInteger(1, digest.digest());
    }
}
