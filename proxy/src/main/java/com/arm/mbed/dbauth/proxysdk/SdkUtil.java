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
package com.arm.mbed.dbauth.proxysdk;

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

import com.arm.mbed.dbauth.proxysdk.cose.CoseException;
import com.arm.mbed.dbauth.proxysdk.cose.Message;
import com.arm.mbed.dbauth.proxysdk.cose.OneKey;
import com.arm.mbed.dbauth.proxysdk.cose.Sign1Message;
import com.arm.mbed.dbauth.proxysdk.cwt.CwtClaimsEnum;
import com.arm.mbed.dbauth.proxysdk.cwt.CwtCose;
import com.arm.mbed.dbauth.proxysdk.cwt.CwtUtils;
import com.arm.mbed.dbauth.proxysdk.operation.Operation;
import com.arm.mbed.dbauth.proxysdk.operation.OperationBundle;
import com.arm.mbed.dbauth.proxysdk.operation.OperationTypeEnum;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.MessageIn;
import com.arm.mbed.dbauth.proxysdk.protocol.MessageTypeEnum;
import com.arm.mbed.dbauth.proxysdk.protocol.NonceRequest;
import com.arm.mbed.dbauth.proxysdk.protocol.NonceResponse;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationRequest;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationResponse;
import com.arm.mbed.dbauth.proxysdk.protocol.ProtocolConstants;
import com.arm.mbed.dbauth.proxysdk.utils.ConsoleLogger;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.EccCoseUtils;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.EccUtils;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.IProxyKeysHelper;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.ProxyKeysHelper;
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
     * ---[  Encode Message  ]--- 
     */
    public static byte[] signBundle(byte[] operationBundleBytes, KeyPair keyPair, IDevice device) {

        // Sign
        OneKey oneKey = EccCoseUtils.convertEcKeyPairToOneKey(keyPair);
        byte[] opBundleSigned;
        try {
            opBundleSigned = CwtCose.sign1(operationBundleBytes, oneKey);
        }
        catch (CoseException e) {
            String msg = "Failed to sign AccessToken with POP private key";
            logger.error(msg);
            throw new ProxyException(msg);
        }

        // send signed operation bundle to device
        OperationRequest request = new OperationRequest(opBundleSigned);
        byte[] operationMsg = request.getEncoded();
        logger.debug("Sending to device: " +
                device +
                " a Perform Operation message with a COSE signed operation-bundle: \n" +
                Base64.toBase64String(operationMsg));

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
        Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(cose.EncodeToBytes());
        
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
        
        PublicKey popPublicKeyDecoded = CwtUtils.decodePublicKey(cborPopPublicKey);
        String pemPopPubKeyResult = EccUtils.createPemStringFromKey(popPublicKeyDecoded);

        String popTrimmedResult = EccUtils.trimNewLines(pemPopPubKeyResult);
        String popTrimmedInput = EccUtils.trimNewLines(popPemPubKey);
        
        if (!popTrimmedInput.equals(popTrimmedResult)) {
            throw new ProxyException("Invalid Access Token - Proof Of Possession key mismatch!");
        }
    }
}
