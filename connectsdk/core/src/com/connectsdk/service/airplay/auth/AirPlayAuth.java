package com.connectsdk.service.airplay.auth;

import android.util.Log;

import com.connectsdk.service.airplay.auth.crypt.Curve25519;
import com.connectsdk.service.airplay.auth.crypt.srp6.AppleSRP6ClientSessionImpl;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.nimbusds.srp6.BigIntegerUtils;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Martin on 08.05.2017.
 */
public class AirPlayAuth {

    private static final int SOCKET_TIMEOUT = 60 * 1000;

    private final InetSocketAddress address;
    private final String clientId;
    private final EdDSAPrivateKey authKey;

    /**
     * Create a new instance of AirPlayAuth, to be used to pair/authenticate with an AppleTV for AirPlay
     *
     * @param address   The address of the AppleTV that you retrieve via mdns, eg. 192.168.1.142:7000
     * @param authToken An AuthToken which must be generated via {@code AirPlayAuth.generateNewAuthToken()}.
     * @throws InvalidKeySpecException
     */
    public AirPlayAuth(InetSocketAddress address, String authToken) {
        try {
            this.address = address;

            String[] authTokenSplit = authToken.split("@");
            this.clientId = authTokenSplit[0];

            PKCS8EncodedKeySpec encoded = new PKCS8EncodedKeySpec(net.i2p.crypto.eddsa.Utils.hexToBytes(authTokenSplit[1]));
            this.authKey = new EdDSAPrivateKey(encoded);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use this method once to generated an "authToken" which is the
     * combination of a random "user" and the hash of an EdDSA-key.
     * Persist the authToken somewhere, either in a static final, inside a property etc.
     *
     * @return An authToken to be used with the constructor of AirPlayAuth.
     */
    public static String generateNewAuthToken() {
        String clientId = AuthUtils.randomString(16);

        return clientId + "@" + net.i2p.crypto.eddsa.Utils.bytesToHex(new KeyPairGenerator().generateKeyPair().getPrivate().getEncoded());
    }

    /**
     * Step 1/3: If the client with this {@code authToken} has never paired in the past,
     * or if {@code AirPlayAuth.authenticate()} fails, call this method to initiate the pairing-process.
     * <p>
     * The AppleTV will display its PIN-dialog afterwards.
     *
     * @throws IOException
     */
    public void startPairing() throws IOException {
        Socket socket = connect();
        AuthUtils.postData(socket, "/pair-pin-start", null, null);
        socket.close();
    }

    private Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        socket.connect(address, SOCKET_TIMEOUT);
        return socket;
    }

    /**
     * Step 2/3: Call this method after the pairing-process has been initiated via {@code AirPlayAuth.startPairing()}.
     *
     * @param pin The PIN displayed on the AppleTV.
     * @throws Exception If there was any exception, eg. if a wrong PIN has been provided. Simply call this method with the new PIN.
     */
    public void doPairing(String pin) throws Exception {

        Socket socket = connect();

        PairSetupPin1Response pairSetupPin1Response = doPairSetupPin1(socket);

        final SRP6ClientSession srp6ClientSession = new AppleSRP6ClientSessionImpl();
        srp6ClientSession.step1(clientId, pin);
        srp6ClientSession.step2(SRP6CryptoParams.getInstance(2048, "SHA-1"), BigIntegerUtils.bigIntegerFromBytes(pairSetupPin1Response.SALT), BigIntegerUtils.bigIntegerFromBytes(pairSetupPin1Response.PK));

        PairSetupPin2Response pairSetupPin2Response = doPairSetupPin2(socket, BigIntegerUtils.bigIntegerToBytes(srp6ClientSession.getPublicClientValue()), BigIntegerUtils.bigIntegerToBytes(srp6ClientSession.getClientEvidenceMessage()));

        srp6ClientSession.step3(BigIntegerUtils.bigIntegerFromBytes(pairSetupPin2Response.PROOF));

        PairSetupPin3Response pairSetupPin3Response = doPairSetupPin3(socket, srp6ClientSession.getSessionKeyHash());

        socket.close();

    }

    /**
     * Step 3/3 and later: This method returns an authenticated connection. Use {@code startPairing()} and {@code doPairing(String)}
     * in case this this method fails with an exception, and then call this method again.
     *
     * @return An authenticated connection which can be used to send commands to the AppleTV
     * @throws Exception If there way any exception, eg. if the client hasn't been paired before.
     */
    public Socket authenticate() throws Exception {
        Socket socket = connect();

        byte[] randomPrivateKey = new byte[32];
        new Random().nextBytes(randomPrivateKey);
        byte[] randomPublicKey = new byte[32];
        Curve25519.keygen(randomPublicKey, null, randomPrivateKey);

        byte[] pairVerify1Response = doPairVerify1(socket, randomPublicKey);
        doPairVerify2(socket, pairVerify1Response, randomPrivateKey, randomPublicKey);
        Log.i("LSH","Pair Verify finished!");
        return socket;
    }

    private PairSetupPin1Response doPairSetupPin1(Socket socket) throws Exception {
        byte[] pairSetupPinRequestData = AuthUtils.createPList(new HashMap<String, String>() {{
            put("method", "pin");
            put("user", clientId);
        }});

        byte[] pairSetupPin1ResponseBytes = AuthUtils.postData(socket, "/pair-setup-pin", "application/x-apple-binary-plist", pairSetupPinRequestData);

        NSDictionary pairSetupPin1Response = (NSDictionary) PropertyListParser.parse(pairSetupPin1ResponseBytes);
        if (pairSetupPin1Response.containsKey("pk") && pairSetupPin1Response.containsKey("salt")) {
            byte[] pk = ((NSData) pairSetupPin1Response.get("pk")).bytes();
            byte[] salt = ((NSData) pairSetupPin1Response.get("salt")).bytes();
            return new PairSetupPin1Response(pk, salt);
        }
        throw new Exception();
    }

    private PairSetupPin2Response doPairSetupPin2(Socket socket, final byte[] publicClientValueA, final byte[] clientEvidenceMessageM1) throws Exception {
        byte[] pairSetupPinRequestData = AuthUtils.createPList(new HashMap<String, byte[]>() {{
            put("pk", publicClientValueA);
            put("proof", clientEvidenceMessageM1);
        }});

        byte[] pairSetupPin2ResponseBytes = AuthUtils.postData(socket, "/pair-setup-pin", "application/x-apple-binary-plist", pairSetupPinRequestData);

        NSDictionary pairSetupPin2Response = (NSDictionary) PropertyListParser.parse(pairSetupPin2ResponseBytes);
        if (pairSetupPin2Response.containsKey("proof")) {
            byte[] proof = ((NSData) pairSetupPin2Response.get("proof")).bytes();
            return new PairSetupPin2Response(proof);
        }
        throw new Exception();
    }

    private PairSetupPin3Response doPairSetupPin3(Socket socket, final byte[] sessionKeyHashK) throws Exception {

        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
        sha512Digest.update("Pair-Setup-AES-Key".getBytes(StandardCharsets.UTF_8));
        sha512Digest.update(sessionKeyHashK);
        byte[] aesKey = Arrays.copyOfRange(sha512Digest.digest(), 0, 16);

        sha512Digest.update("Pair-Setup-AES-IV".getBytes(StandardCharsets.UTF_8));
        sha512Digest.update(sessionKeyHashK);
        byte[] aesIV = Arrays.copyOfRange(sha512Digest.digest(), 0, 16);

        int lengthB;
        int lengthA = lengthB = aesIV.length - 1;
        for (; lengthB >= 0 && 256 == ++aesIV[lengthA]; lengthA = lengthB += -1) ;

        Cipher aesGcm128Encrypt = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        aesGcm128Encrypt.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, aesIV));
        final byte[] aesGcm128ClientLTPK = aesGcm128Encrypt.doFinal(authKey.getAbyte());

