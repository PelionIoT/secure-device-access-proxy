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
package com.arm.mbed.dbauth.proxysdk;

import com.arm.mbed.dbauth.proxysdk.utils.SystemUtil;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.AndroidFileKeyStore;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.FileKeyStore;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.IKeyStore;

public class KeyStoreFactory {

    // Single instance
    private static IKeyStore keyStore;

    /**
     * ---[ Get KeyStore ]---
     */
    public synchronized static IKeyStore getKeyStore(String path) {
        if (null == keyStore) {
            keyStore = determineKeyStore(path);
        }
        return keyStore;
    }

    /**
     * Determine KeyStore per OS.
     */
    private static IKeyStore determineKeyStore(String path) {
        IKeyStore keyStore;

        if (SystemUtil.isAndroid()) {
            if (null == path || path.isEmpty()) {
                throw new ProxyException("KeyStoreFactory - keystore path is null");
            }
            keyStore = new AndroidFileKeyStore(path);
        }
        else {
            keyStore = new FileKeyStore(path);
        }

        return keyStore;
    }

}
