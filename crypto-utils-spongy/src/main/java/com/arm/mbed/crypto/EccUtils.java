//----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorised under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C) COPYRIGHT 2013-2016 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
//----------------------------------------------------------------------------
package com.arm.mbed.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.DERBitString;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.util.io.pem.PemReader;

public class EccUtils {
    private static final Logger logger = LoggerFactory.getLogger(EccUtils.class.getName());

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String providerName = CryptoConstants.SC_JCA_PROVIDER_NAME;
    private static final String dashes = "-----";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair eccKeyPairGeneration() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CryptoConstants.KEY_TYPE_EC, providerName);
            keyPairGenerator.initialize(new ECGenParameterSpec(CryptoConstants.DEFAULT_CURVE_NAME));
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new ProviderException("Failed to generate an EC key pair for provider " + providerName, e);
        }
    }

    public static byte[] encodePubKey(PublicKey pubKey)
    {
        ASN1InputStream asn1InputStream = null;
        try {
            asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded()));
            DLSequence dls = (DLSequence)asn1InputStream.readObject();
            DERBitString dbs = (DERBitString) dls.toArray()[1];
            return dbs.getBytes();
        } catch (IOException e) {
            throw new ProviderException("Failed to extract bytes from a PublicKey", e);
        } finally {
            if (asn1InputStream != null) {
                try {
                    asn1InputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String getPemNoHeadersAndCrLf(String pem) {
        return pem.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "");
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
                throw new InvalidParameterException("Failed to decode public key, unknown PEM format");
            }
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                throw (InvalidParameterException)e;
            }
            throw new InvalidParameterException("Failed to parse multiline PEM formatted public key: " + e.getMessage());
        } finally {
            if (null != pemParser) {
                try {
                    pemParser.close();
                } catch (IOException e) {
                    logger.error("Failed to close PEMParser stream");
                }
            }
        }
        for (String keyType : CryptoConstants.KEY_TYPES_SUPPORTED) {
            if (keyType.equals(pubKey.getAlgorithm())) {
                return pubKey;
            }
        }
        throw new InvalidParameterException("Failed to decode public key, unknow key type '" + pubKey.getAlgorithm() + "'");
    }

    public static PrivateKey decodePEMPrivateKey(String pemPrivKey) {

        PrivateKey privKey;
        PEMParser pemParser = null;
        try {
            pemParser = new PEMParser(new PemReader(new StringReader(enforce3linesKey(pemPrivKey))));
            Object pemObject = pemParser.readObject();
            if (pemObject instanceof PEMKeyPair || pemObject instanceof PrivateKeyInfo) {
                JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter().setProvider(providerName);
                if (pemObject instanceof PEMKeyPair) {
                    KeyPair keyPair = pemKeyConverter.getKeyPair((PEMKeyPair)pemObject);
                    privKey = keyPair.getPrivate();
                } else {
                    privKey = pemKeyConverter.getPrivateKey((PrivateKeyInfo)pemObject);
                }
            } else {
                throw new InvalidParameterException("Failed to decode private key, unknown PEM format");
            }
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                throw (InvalidParameterException)e;
            }
            throw new InvalidParameterException("Failed to decode private key:" + e.getMessage());
        } finally {
            if (null != pemParser) {
                try {
                    pemParser.close();
                } catch (IOException e) {
                    logger.error("Failed to close PEMParser stream");
                }
            }
        }
        for (String keyType : CryptoConstants.KEY_TYPES_SUPPORTED) {
            if (keyType.equals(privKey.getAlgorithm())) {
                return privKey;
            }
        }
        throw new InvalidParameterException("Failed to decode private key, unknow key type '" + privKey.getAlgorithm() + "'");
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

    public static PKCS10CertificationRequest decodePEMCsr(String pemCsr) {

        PEMParser pemParser = null;
        try {
            pemParser = new PEMParser(new PemReader(new StringReader(enforce3linesKey(pemCsr))));
            Object pemObject = pemParser.readObject();
            return (PKCS10CertificationRequest) pemObject;
        } catch (Exception e) {
            throw new InvalidParameterException("Failed to parse PEM encoded csr:" + e.getMessage());
        } finally {
            if (null != pemParser) {
                try {
                    pemParser.close();
                } catch (IOException e) {
                    logger.error("Failed to close PEMParser stream");
                }
            }
        }
    }

    public static X509Certificate decodePEMCertificate(String pemCertificate) throws IOException {

        PEMParser pemParser = null;
        try {
            validateCertificatePemBase64(pemCertificate);
            pemParser = new PEMParser(new PemReader(new StringReader(enforce3linesKey(pemCertificate))));
            Object pemObject = pemParser.readObject();
            if (pemObject instanceof X509CertificateHolder) {
                return new JcaX509CertificateConverter().
                        setProvider(providerName).
                        getCertificate((X509CertificateHolder)pemObject);
            } else {
                throw new InvalidParameterException("Failed to parse PEM encoded certificate, unknown PEM format");
            }
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                throw (InvalidParameterException)e;
            }
            throw new InvalidParameterException("Failed to parse PEM encoded certificate: " + e.getMessage());
        } finally {
            if (null != pemParser) {
                try {
                    pemParser.close();
                } catch (IOException e) {
                    logger.error("Failed to close PEMParser stream");
                }
            }
        }
    }

    public static PublicKey convertPublicKeyInfoToPublicKey(SubjectPublicKeyInfo pubKeyInfo) {
        try {
            JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter()
                    .setProvider(providerName);

            return pemKeyConverter.getPublicKey(pubKeyInfo);
        } catch (Exception e) {
            throw new InvalidParameterException("Failed to decode public key: " + e.getMessage());
        }

    }

    public static byte[] getPubKeyId(String pemPubKey, String sha) {
        PublicKey pubKey = decodePEMPublicKey(pemPubKey);
        return getPubKeyId(pubKey, sha);
    }

    public static byte[] getPubKeyId(PublicKey pubKey, String sha) {

        logger.debug("getPubKeyId({}, {})", Base64.getEncoder().encodeToString(pubKey.getEncoded()), sha);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(sha, providerName);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            logger.error("Failed to compute the public key id", e);
            throw new ProviderException("Failed to compute the public key id", e);
        }
        byte[] digest = md.digest(pubKey.getEncoded());
        return Arrays.copyOfRange(digest, 0, CryptoConstants.PUB_KEY_ID_SHA_RANGE.get(sha));    // 160 bits
    }

    public static String enforce3linesKey(String pemKeyStr) {
        if (pemKeyStr.indexOf('\n') > 0) {
            return pemKeyStr;
        }
        int indexOf2nd = pemKeyStr.indexOf(dashes, dashes.length());
        if (indexOf2nd < 0) {
            throw new InvalidParameterException("Malformatted PEM string");
        }
        int indexOf3rd = pemKeyStr.indexOf(dashes, indexOf2nd + dashes.length());
        if (indexOf3rd < 0) {
            throw new InvalidParameterException("Malformatted PEM string");
        }
        return pemKeyStr.substring(0, indexOf2nd) + dashes + "\n" +
        pemKeyStr.substring(indexOf2nd + dashes.length(), indexOf3rd) + "\n" +
        pemKeyStr.substring(indexOf3rd, pemKeyStr.length());
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
                // This happens when the BigInteger "looks like" a negative number, but is actually positive - this is the sign bit.
                System.arraycopy(bytes, 1, correctedBytes, 0, expectedByteAraySize);
                return correctedBytes;
            } else if (bytes.length < expectedByteAraySize) {
                // Array contains less than the expected amount of bytes - because they are zeroes. Prepend zeroes.
                System.arraycopy(bytes, 0, correctedBytes, expectedByteAraySize - bytes.length, bytes.length);
                return correctedBytes;
            } else {
                throw new InvalidParameterException("Unexpected big integer size");
            }
        }
    }

    public static String trimNewLines(String str) {
        String trimmed = str.replace("\r", "").replace("\n", "").replace(LINE_SEPARATOR, "");
        return trimmed;
    }

    private static void validateCertificatePemBase64(String pem) {
        String pemNoHeaders = pem.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "");
        Base64.getDecoder().decode(pemNoHeaders);
    }

    private static void validatePubKeyPemBase64(String pem) {
        String pemNoHeaders = pem.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "");
        Base64.getDecoder().decode(pemNoHeaders);
    }
}