        byte[] pairSetupPinRequestData = AuthUtils.createPList(new HashMap<String, byte[]>() {{
            put("epk", Arrays.copyOfRange(aesGcm128ClientLTPK, 0, aesGcm128ClientLTPK.length - 16));
            put("authTag", Arrays.copyOfRange(aesGcm128ClientLTPK, aesGcm128ClientLTPK.length - 16, aesGcm128ClientLTPK.length));
        }});

        byte[] pairSetupPin3ResponseBytes = AuthUtils.postData(socket, "/pair-setup-pin", "application/x-apple-binary-plist", pairSetupPinRequestData);

        NSDictionary pairSetupPin3Response = (NSDictionary) PropertyListParser.parse(pairSetupPin3ResponseBytes);

        if (pairSetupPin3Response.containsKey("epk") && pairSetupPin3Response.containsKey("authTag")) {
            byte[] epk = ((NSData) pairSetupPin3Response.get("epk")).bytes();
            byte[] authTag = ((NSData) pairSetupPin3Response.get("authTag")).bytes();

            return new PairSetupPin3Response(epk, authTag);
        }
        throw new Exception();
    }

    private byte[] doPairVerify1(Socket socket, byte[] randomPublicKey) throws Exception {
        return AuthUtils.postData(socket, "/pair-verify", "application/octet-stream", AuthUtils.concatByteArrays(new byte[]{1, 0, 0, 0}, randomPublicKey, authKey.getAbyte()));
    }

    private void doPairVerify2(Socket socket, byte[] pairVerify1Response, byte[] randomPrivateKey, byte[] randomPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException, SignatureException {
        byte[] atvPublicKey = Arrays.copyOfRange(pairVerify1Response, 0, 32);
        byte[] sharedSecret = new byte[32];
        Curve25519.curve(sharedSecret, randomPrivateKey, atvPublicKey);

        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
        sha512Digest.update("Pair-Verify-AES-Key".getBytes(StandardCharsets.UTF_8));
        sha512Digest.update(sharedSecret);
        byte[] sharedSecretSha512AesKey = Arrays.copyOfRange(sha512Digest.digest(), 0, 16);

        sha512Digest.update("Pair-Verify-AES-IV".getBytes(StandardCharsets.UTF_8));
        sha512Digest.update(sharedSecret);
        byte[] sharedSecretSha512AesIV = Arrays.copyOfRange(sha512Digest.digest(), 0, 16);

        Cipher aesCtr128Encrypt = Cipher.getInstance("AES/CTR/NoPadding");
        aesCtr128Encrypt.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sharedSecretSha512AesKey, "AES"), new IvParameterSpec(sharedSecretSha512AesIV));

        aesCtr128Encrypt.update(Arrays.copyOfRange(pairVerify1Response, 32, pairVerify1Response.length));

        EdDSAEngine edDSAEngine = new EdDSAEngine();
        edDSAEngine.initSign(authKey);

        byte[] signature = aesCtr128Encrypt.update(edDSAEngine.signOneShot(AuthUtils.concatByteArrays(randomPublicKey, atvPublicKey)));

        AuthUtils.postData(socket, "/pair-verify", "application/octet-stream", AuthUtils.concatByteArrays(new byte[]{0, 0, 0, 0}, signature));
    }
}
