//----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorised under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C) COPYRIGHT 2013-2016 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
//----------------------------------------------------------------------------
package com.arm.mbed.crypto;

import java.util.HashMap;
import java.util.Map;

public class CryptoConstants {
    // Supported KeyTypes
    public static final String KEY_TYPE_EC               = "EC";
    public static final String KEY_TYPE_ECDSA            = "ECDSA";
    public static final String KEY_TYPE_RSA              = "RSA";
    public static final String[] KEY_TYPES_SUPPORTED     = new String[] {KEY_TYPE_EC, KEY_TYPE_ECDSA, KEY_TYPE_RSA};

    // Supported sign algorithms
    public static final String SHA256withECDSA           = "SHA256withECDSA";
    public static final String NONEwithECDSA             = "NONEwithECDSA";
    public static final String SHA256withRSA             = "SHA256withRSA";

    // EC constants
    public static final int CURVE_SIGN_BYTE_LENGTH       = 64;
    public static final int DEFAULT_EC_KEY_LENGTH        = 128;
    public static final String DEFAULT_CURVE_NAME        = "secp256r1";

    // RSA constants
    public static final int RSA_KEY_LENGTH_2048          = 2048;
    public static final int RSA_KEY_LENGTH_4096          = 4096;
    public static final int[] RSA_KEY_LENGTHS            = new int[] {RSA_KEY_LENGTH_2048, RSA_KEY_LENGTH_4096};
    public static final String RSA_PUBLIC_EXPONENT_F0    = "F0";
    public static final String RSA_PUBLIC_EXPONENT_F4    = "F4";
    
    // SHA algorithms
    public static final String SHA1   = "SHA1";
    public static final String SHA256 = "SHA256";
    
    // Providers & KeyStores
    public static final String SC_JCA_PROVIDER_NAME      = "SC";	// SpongyCastle

    // Validation structures
    @SuppressWarnings("serial")
    public static final Map<String, String[]> VALID_SIGN_ALGOS_BY_KEY_TYPE = new HashMap<String, String[]>() {{
            put(KEY_TYPE_EC, new String[] {SHA256withECDSA, NONEwithECDSA});
            put(KEY_TYPE_ECDSA, new String[] {SHA256withECDSA, NONEwithECDSA});
            put(KEY_TYPE_RSA, new String[] {SHA256withRSA});
        }};
    @SuppressWarnings("serial")
    public static final Map<String, String[]> VALID_DECRYPT_ALGOS_BY_KEY_TYPE = new HashMap<String, String[]>() {{
            put(KEY_TYPE_EC, new String[] {});
            put(KEY_TYPE_ECDSA, new String[] {});
            put(KEY_TYPE_RSA, new String[] {});
        }};
        
    @SuppressWarnings("serial")
    public static final Map<String, Integer> PUB_KEY_ID_SHA_RANGE = new HashMap<String, Integer>() {{
            put(SHA1,   20);
            put(SHA256, 32);
        }};

    // miscellaneous
    public static final String CSR_ATTR_TRUST_LEVEL_OID    = "1.3.6.1.4.1.4128.201";
}
