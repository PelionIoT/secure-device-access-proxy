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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MbedCloudLoginResponse {

    private String account_id;
    private String user_id;
    private String token;
    private int expires_in;
    private String role;
    private String[] roles;
    private String status;
    private String mfa_status;

    public String getAccount_id() {
        return account_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getToken() {
        return token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public String getRole() {
        return role;
    }

    public String[] getRoles() {
        return roles;
    }

    public String getStatus() {
        return status;
    }

    public String getMfa_status() {
        return mfa_status;
    }
}
