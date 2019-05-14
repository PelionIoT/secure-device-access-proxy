package com.arm.mbed.aceauth.test.tests;

import java.security.Security;

import org.spongycastle.jce.provider.BouncyCastleProvider;

public class BaseTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
