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
package com.arm.mbed.dbauth.proxysdk.server;

import java.io.IOException;

import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.http.HttpHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserPasswordServer extends AuthServerBase {

    //TODO:: I need UserPasswordServer class to be Serializable, at the moment i am disabling it.
    //private ConsoleLogger logger = new ConsoleLogger(getClass());


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

        //logger.debug("Acquiring JWT from MbedCloud");
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

    private static final long serialVersionUID = 5056269713481003911L;
}
