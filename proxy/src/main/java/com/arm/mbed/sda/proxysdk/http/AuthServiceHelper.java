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
package com.arm.mbed.sda.proxysdk.http;

import java.io.IOException;
import java.util.List;

import com.arm.mbed.sda.proxysdk.CommonConstants;
import com.arm.mbed.sda.proxysdk.server.IAuthServer;
import com.arm.mbed.sda.proxysdk.utils.ConsoleLogger;
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
