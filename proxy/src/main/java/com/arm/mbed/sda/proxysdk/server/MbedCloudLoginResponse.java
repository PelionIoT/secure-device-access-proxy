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
package com.arm.mbed.sda.proxysdk.server;

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
