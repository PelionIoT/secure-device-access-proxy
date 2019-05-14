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
package com.arm.mbed.sda.proxysdk;

import java.util.List;

import org.spongycastle.util.encoders.Base64;

import com.arm.mbed.crypto.EccUtils;
import com.arm.mbed.sda.proxysdk.http.AuthServiceHelper;
import com.arm.mbed.sda.proxysdk.operation.ParamElement;
import com.arm.mbed.sda.proxysdk.protocol.OperationResponse;
import com.arm.mbed.sda.proxysdk.server.IAuthServer;
import com.arm.mbed.sda.proxysdk.utils.ConsoleLogger;
import com.upokecenter.cbor.CBORObject;

public class SecuredDeviceAccess {

    private static ConsoleLogger logger = new ConsoleLogger(SecuredDeviceAccess.class);
    
    static {
    	new EccUtils();	// init crypto utils
    }
    
    public static void setKeyStorePath(String path) {
        SdkUtil.setKeyStorePath(path);
    }

    /**
     * ---[  Get Access Token  ]---
     */
    public static String getAccessToken( IAuthServer authServer, List<String> audience, String scope) {
            
        logger.debug("Get Access Token");

        String popPemPubKey = SdkUtil.getPopPemPubKey();

        String accessToken = AuthServiceHelper.createAccessToken(authServer,
                                                                 audience,
                                                                 scope,
                                                                 popPemPubKey);
        try {
        	SdkUtil.validateTokenSanity(accessToken, popPemPubKey);
        }
        catch(Exception e) {
            String msg = String.format("Access Token did not pass basic validation! Message: %s", e.toString());
            logger.error(msg);
            throw new ProxyException(msg, e);
        }

        return accessToken;
    }

    /**
     * ---[  Send Message  ]---
     */
    public static OperationResponse sendMessage( String accessToken, String cmd, ParamElement[] params, IDevice device ) {

        logger.debug("Getting Nonce...");
        CBORObject nonceNumber = SdkUtil.getNonce(device);

        logger.debug("Building Operation Bundle");
        byte[] operationBundleBytes = SdkUtil.encodeOperationBundle(cmd, params, accessToken, nonceNumber);

        // Sign operation bundle
        logger.debug("Signing operation bundle with PoP private key: " + CommonConstants.POP_ALIAS);

        byte[] signedOperationBundleBytes = SdkUtil.signBundle(operationBundleBytes, SdkUtil.getPopKeyPair());
        byte[] operationMsg = SdkUtil.encodeOperationRequestMessage(signedOperationBundleBytes);

        //TODO:: add debug flag so the OP will be printed on debug only.
        logger.debug("Sending to device: " +
                device +
                " a Perform Operation message with a COSE-signed operation-bundle: \n" +
                Base64.toBase64String(operationMsg));

        return SdkUtil.getOperationResponse(device, operationMsg);
    }

    /**
     * ---[  Generate new POP key pair  ]---
     */
    public static void generateNewPopKeyPair() {
    	SdkUtil.generateNewPopKeyPair();
    }

}
