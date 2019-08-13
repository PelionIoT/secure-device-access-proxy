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
