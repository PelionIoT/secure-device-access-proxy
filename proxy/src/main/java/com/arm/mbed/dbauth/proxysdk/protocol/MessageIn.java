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
package com.arm.mbed.dbauth.proxysdk.protocol;

import org.spongycastle.util.encoders.Base64;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

public class MessageIn implements IMessageIn {

    private final MessageTypeEnum type;
    protected final CBORObject cborPayload;

    public MessageIn(byte[] msg) {

        cborPayload = CBORObject.DecodeFromBytes(msg);
        if (!cborPayload.getType().equals(CBORType.Array)) {
            throw new ProtocolException("Encoded message payload is not a CBOR array");
        }
        if (cborPayload.getValues().isEmpty()) {
            throw new ProtocolException("Encoded message payload is an empty CBOR array");
        }

        int typeInt = cborPayload.get(0).AsInt32();
        type = MessageTypeEnum.parse(typeInt);
        if (type.equals(MessageTypeEnum.INVALID)) {
            throw new ProtocolException("Encoded message type " + typeInt + " is invalid");
        }
        System.out.println("Got a device response of type " + type.name() + ", in HEX: " +
                Base64.toBase64String(cborPayload.EncodeToBytes()));
    }

    @Override
    public MessageTypeEnum getType() {
        return type;
    }

    @Override
    public CBORObject getPayload() {
        return cborPayload;
    }

}
