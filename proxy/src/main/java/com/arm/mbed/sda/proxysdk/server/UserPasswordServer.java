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
package com.arm.mbed.sda.proxysdk.server;

import java.io.IOException;

import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.http.HttpHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserPasswordServer extends AuthServerBase {

    private static final long serialVersionUID = 5056269713481003911L;

    private String accountId;
    private String username;
    private String password;

    public UserPasswordServer(String baseUrl, String accountId, String username, String password) {
        super(baseUrl);

        this.accountId = accountId;
        this.username = username;
        this.password = password;
    }

    @Override
    public String acquireJwt() {

        this.jwt = loginToMbed();
        return this.jwt;
    }

    private String loginToMbed() {

        MbedCloudLoginRequest loginRequest = new MbedCloudLoginRequest(this.accountId, this.username, this.password);

        try {
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            String jsonRequest = mapper.writeValueAsString(loginRequest);

            String jsonResponse = HttpHandler.getInstance().httpRequest(this.getBaseUrl() + "/auth/login", jsonRequest,
                    1000 * 60, "POST");

            if (null == jsonResponse) {
                throw new ProxyException("Unable to Login to MbedCloud. (Empty Response)");
            }

            MbedCloudLoginResponse loginResponse = mapper.readValue(jsonResponse, MbedCloudLoginResponse.class);
            return loginResponse.getToken();
        } catch (IOException e) {
            throw new ProxyException("Failed to serialize/deserialize to/from JSON: ", e);
        }
    }
}
