package com.connectsdk.service.airplay.auth.crypt.srp6;

import com.nimbusds.srp6.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ClientEvidenceRoutineImpl implements ClientEvidenceRoutine {

    private final SRP6ClientSession srp6ClientSession;

    public ClientEvidenceRoutineImpl(SRP6ClientSession srp6ClientSession) {
        this.srp6ClientSession = srp6ClientSession;
    }

    /**
     * Calculates M1 according to the following formula:
     * <p>
     * M1 = H(H(N) xor H(g) || H(username) || s || A || B || K)
     */
    public BigInteger computeClientEvidence(SRP6CryptoParams cryptoParams,
                                            SRP6ClientEvidenceContext ctx) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(cryptoParams.H);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not locate requested algorithm", e);
        }

        digest.update(BigIntegerUtils.bigIntegerToBytes(cryptoParams.N));
        byte[] hN = digest.digest();

        digest.update(BigIntegerUtils.bigIntegerToBytes(cryptoParams.g));
        byte[] hg = digest.digest();

        byte[] hNhg = xor(hN, hg);

        digest.update(ctx.userID.getBytes(StandardCharsets.UTF_8));
        byte[] hu = digest.digest();

        digest.update(hNhg);
        digest.update(hu);
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.s));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.A));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.B));
        digest.update(srp6ClientSession.getSessionKeyHash());

        return new BigInteger(1, digest.digest());
    }

    private static byte[] xor(byte[] b1, byte[] b2) {
        byte[] result = new byte[b1.length];
        for (int i = 0; i < b1.length; i++) {
            result[i] = (byte) (b1[i] ^ b2[i]);
        }
        return result;
    }

}