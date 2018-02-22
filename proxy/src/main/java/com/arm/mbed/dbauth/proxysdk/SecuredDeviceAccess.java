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

import java.util.List;

import com.arm.mbed.dbauth.proxysdk.http.AuthServiceHelper;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationResponse;
import com.arm.mbed.dbauth.proxysdk.server.IAuthServer;
import com.arm.mbed.dbauth.proxysdk.utils.ConsoleLogger;
import com.upokecenter.cbor.CBORObject;

public class SecuredDeviceAccess {

    private static ConsoleLogger logger = new ConsoleLogger(SecuredDeviceAccess.class);
    
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

        byte[] operationMsg = SdkUtil.signBundle(operationBundleBytes, SdkUtil.getPopKeyPair(), device);

        //TODO:: add debug flag so the OP will be printed on debug only.
        //String base64 = new String(Base64.encode(operationMsg));

        byte[] response = device.sendMessage(operationMsg);

        // parse the response
        return SdkUtil.parseResponse(response);
    }

    /**
     * ---[  Generate new POP key pair  ]---
     */
    public static void generateNewPopKeyPair() {
    	SdkUtil.generateNewPopKeyPair();
    }

}
