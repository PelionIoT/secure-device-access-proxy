package com.arm.mbed.aceauth.test.cose;

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
