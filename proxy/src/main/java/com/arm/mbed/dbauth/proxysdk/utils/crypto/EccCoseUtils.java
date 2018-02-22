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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.spongycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;

import com.arm.mbed.dbauth.proxysdk.cose.KeyKeys;
import com.arm.mbed.dbauth.proxysdk.cose.OneKey;
import com.upokecenter.cbor.CBORObject;

public class EccCoseUtils {

    /*********************************************** Public Functions *************************************************/
    public static OneKey convertEcKeyPairToOneKey(KeyPair keyPair) {

        AsymmetricKeyParameter privKeyParam = null;
        AsymmetricKeyParameter pubKeyParam = null;
        try {
            privKeyParam = generatePrivateKeyParameter(keyPair.getPrivate());
            pubKeyParam = generatePublicKeyParameter(keyPair.getPublic());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Key Conversion failed", e);
        }

        AsymmetricCipherKeyPair p1 = new AsymmetricCipherKeyPair(
                pubKeyParam,
                privKeyParam);

        ECPublicKeyParameters keyPublic = (ECPublicKeyParameters) p1.getPublic();
        ECPrivateKeyParameters keyPrivate = (ECPrivateKeyParameters) p1.getPrivate();

        byte[] rgbX = keyPublic.getQ().normalize().getXCoord().getEncoded();
        byte[] rgbY = keyPublic.getQ().normalize().getYCoord().getEncoded();
        byte[] rgbD = keyPrivate.getD().toByteArray();

        OneKey key = new OneKey();

        key.add(KeyKeys.KeyType, KeyKeys.KeyType_EC2);
        key.add(KeyKeys.EC2_Curve, KeyKeys.EC2_P256);
        key.add(KeyKeys.EC2_X, CBORObject.FromObject(rgbX));
        key.add(KeyKeys.EC2_Y, CBORObject.FromObject(rgbY));
        key.add(KeyKeys.EC2_D, CBORObject.FromObject(rgbD));

        return key;         
    }

    /*********************************************** Private Functions ************************************************/
    private static AsymmetricKeyParameter generatePrivateKeyParameter(PrivateKey key) throws InvalidKeyException {

        if (key instanceof ECPrivateKey) {
            ECPrivateKey k = (ECPrivateKey) key;
            ECParameterSpec s = k.getParameters();

            if (s == null) {
                s = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            }
            return new ECPrivateKeyParameters(
                    k.getD(), 
                    new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));

        } else if (key instanceof java.security.interfaces.ECPrivateKey) {
            java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey) key;
            ECParameterSpec s = EC5Util.convertSpec(privKey.getParams(), false);

            return new ECPrivateKeyParameters(
                    privKey.getS(), 
                    new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));

        } else {
            // see if we can build a key from key.getEncoded()
            try {
                byte[] bytes = key.getEncoded();
                if (bytes == null) {
                    throw new InvalidKeyException("no encoding for EC private key");
                }
                PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(bytes));
                if (privateKey instanceof java.security.interfaces.ECPrivateKey) {
                    return ECUtil.generatePrivateKeyParameter(privateKey);
                }
            } catch (Exception e) {
                throw new InvalidKeyException("cannot identify EC private key: " + e.toString());
            }
        }
        throw new InvalidKeyException("can't identify EC private key.");
    }

    private static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            ECPublicKey k = (ECPublicKey) key;
            ECParameterSpec s = k.getParameters();
            return new ECPublicKeyParameters(
                    k.getQ(), 
                    new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        } else if (key instanceof java.security.interfaces.ECPublicKey) {
            java.security.interfaces.ECPublicKey pubKey = (java.security.interfaces.ECPublicKey) key;
            ECParameterSpec s = EC5Util.convertSpec(pubKey.getParams(), false);
            return new ECPublicKeyParameters(
                    EC5Util.convertPoint(pubKey.getParams(), pubKey.getW(), false), 
                    new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        }
        throw new InvalidKeyException("cannot identify EC public key.");
    }

}
