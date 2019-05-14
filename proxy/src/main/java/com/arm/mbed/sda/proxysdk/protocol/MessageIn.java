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
