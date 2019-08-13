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
package com.arm.mbed.sda.proxysdk;

import com.arm.mbed.sda.proxysdk.utils.SystemUtil;
import com.arm.mbed.sda.proxysdk.utils.crypto.AndroidFileKeyStore;
import com.arm.mbed.sda.proxysdk.utils.crypto.FileKeyStore;
import com.arm.mbed.sda.proxysdk.utils.crypto.IKeyStore;

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
