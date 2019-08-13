// ----------------------------------------------------------------------------
// Copyright 2017-2019 ARM Ltd.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ----------------------------------------------------------------------------
package com.arm.mbed.aceauth.cose;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import org.spongycastle.asn1.nist.NISTNamedCurves;
import org.spongycastle.asn1.sec.SECObjectIdentifiers;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.jce.spec.ECNamedCurveSpec;

import com.upokecenter.cbor.CBORObject;

import com.arm.mbed.crypto.CryptoConstants;
import com.arm.mbed.crypto.EccUtils;

public class CoseUtils {
	
    public static CBORObject encodeCoseKey(PublicKey pubKey) {
        
        try {
            // Convert to Spongy form.
            ECPublicKeyParameters pubKeyParams = (ECPublicKeyParameters)PublicKeyFactory.createKey(pubKey.getEncoded());
            
            // Extract X
            BigInteger bigX = pubKeyParams.getQ().getAffineXCoord().toBigInteger();
            byte[] bytesX = EccUtils.convertBigIntegerToByteArray(bigX, CoseConstants.EC_PUBLIC_KEY_BYTE_LENGTH);
            // As CBOR X.
            CBORObject cborX = CBORObject.FromObject(bytesX).Untag();
            
            // Extract Y
            BigInteger bigY = pubKeyParams.getQ().getAffineYCoord().toBigInteger();
            byte[] bytesY = EccUtils.convertBigIntegerToByteArray(bigY, CoseConstants.EC_PUBLIC_KEY_BYTE_LENGTH);
            // As CBOR Y.
            CBORObject cborY = CBORObject.FromObject(bytesY).Untag();
            
            // CBOR Cose Type,Curve,X,Y.
            CBORObject coseKey = CBORObject.NewMap();            
            coseKey.Add(CBORObject.FromObject(CoseConstants.KEY_TYPE_KEY).Untag(), CBORObject.FromObject(CoseConstants.KEY_TYPE).Untag());
            coseKey.Add(CBORObject.FromObject(CoseConstants.CURVE_KEY).Untag(),  CBORObject.FromObject(CoseConstants.CURVE).Untag());
            coseKey.Add(CBORObject.FromObject(CoseConstants.X_KEY).Untag(),  cborX);
            coseKey.Add(CBORObject.FromObject(CoseConstants.Y_KEY).Untag(),  cborY);
            
            // Wrap it with CBOR as CoseKeyName.
            CBORObject cnf = CBORObject.NewMap();
            cnf.Add(CBORObject.FromObject(CoseConstants.COSE_KEY_NAME).Untag(), coseKey);
            return cnf;
        } catch (Exception x) {
            throw new CoseException("Failed to encode cose key", x);
        } 
    }

    public static PublicKey decodeCoseKey(CBORObject cnfCbor) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException {
        CBORObject coseKey = cnfCbor.get(CBORObject.FromObject(CoseConstants.COSE_KEY_NAME).Untag());

        int keyType = coseKey.get(CBORObject.FromObject(CoseConstants.KEY_TYPE_KEY).Untag()).AsInt32();
        int curve = coseKey.get(CBORObject.FromObject(CoseConstants.CURVE_KEY).Untag()).AsInt32();

        PublicKey publicKey;

        if (keyType == CoseConstants.KEY_TYPE && curve == CoseConstants.CURVE) {
            byte[] x = coseKey.get(CBORObject.FromObject(CoseConstants.X_KEY).Untag()).GetByteString();
            byte[] y = coseKey.get(CBORObject.FromObject(CoseConstants.Y_KEY).Untag()).GetByteString();

            X9ECParameters x9EcParams = NISTNamedCurves.getByOID(SECObjectIdentifiers.secp256r1);
            if (null == x9EcParams) {
                throw new CoseException("Failed to get parameters for EC curve with OID " + SECObjectIdentifiers.secp256r1.toString());
            }
            String curveName = NISTNamedCurves.getName(SECObjectIdentifiers.secp256r1);
            ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec(curveName, x9EcParams.getCurve(), x9EcParams.getG(), x9EcParams.getN());
            BigInteger bigX = new BigInteger(1,x);
            BigInteger bigY = new BigInteger(1,y);
            ECPoint pointW = new ECPoint(bigX, bigY);
            ECPublicKeySpec ecPubKeySpec = new ECPublicKeySpec(pointW, eCNamedCurveSpec);
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstants.KEY_TYPE_ECDSA, CryptoConstants.SC_JCA_PROVIDER_NAME);
            publicKey = keyFactory.generatePublic(ecPubKeySpec);
        }
        else {
            throw new IllegalArgumentException("Algorithm parameters not supported");
        }
        return publicKey;
    }

}
