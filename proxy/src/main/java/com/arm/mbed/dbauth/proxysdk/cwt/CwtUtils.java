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
package com.arm.mbed.dbauth.proxysdk.cwt;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.util.PublicKeyFactory;

import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.CryptoConstants;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.EccUtils;
import com.upokecenter.cbor.CBORObject;

public class CwtUtils {

    public static final int COSE_KEY_NAME               = 1;    // "COSE_Key"
    public static final int KEY_TYPE_KEY                = 1;
    public static final int KEY_TYPE                    = 2;    // "EC"
    public static final int CURVE_KEY                   = -1;
    public static final int CURVE                       = 1;    // "P-256"
    public static final int X_KEY                       = -2;
    public static final int Y_KEY                       = -3;
    public static final int EC_PUBLIC_KEY_BYTE_LENGTH   = 32;
    
    public static CBORObject encodeCoseKey(PublicKey pubKey) {
        try {
            // Convert to Spongy form.
            ECPublicKeyParameters pubKeyParams = (ECPublicKeyParameters)PublicKeyFactory.createKey(pubKey.getEncoded());
            
            // Extract X
            BigInteger bigX = pubKeyParams.getQ().getAffineXCoord().toBigInteger();
            byte[] bytesX = EccUtils.convertBigIntegerToByteArray(bigX, EC_PUBLIC_KEY_BYTE_LENGTH);
            // As CBOR X.
            CBORObject cborX = CBORObject.FromObject(bytesX).Untag();
            
            // Extract Y
            BigInteger bigY = pubKeyParams.getQ().getAffineYCoord().toBigInteger();
            byte[] bytesY = EccUtils.convertBigIntegerToByteArray(bigY, EC_PUBLIC_KEY_BYTE_LENGTH);
            // As CBOR Y.
            CBORObject cborY = CBORObject.FromObject(bytesY).Untag();
            
            // CBOR Cose Type,Curve,X,Y.
            CBORObject coseKey = CBORObject.NewMap();            
            coseKey.Add(CBORObject.FromObject(KEY_TYPE_KEY).Untag(), CBORObject.FromObject(KEY_TYPE).Untag());
            coseKey.Add(CBORObject.FromObject(CURVE_KEY).Untag(),  CBORObject.FromObject(CURVE).Untag());
            coseKey.Add(CBORObject.FromObject(X_KEY).Untag(),  cborX);
            coseKey.Add(CBORObject.FromObject(Y_KEY).Untag(),  cborY);
            
            // Wrap it with CBOR as CoseKeyName.
            CBORObject cnf = CBORObject.NewMap();
            cnf.Add(CBORObject.FromObject(COSE_KEY_NAME).Untag(), coseKey);
            return cnf;
        } catch (Exception x) {
            throw new ProxyException("Unable to encode Cose CBORObject from Public-Key.", x);
        } 
    }
    
    public static PublicKey decodePublicKey(CBORObject cnfCbor) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException {
        CBORObject coseKey = cnfCbor.get(CBORObject.FromObject(COSE_KEY_NAME).Untag());
        
        int keyType = coseKey.get(CBORObject.FromObject(KEY_TYPE_KEY).Untag()).AsInt32();
        int curveKey = coseKey.get(CBORObject.FromObject(CURVE_KEY).Untag()).AsInt32();
        
        PublicKey publicKey;
        
        if (keyType == KEY_TYPE && curveKey == CURVE) {
            byte[] x = coseKey.get(CBORObject.FromObject(X_KEY).Untag()).GetByteString();
            byte[] y = coseKey.get(CBORObject.FromObject(Y_KEY).Untag()).GetByteString();
            
            ECPoint ecPubPoint = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
            AlgorithmParameters algoParams = AlgorithmParameters.getInstance(CryptoConstants.ALGO_EC, CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE);
            algoParams.init(new ECGenParameterSpec(CryptoConstants.ALGO_EC_PARAM));
            
            ECParameterSpec ecParams = algoParams.getParameterSpec(ECParameterSpec.class);
            
            ECPublicKeySpec ecPubKeySpec = new ECPublicKeySpec(ecPubPoint, ecParams);
            
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstants.ALGO_EC);
            publicKey = keyFactory.generatePublic(ecPubKeySpec);
        }
        else {
            throw new IllegalArgumentException("Algorithm parameters not supported");
        }
        
        return publicKey;
    }

}
