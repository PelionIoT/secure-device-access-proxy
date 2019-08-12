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
package com.arm.mbed.aceauth.cose;

import com.upokecenter.cbor.CBORObject;

public class CoseConstants {

    public static final int COSE_KEY_NAME               = 1;    // "COSE_Key"
    public static final int KEY_TYPE_KEY                = 1;
    public static final int KEY_TYPE                    = 2;    // "EC"
    public static final int CURVE_KEY                   = -1;
    public static final int CURVE                       = 1;    // "P-256"
    public static final int X_KEY                       = -2;
    public static final int Y_KEY                       = -3;
    public static final int EC_PUBLIC_KEY_BYTE_LENGTH   = 32;
    
    public enum HeaderKeys {
        Algorithm(1);

        private CBORObject value;

        HeaderKeys(int val) {
            this.value = CBORObject.FromObject(val);
        }

        public CBORObject AsCBOR() {
            return value;
        }
    }

    public enum AlgorithmID {
        ECDSA_256(-7);

        private final CBORObject value;

        AlgorithmID(int value) {
            this.value = CBORObject.FromObject(value);
        }    

        public CBORObject AsCBOR() {
            return value;
        }
    }

    public enum MessageTag {
        Unknown(0),
        Sign1(18);

        private final int value;

        MessageTag(int i) {
            value = i;
        }

        public int value() {
        	return value;
        }
        
        public static MessageTag FromInt(int i) throws CoseException {
            for (MessageTag m : MessageTag.values()) {
                if (i == m.value) return m;
            }
            throw new CoseException("Not a message tag number");
        }
    }
}
