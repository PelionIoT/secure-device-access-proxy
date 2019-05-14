package com.arm.mbed.aceauth.test.cose;

public enum MessageTag {
    Unknown(0),
    Encrypt0(16),
    Encrypt(96),
    Sign1(18),
    Sign(98),
    MAC(97),
    MAC0(17);
    
    public final int value;
    
    MessageTag(int i) {
        value = i;
    }
    
    public static MessageTag FromInt(int i) throws CoseException {
        for (MessageTag m : MessageTag.values()) {
            if (i == m.value) return m;
        }
        throw new CoseException("Not a message tag number");
    }
}
