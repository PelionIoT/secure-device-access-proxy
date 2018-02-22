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
package com.arm.mbed.dbauth.proxysdk.utils.crypto;

import java.security.KeyPair;

public class ProxyKeysHelper implements IProxyKeysHelper {

    private IKeyStore fileKeyStore;

    public ProxyKeysHelper(IKeyStore fileKeyStore) {
        this.fileKeyStore = fileKeyStore;
    }

    //Generates new key pair with alias and store in keystore
    @Override
    public void storePopKeyPair(String alias, KeyPair keyPair) {
        fileKeyStore.setKeyEntry(alias, keyPair);
    }

    @Override
    public KeyPair getKeyPairFromKeyStore(String alias) {
        return fileKeyStore.getKeyPair(alias);
    }

    @Override
    public void deleteKeyPair(String alias) {

        fileKeyStore.deleteEntry(alias);
    }

	@Override
	public KeyPair generateKeyPair() {
        return EccUtils.eccKeyPairGeneration(CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE);
	}

}
