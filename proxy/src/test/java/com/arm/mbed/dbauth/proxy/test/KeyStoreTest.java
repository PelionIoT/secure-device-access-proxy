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
package com.arm.mbed.dbauth.proxy.test;

import java.security.KeyPair;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.arm.mbed.dbauth.proxysdk.KeyStoreFactory;
import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.CryptoConstants;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.EccUtils;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.IKeyStore;

public class KeyStoreTest {

    @Test
    public void useKeys() {

        IKeyStore keyStore = KeyStoreFactory.getKeyStore(null);
        KeyPair keyPair = EccUtils.eccKeyPairGeneration(CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE);

        keyStore.size();

        String alias = "myKeys";

        keyStore.setKeyEntry(alias, keyPair);

        keyStore.getKeyPair(alias); 

        try {
            keyStore.setKeyEntry("", keyPair);
        } catch (ProxyException e) {
            Assert.assertEquals("Alias is Empty or Null", e.getMessage());
            System.out.println("Passed"); 
        }

        keyStore.getKeyPair(alias);

        Assert.assertTrue(keyStore.containsKey(alias));

        Assert.assertFalse(keyStore.containsKey("a"));

        Assert.assertFalse(keyStore.isEmpty());

        keyStore.deleteEntry(alias);
    }

}
