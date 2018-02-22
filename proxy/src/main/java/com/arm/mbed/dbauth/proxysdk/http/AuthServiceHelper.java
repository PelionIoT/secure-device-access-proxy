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
package com.arm.mbed.dbauth.proxysdk.http;

import java.io.IOException;
import java.util.List;

import com.arm.mbed.dbauth.proxysdk.CommonConstants;
import com.arm.mbed.dbauth.proxysdk.server.IAuthServer;
import com.arm.mbed.dbauth.proxysdk.utils.ConsoleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthServiceHelper {

    private static ConsoleLogger logger = new ConsoleLogger(AuthServiceHelper.class);

    public static String createTrustAnchor(IAuthServer server, String description) {
        logger.debug("Create Trust Anchor");

        String url = server.getBaseUrl() + CommonConstants.TRUST_ANCHOR_PATH;

        CreateTrustAnchorRequest request = new CreateTrustAnchorRequest();
        request.setDescription(description);

        String responseStr;
        server.acquireJwt();
        responseStr	= HttpHandler.getInstance().httpRequest( server.getJwt(),
                url,
                request.toString(),
                CommonConstants.HTTP_REQUEST_TIMEOUT_MILLIS,
                "POST",
                true);

        logger.debug("Http Result: " + responseStr);

        CreateTrustAnchorResponse responseJson;
        try {
            responseJson = new ObjectMapper().readValue(responseStr, CreateTrustAnchorResponse.class);
        } catch (IOException e) {
            logger.error("JSON Mapper Error: " + e.getMessage());
            throw new RuntimeException("JSON Mapper Error");
        }

        return responseJson.getPublicKey();
    }

    public static String createAccessToken( IAuthServer server,
                                            List<String> audience,
                                            String scope,
                                            String popPemPublicKey) {

        logger.debug("Create Access Token");

        String url = server.getBaseUrl() + CommonConstants.TOKEN_PATH;

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setGrantType(CommonConstants.GRANT_TYPE_DEFAULT);
        request.setAudience(audience);
        request.setScope(scope);
        request.setCnf(popPemPublicKey);

        server.acquireJwt();

        String responseStr = HttpHandler.getInstance().httpRequest(
                server.getJwt(),
                url,
                request.toString(),
                CommonConstants.HTTP_REQUEST_TIMEOUT_MILLIS,
                "POST",
                true);

        System.out.println("Http Result: " + responseStr);

        CreateAccessTokenResponse responseJson;
        try {
            responseJson = new ObjectMapper().readValue(responseStr, CreateAccessTokenResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON Mapper EER");
        }

        return responseJson.getToken();
    }

}
