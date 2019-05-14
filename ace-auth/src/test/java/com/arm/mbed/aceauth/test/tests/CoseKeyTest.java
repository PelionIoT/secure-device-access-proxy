package com.arm.mbed.aceauth.test.tests;

import java.security.KeyPair;
import java.security.PublicKey;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.spongycastle.util.Arrays;

import com.upokecenter.cbor.CBORObject;

import com.arm.mbed.crypto.EccUtils;

import com.arm.mbed.aceauth.cose.CoseUtils;

public class CoseKeyTest extends BaseTest {
    
    @Test
    public void testCoseKeyEncodingDecoding() throws Exception {
        
        KeyPair keyPair = EccUtils.eccKeyPairGeneration();
        PublicKey pubKeyIn = keyPair.getPublic();
        
        CBORObject cborCoseKey = CoseUtils.encodeCoseKey(pubKeyIn);
        PublicKey pubKeyOut = CoseUtils.decodeCoseKey(cborCoseKey);
        
        Assert.assertTrue(Arrays.areEqual(pubKeyIn.getEncoded(), pubKeyOut.getEncoded()));
    }
}
