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
package com.arm.mbed.sda.proxysdk;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.spongycastle.util.encoders.Base64;

import com.arm.mbed.aceauth.cose.CoseConstants.MessageTag;
import com.arm.mbed.aceauth.cose.CoseException;
import com.arm.mbed.aceauth.cose.CoseMessage;
import com.arm.mbed.aceauth.cose.CoseMessageSign1;
import com.arm.mbed.aceauth.cose.CoseUtils;
import com.arm.mbed.aceauth.cwt.CwtClaimsEnum;
import com.arm.mbed.sda.proxysdk.operation.Operation;
import com.arm.mbed.sda.proxysdk.operation.OperationBundle;
import com.arm.mbed.sda.proxysdk.operation.OperationTypeEnum;
import com.arm.mbed.sda.proxysdk.operation.ParamElement;
import com.arm.mbed.sda.proxysdk.protocol.MessageIn;
import com.arm.mbed.sda.proxysdk.protocol.MessageTypeEnum;
import com.arm.mbed.sda.proxysdk.protocol.NonceRequest;
import com.arm.mbed.sda.proxysdk.protocol.NonceResponse;
import com.arm.mbed.sda.proxysdk.protocol.OperationRequest;
import com.arm.mbed.sda.proxysdk.protocol.OperationResponse;
import com.arm.mbed.sda.proxysdk.protocol.ProtocolConstants;
import com.arm.mbed.sda.proxysdk.utils.ConsoleLogger;
import com.arm.mbed.crypto.EccUtils;
import com.arm.mbed.sda.proxysdk.utils.crypto.IProxyKeysHelper;
import com.arm.mbed.sda.proxysdk.utils.crypto.ProxyKeysHelper;
import com.upokecenter.cbor.CBORObject;

public class SdkUtil {

    private static ConsoleLogger logger = new ConsoleLogger(SdkUtil.class);
    private static final byte[] NONCE_REQUEST_BYTES = new NonceRequest().getEncoded();
    private static IProxyKeysHelper keysHelper;
    
    public static void setKeyStorePath(String path) {
        keysHelper = new ProxyKeysHelper(KeyStoreFactory.getKeyStore(path));
    }

    /**
     * ---[  Get Nonce  ]---
     */
    public static CBORObject getNonce(IDevice device) {

        // Get Nonce
        byte[] nonceResponseBytes = device.sendMessage(NONCE_REQUEST_BYTES);

        logger.debug("Nonce Bytes Response: " + Base64.toBase64String(nonceResponseBytes));

        MessageIn msgIn = new MessageIn(nonceResponseBytes);
        if (!msgIn.getType().equals(MessageTypeEnum.NONCE_RESPONSE)) {
            String msg = "Invalid response to Get Nonce request: " + msgIn.getType().toString();
            logger.error(msg);
            throw new ProxyException(msg);
        }
        NonceResponse nonceResponse = new NonceResponse(msgIn);
        if (nonceResponse.getResponseStatus() != ProtocolConstants.STATUS_OK) {
            String msg = "Bad nonce response status: " + nonceResponse.getResponseStatus();
            logger.error(msg);
            throw new ProxyException(msg);
        }
        CBORObject nonce = nonceResponse.getNonce();
        logger.debug("Device responded with the nonce Number: " + nonce.ToJSONString());

        return nonce;
    }


    /**
     * ---[  Get OperationResponse  ]---
     */
    public static OperationResponse getOperationResponse(IDevice device, byte[] operationMsg) {
        byte[] response = device.sendMessage(operationMsg);

        // parse the response
        return SdkUtil.parseResponse(response);
    }

    /**
     * ---[  Generate KeyPair  ]---
     */
    public static KeyPair generateKeyPair() {
        KeyPair keyPair = keysHelper.generateKeyPair();
        logger.debug("Generated a new keypair");

        return keyPair;
    }

    /**
     * ---[  Generate KeyPair  ]---
     */
    public static KeyPair generateNewPopKeyPair() {
    	keysHelper.deleteKeyPair(CommonConstants.POP_ALIAS);
    	return getPopKeyPair();
    }

    /**
     * ---[  Get PoP KeyPair  ]---
     */
    public static KeyPair getPopKeyPair() {
        KeyPair popKeyPair = keysHelper.getKeyPairFromKeyStore(CommonConstants.POP_ALIAS);
        if (null == popKeyPair) {
            popKeyPair = keysHelper.generateKeyPair();
            keysHelper.storePopKeyPair(CommonConstants.POP_ALIAS, popKeyPair);
            logger.debug("Generated & stored in local keystore the POP keypair " + CommonConstants.POP_ALIAS);
        }

        return popKeyPair;
    }

    /**
     * ---[  Sign Operation Bundle  ]--- 
     */
    public static byte[] signBundle(byte[] operationBundleBytes, KeyPair keyPair) {

        // Sign
        CoseMessageSign1 sign1Msg = new CoseMessageSign1();
        try {
            byte[] opBundleSigned = sign1Msg.sign(operationBundleBytes, keyPair.getPrivate()).EncodeToBytes();
            return opBundleSigned;
        }
        catch (CoseException e) {
            String msg = "Failed to sign AccessToken with POP private key";
            logger.error(msg);
            throw new ProxyException(msg);
        }
    }

    /**
     * ---[  Encode Message  ]--- 
     */
    public static byte[] encodeOperationRequestMessage(byte[] opBundleSigned) {

	    OperationRequest request = new OperationRequest(opBundleSigned);
	    byte[] operationMsg = request.getEncoded();
	
	    return operationMsg;
    }

