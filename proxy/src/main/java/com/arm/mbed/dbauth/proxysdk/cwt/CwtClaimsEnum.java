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
package com.arm.mbed.dbauth.proxysdk.cwt;

public enum CwtClaimsEnum {

    // draft-ietf-ace-cbor-web-token-08 standard claims
    ISSUER          ("iss", 1),
    SUBJECT         ("sub", 2),
    AUDIENCE        ("aud", 3),
    EXPIRATION_TIME ("exp", 4),
    NOT_BEFORE      ("nbf", 5),
    ISSUED_AT       ("iat", 6),
    CWT_ID          ("cti", 7),

    // ACE OAUTH
    SCOPE			("scp", 12),
    CONFIRMATION    ("cnf", 25);

    private String claimName;
    private int claimKey;

    CwtClaimsEnum(String name, int key) {
        this.claimName = name;
        this.claimKey = key;
    }

    public CwtClaimsEnum fromKey(int key) {
        for (CwtClaimsEnum e : CwtClaimsEnum.values()) {
            if (e.getClaimKey() == key) {
                return e;
            }
        }
        throw new IllegalArgumentException("Not a valid claim key: " + key);
    }

    public CwtClaimsEnum fromName(String name) {
        for (CwtClaimsEnum e : CwtClaimsEnum.values()) {
            if (e.getClaimName().equals(name)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Not a valid claim name: " + name);
    }

    public String getClaimName() {
        return claimName;
    }

    public int getClaimKey() {
        return claimKey;
    }

}
