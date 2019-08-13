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

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

public class MessageIn implements IMessageIn {

    private final MessageTypeEnum type;
    protected final CBORObject cborPayload;

    public MessageIn(byte[] msg) {

        cborPayload = CBORObject.DecodeFromBytes(msg);
        if (!cborPayload.getType().equals(CBORType.Map)) {
            throw new ProtocolException("Encoded input message payload is not a CBOR map");
        }
        CBORObject typeCbor = cborPayload.get(ResponseFieldEnum.FIELD_TYPE.getValueAsCbor());
        if (null == typeCbor) {
            throw new ProtocolException("Encoded input message is missing the type");
        }
        int typeInt;
        try {
        	typeInt = typeCbor.AsInt32();
        } catch(Exception ex) {
        	throw new ProtocolException("Encoded input message type is invalid: " + ex.getMessage());
        }
        this.type = MessageTypeEnum.parse(typeInt);
        if (this.type.equals(MessageTypeEnum.INVALID)) {
            throw new ProtocolException("Encoded input message type " + typeInt + " is invalid");
        }
        System.out.println("Got a device response of type " + this.type.name() + ", in HEX: " +
                Hex.toHexString(msg));
    }

    @Override
    public MessageTypeEnum getType() {
        return this.type;
    }

    @Override
    public CBORObject getPayload() {
        return this.cborPayload;
    }

}
