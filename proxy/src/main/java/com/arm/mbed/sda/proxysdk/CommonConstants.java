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

public class CommonConstants {

    public final static String POP_ALIAS    = "proxy-pop-keypair";

    public static final String TOKEN_PATH = "/ace-auth/token";
    public static final String TRUST_ANCHOR_PATH = "/v3/trust-anchors";

    public static final int HTTP_REQUEST_TIMEOUT_MILLIS = 1000 * 30;  // 30 seconds timeout.
    
    public static final String GRANT_TYPE_DEFAULT = "client_credentials";
}
