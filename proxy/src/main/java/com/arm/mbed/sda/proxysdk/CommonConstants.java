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

public class CommonConstants {

    public final static String POP_ALIAS    = "proxy-pop-keypair";

    public static final String TOKEN_PATH = "/ace-auth/token";
    public static final String TRUST_ANCHOR_PATH = "/v3/trust-anchors";

    public static final int HTTP_REQUEST_TIMEOUT_MILLIS = 1000 * 30;  // 30 seconds timeout.
    
    public static final String GRANT_TYPE_DEFAULT = "client_credentials";
}