    public static OperationResponse parseResponse(byte[] responseBytes) {
        MessageIn msgIn = new MessageIn(responseBytes);
        if (!msgIn.getType().equals(MessageTypeEnum.OPERATION_RESPONSE)) {
            throw new ProxyException("Invalid response to Operation request: " + msgIn.getType().toString());
        }
        OperationResponse opResponse = new OperationResponse(msgIn);
        if (opResponse.getResponseStatus() != ProtocolConstants.STATUS_OK) {
            throw new ProxyException("Bad operation response status: " + opResponse.getResponseStatus());
        }

        return opResponse;
    }

    /**
     * ---[  Encode Operation Bundle  ]--- 
     */
    public static byte[] encodeOperationBundle(String cmd, ParamElement[] params, String accessToken, CBORObject nonce) {

        Operation op = new Operation.Builder()
                .setType(OperationTypeEnum.FUNCTION_CALL.getValue())
                .setFunction(cmd)                   // E.g: "lcd-display"
                .setParams(params)                  // E.g: "hello dev1"
                .build();

        OperationBundle opb = new OperationBundle.Builder()
                .setToken(Base64.decode(accessToken))
                .setOperation(op)
                .setNonce(nonce)
                .build();

        return opb.getEncoded();
    }

    /**
     *  -------------[  Validation Util Methods  ]------------------   
     */
    public static String getString(CBORObject cbor, CwtClaimsEnum cwtEnum) {
        String value = getClaimsValue(cbor, cwtEnum).AsString();
        return value;
    }
    
    public static Date getDate(CBORObject cbor, CwtClaimsEnum cwtEnum) {
        long seconds = getClaimsValue(cbor, cwtEnum).AsInt64();
        long millis = seconds * 1000;
        return new Date(millis);
    }
    
    public static CBORObject getClaimsValue(CBORObject cbor, CwtClaimsEnum cwtEnum) {
        return cbor.get( CBORObject.FromObject(cwtEnum.getClaimKey()) );
    }
    
    
    //Converting string lists from CBOR to String (for test convenience)
    public static List<String> getListOfString(Collection<CBORObject> list) {
    	if (list.isEmpty()) return null;
    	List<String> result = new ArrayList<String>();
    	for (CBORObject o: list) {
    		String s = o.AsString();
    		result.add(s);
    	}
    	return result;
    }
    
    /**
     * ---[  Get Proof-of-Possession PublicKey  ]---
     */
    public static String getPopPemPubKey() {

        KeyPair popKeyPair = SdkUtil.getPopKeyPair();
        String pemPublicKey;

        try {
            pemPublicKey = EccUtils.createPemStringFromKey(popKeyPair.getPublic());
        }
        catch (IOException e) {
            throw new ProxyException("Failed to create POP keypair", e);
        }

        return pemPublicKey;
    }


    public static void validateTokenSanity(String accessToken, String popPemPubKey) throws CoseException, 
                                                                                            NoSuchAlgorithmException, 
                                                                                            NoSuchProviderException, 
                                                                                            InvalidParameterSpecException, 
                                                                                            InvalidKeySpecException, 
                                                                                            IOException {        
        // 1. Decode Base64 to bytes
        byte[] cwtTokenBytes = Base64.decode(accessToken);
        ByteArrayInputStream bais = new ByteArrayInputStream(cwtTokenBytes);
        BufferedInputStream bufIn = new BufferedInputStream(bais);
        
        // 2. CBOR Read
        CBORObject cborCose61 = CBORObject.Read(bufIn);
        
        // 3. UnTag ALL
        CBORObject cose = cborCose61.UntagOne();
        
        // 4. Validating Signature
        CoseMessageSign1 msg = (CoseMessageSign1) CoseMessage.DecodeFromBytes(cose.EncodeToBytes(), MessageTag.Sign1);
        
        // 5. Validate Claims:
        CBORObject cwtClaimsMap = CBORObject.DecodeFromBytes(msg.GetContent());
        
        //    Audience
        CBORObject resultAudience = SdkUtil.getClaimsValue(cwtClaimsMap, CwtClaimsEnum.AUDIENCE);
        String resultScope = SdkUtil.getString(cwtClaimsMap, CwtClaimsEnum.SCOPE);
        String resultTrustAnchor = SdkUtil.getString(cwtClaimsMap, CwtClaimsEnum.ISSUER);
        CBORObject cborPopPublicKey = SdkUtil.getClaimsValue(cwtClaimsMap, CwtClaimsEnum.CONFIRMATION);

        logger.debug(String.format("AccessToken Result:"));
        logger.debug(String.format("Audience ........:%s", resultAudience ));
        logger.debug(String.format("Scope ...........:%s", resultScope ));
        logger.debug(String.format("Trust Anchor ....:%s", resultTrustAnchor ));
        
        PublicKey popPublicKeyDecoded = CoseUtils.decodeCoseKey(cborPopPublicKey);
        String pemPopPubKeyResult = EccUtils.createPemStringFromKey(popPublicKeyDecoded);

        String popTrimmedResult = EccUtils.trimNewLines(pemPopPubKeyResult);
        String popTrimmedInput = EccUtils.trimNewLines(popPemPubKey);
        
        if (!popTrimmedInput.equals(popTrimmedResult)) {
            throw new ProxyException("Invalid Access Token - Proof Of Possession key mismatch!");
        }
    }
}
