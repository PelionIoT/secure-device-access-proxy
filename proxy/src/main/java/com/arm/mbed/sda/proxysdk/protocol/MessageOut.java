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
package com.arm.mbed.sda.proxysdk.protocol;

import com.upokecenter.cbor.CBORObject;

public class MessageOut {

    private final MessageTypeEnum type;
    protected final byte[] payload;

    protected MessageOut(MessageTypeEnum type) {
        this.type = type;
        this.payload = null;
    }

    protected MessageOut(MessageTypeEnum type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public byte[] getEncoded() {
        CBORObject cborPayload = CBORObject.NewArray().Untag();
        cborPayload.Add(CBORObject.FromObject(type.getValue()).Untag());
        if (null != payload) {
            cborPayload.Add(CBORObject.FromObject(payload).Untag());
        }
        return cborPayload.EncodeToBytes();
    }

}
