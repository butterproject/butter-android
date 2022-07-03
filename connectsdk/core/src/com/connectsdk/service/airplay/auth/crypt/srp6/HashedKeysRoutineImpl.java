package com.connectsdk.service.airplay.auth.crypt.srp6;

import com.nimbusds.srp6.BigIntegerUtils;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.URoutine;
import com.nimbusds.srp6.URoutineContext;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Martin on 17.05.2017.
 */
class HashedKeysRoutineImpl implements URoutine {
    @Override
    public BigInteger computeU(SRP6CryptoParams cryptoParams, URoutineContext ctx) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(cryptoParams.H);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not locate requested algorithm", e);
        }
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.A));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.B));

        return BigIntegerUtils.bigIntegerFromBytes(digest.digest());
    }
}
