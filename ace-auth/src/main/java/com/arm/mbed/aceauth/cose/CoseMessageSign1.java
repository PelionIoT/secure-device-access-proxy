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

import static java.lang.Math.min;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;

import org.spongycastle.asn1.nist.NISTNamedCurves;
import org.spongycastle.asn1.sec.SECObjectIdentifiers;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECPoint;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import com.arm.mbed.aceauth.cose.CoseConstants.AlgorithmID;
import com.arm.mbed.aceauth.cose.CoseConstants.HeaderKeys;
import com.arm.mbed.aceauth.cose.CoseConstants.MessageTag;
import com.arm.mbed.aceauth.cwt.CwtClaims;

public class CoseMessageSign1 extends CoseMessage {

    private static final String contextString = "Signature1";
    private byte[] rgbSignature;

    public CoseMessageSign1() {
        super(MessageTag.Sign1);
    }

    /**
     * Encodes this CWT with sign1.
     */
    public CBORObject sign(CwtClaims cwtClaims, PrivateKey privateKey) throws CoseException {
        return sign(cwtClaims.getClaims().EncodeToBytes(), privateKey);
    }

    public CBORObject sign(byte[] content, PrivateKey privateKey) throws CoseException {

        this.rgbContent = content;

        objProtected.Add(HeaderKeys.Algorithm.AsCBOR(), AlgorithmID.ECDSA_256.AsCBOR());
        rgbProtected = objProtected.EncodeToBytes();
        CBORObject objToSign = CBORObject.NewArray();
        objToSign.Add(contextString);
        objToSign.Add(rgbProtected);
        objToSign.Add(externalData);
        objToSign.Add(rgbContent);
        rgbSignature = signImpl(objToSign.EncodeToBytes(), privateKey);

        return EncodeToCBORObject();
    }

    public boolean validate(PublicKey pubKey) throws CoseException {
        CBORObject obj = CBORObject.NewArray();
        obj.Add(contextString);
        if (objProtected.size() > 0) obj.Add(rgbProtected);
        else obj.Add(CBORObject.FromObject(new byte[0]));
        obj.Add(externalData);
        obj.Add(rgbContent);
        return validateSignature(obj.EncodeToBytes(), rgbSignature, pubKey);
    }

    @Override
    protected void DecodeFromCBORObject(CBORObject messageObject) throws CoseException {

        if (messageObject.size() != 4) throw new CoseException("Invalid Sign1 structure");

        if (messageObject.get(0).getType() == CBORType.ByteString) {
            rgbProtected = messageObject.get(0).GetByteString();
            if (messageObject.get(0).GetByteString().length == 0) objProtected = CBORObject.NewMap();
            else {
                objProtected = CBORObject.DecodeFromBytes(rgbProtected);
                if (objProtected.size() == 0) rgbProtected = new byte[0];
            }
        }
        else throw new CoseException("Invalid Sign1 structure");

        if (messageObject.get(1).getType() == CBORType.Map) {
            objUnprotected = messageObject.get(1);
        }
        else throw new CoseException("Invalid Sign1 structure");

        if (messageObject.get(2).getType() == CBORType.ByteString) rgbContent = messageObject.get(2).GetByteString();
        else if (!messageObject.get(2).isNull()) throw new CoseException("Invalid Sign1 structure");

        if (messageObject.get(3).getType() == CBORType.ByteString) rgbSignature = messageObject.get(3).GetByteString();
        else throw new CoseException("Invalid Sign1 structure");
    }

    @Override
    protected CBORObject EncodeCBORObject() throws CoseException {

        CBORObject objWithSignature = CBORObject.NewArray();
        
        objWithSignature.Add(rgbProtected);        
        objWithSignature.Add(objUnprotected);
        objWithSignature.Add(rgbContent);
        objWithSignature.Add(rgbSignature);
        
        return objWithSignature;
    }

    private byte[] signImpl(byte[] rgbToBeSigned, PrivateKey privateKey) {

        Digest digest = new SHA256Digest();
        digest.update(rgbToBeSigned, 0, rgbToBeSigned.length);
        byte[] rgbDigest = new byte[digest.getDigestSize()];
        digest.doFinal(rgbDigest, 0);

        ECPrivateKey ecpk = (ECPrivateKey)privateKey;
        ECParameterSpec spec = EC5Util.convertSpec(ecpk.getParams(), false);

        ECPrivateKeyParameters privKeyParams =
                    new ECPrivateKeyParameters(
                                               ecpk.getS(),
                                               new ECDomainParameters(spec.getCurve(),
                                                       spec.getG(),
                                                       spec.getN(),
                                                       spec.getH(),
                                                       spec.getSeed()));

        ECDSASigner ecdsa = new ECDSASigner();
        ecdsa.init(true, privKeyParams);
        BigInteger[] sig = ecdsa.generateSignature(rgbDigest);

        ecdsa = new ECDSASigner();
        ecdsa.init(true, privKeyParams);
        sig = ecdsa.generateSignature(rgbDigest);

        int cb = (spec.getCurve().getFieldSize() + 7)/8;
        byte[] r = sig[0].toByteArray();
        byte[] s = sig[1].toByteArray();

        byte[] sigs = new byte[cb*2];
        int cbR = min(cb,r.length);
        System.arraycopy(r, r.length - cbR, sigs, cb - cbR, cbR);
        cbR = min(cb, s.length);
        System.arraycopy(s, s.length - cbR, sigs, cb + cb - cbR, cbR);

        return sigs;
    }

    private boolean validateSignature(byte[] rgbToBeSigned, byte[] rgbSignature, PublicKey pubKey) {
        
        byte[] rgbR = new byte[rgbSignature.length/2];
        byte[] rgbS = new byte[rgbSignature.length/2];
        System.arraycopy(rgbSignature, 0, rgbR, 0, rgbR.length);
        System.arraycopy(rgbSignature, rgbR.length, rgbS, 0, rgbR.length);

        Digest digest = new SHA256Digest();
        digest.update(rgbToBeSigned, 0, rgbToBeSigned.length);
        byte[] rgbDigest = new byte[digest.getDigestSize()];
        digest.doFinal(rgbDigest, 0);

        X9ECParameters p = NISTNamedCurves.getByOID(SECObjectIdentifiers.secp256r1);
        if (null == p) {
            throw new CoseException("Failed to get parameters for EC curve with OID " + SECObjectIdentifiers.secp256r1.toString());
        }
        ECDomainParameters parameters = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());

        ECPoint point = ((ECPublicKey)pubKey).getQ();
        ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(point, parameters);

        ECDSASigner ecdsa = new ECDSASigner();
        ecdsa.init(false, pubKeyParams);
        return ecdsa.verifySignature(rgbDigest, new BigInteger(1, rgbR), new BigInteger(1, rgbS));                
    }

}
