// ----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorized under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C)COPYRIGHT 2018 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
// ----------------------------------------------------------------------------
package com.arm.mbed.dbauth.proxysdk.utils.crypto;

import static com.arm.mbed.dbauth.proxysdk.utils.crypto.CryptoConstants.ALGO_EC;
import static com.arm.mbed.dbauth.proxysdk.utils.crypto.CryptoConstants.ALGO_ECDSA;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.io.pem.PemReader;

import com.arm.mbed.dbauth.proxysdk.ProxyException;

public class EccUtils {

    private static final String dashes = "-----";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static {
        //Add BouncyCastle provider
        loadJcaJceProvider();
    }

    public static KeyPair eccKeyPairGeneration(String providerName) {
        try {
            java.security.KeyPairGenerator keyPairGenerator = 
                    java.security.KeyPairGenerator.getInstance("EC", providerName);
            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to generate an EC key pair for provider " + providerName, e);
        }
    }

    /**
     * ---[  Create PEM String from Key  ]--- 
     */
    public static String createPemStringFromKey(Key key) throws IOException {

        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(key);
        pemWriter.flush();
        pemWriter.close();

        return stringWriter.toString();
    }

    public static PublicKey decodePEMPublicKey(String pemPubKey) {

        PEMParser pemParser = null;
        PublicKey pubKey;
        try {
            validatePubKeyPemBase64(pemPubKey);
            pemParser = new PEMParser(new PemReader(new StringReader(enforce3linesKey(pemPubKey))));
            Object pemObject = pemParser.readObject();
            if (pemObject == null) throw new Exception("pemParser.readObject() returned null unexpectedly!");
            if (pemObject instanceof SubjectPublicKeyInfo) {
                pubKey = convertPublicKeyInfoToPublicKey((SubjectPublicKeyInfo)pemObject);
            } else {
                throw new ProxyException("Failed to decode public key, unknown PEM format");
            }
        } catch (Exception e) {
            if (e instanceof ProxyException) {
                throw (ProxyException)e;
            }
            throw new ProxyException("Failed to parse multiline PEM formatted public key", e);
        } finally {
            if (null != pemParser) {
                try {
                    pemParser.close();
                } catch (IOException e) {}
            }
        }
        if (ALGO_EC.equals(pubKey.getAlgorithm()) || ALGO_ECDSA.equals(pubKey.getAlgorithm())) {
            return pubKey;
        }
        throw new ProxyException("Failed to decode public key, unknow key type '" + pubKey.getAlgorithm() + "'");
    }

    public static void loadJcaJceProvider() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("org.spongycastle.jce.provider.BouncyCastleProvider");
            Provider bouncyCastleProvider = (Provider) clazz.newInstance();
            Security.addProvider(bouncyCastleProvider);
        } catch (Exception e) {
            throw new ProxyException("Proxy failed to initialize: " + e.getMessage());
        }
    }

    private static PublicKey convertPublicKeyInfoToPublicKey(SubjectPublicKeyInfo pubKeyInfo) {
        try {
            JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter().setProvider(
                    CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE);
            return pemKeyConverter.getPublicKey(pubKeyInfo);
        } catch (Exception e) {
            throw new ProxyException("Failed to decode public key", e);
        }
    }

    private static void validatePubKeyPemBase64(String pem) {
        String pemNoHeaders = pem.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "");
        Base64.decode(pemNoHeaders);
    }

    private static String enforce3linesKey(String pemKeyStr) {
        if (pemKeyStr.indexOf('\n') > 0) {
            return pemKeyStr;
        }
        int indexOf2nd = pemKeyStr.indexOf(dashes, dashes.length());
        if (indexOf2nd < 0) {
            throw new IllegalArgumentException("Malformatted PEM string");
        }
        int indexOf3rd = pemKeyStr.indexOf(dashes, indexOf2nd + dashes.length());
        if (indexOf3rd < 0) {
            throw new IllegalArgumentException("Malformatted PEM string");
        }
        return pemKeyStr.substring(0, indexOf2nd) + dashes + "\n" +
        pemKeyStr.substring(indexOf2nd + dashes.length(), indexOf3rd) + "\n" +
        pemKeyStr.substring(indexOf3rd, pemKeyStr.length());
    }

    public static String trimNewLines(String str) {
        String trimmed = str.replace("\r", "").replace("\n", "").replace(LINE_SEPARATOR, "");
        return trimmed;
    }

    /**
     * Converts BigInteger to a byte array.
     * Special treatment needed in case the BigInteger appears like a negative number, or required less bytes than
     * we expect.
     */
    public static byte[] convertBigIntegerToByteArray(BigInteger bigInt, int expectedByteAraySize) {
        assert bigInt != null;
        assert expectedByteAraySize > 0;

        byte[] bytes = bigInt.toByteArray();

        if (bytes.length == expectedByteAraySize) {
            // Array is of the expected size - just return it.
            return bytes;
        } else {
            byte[] correctedBytes = new byte[expectedByteAraySize];  // Filled with zeroes.
            if (bytes.length == expectedByteAraySize + 1) {
                // Array contains one extra character at the beginning, which should be 0x00. Trim it.
                // This happens when the BigInteger "looks like" a negative number, 
                // but is actually positive - this is the sign bit.
                System.arraycopy(bytes, 1, correctedBytes, 0, expectedByteAraySize);
                return correctedBytes;
            } else if (bytes.length < expectedByteAraySize) {
                // Array contains less than the expected amount of bytes - because they are zeroes. Prepend zeroes.
                System.arraycopy(bytes, 0, correctedBytes, expectedByteAraySize - bytes.length, bytes.length);
                return correctedBytes;
            } else {
                throw new ProxyException("Unexpected big integer size");
            }
        }
    }

}
