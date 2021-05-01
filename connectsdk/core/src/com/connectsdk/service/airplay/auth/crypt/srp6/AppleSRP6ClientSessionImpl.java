package com.connectsdk.service.airplay.auth.crypt.srp6;

import com.nimbusds.srp6.BigIntegerUtils;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.XRoutineWithUserIdentity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Created by Martin on 18.05.2017.
 */
public class AppleSRP6ClientSessionImpl extends SRP6ClientSession {

    public AppleSRP6ClientSessionImpl() {
        super();
        setClientEvidenceRoutine(new ClientEvidenceRoutineImpl(this));
        setServerEvidenceRoutine(new ServerEvidenceRoutineImpl(this));
        setXRoutine(new XRoutineWithUserIdentity());
        setHashedKeysRoutine(new HashedKeysRoutineImpl());
    }

    /**
     * Gets the hash 'K' of the shared session key H(S).
     *
     * @return The hash of the shared session key H(S). {@code null}
     * will be returned if authentication failed or the method is
     * invoked in a session state when the session key 'S' has not
     * been computed yet.
     */
    public byte[] getSessionKeyHash() {

        if (S == null)
            return null;

        MessageDigest digest = config.getMessageDigestInstance();

        if (digest == null)
            throw new IllegalArgumentException("Unsupported hash algorithm 'H': " + config.H);

        digest.update(BigIntegerUtils.bigIntegerToBytes(S));
        digest.update(new byte[]{0, 0, 0, 0});
        byte[] K1 = digest.digest();

        digest.update(BigIntegerUtils.bigIntegerToBytes(S));
        digest.update(new byte[]{0, 0, 0, 1});
        byte[] K2 = digest.digest();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(K1);
            outputStream.write(K2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
