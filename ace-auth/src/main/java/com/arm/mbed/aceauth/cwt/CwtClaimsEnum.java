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
package com.arm.mbed.aceauth.cwt;

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
