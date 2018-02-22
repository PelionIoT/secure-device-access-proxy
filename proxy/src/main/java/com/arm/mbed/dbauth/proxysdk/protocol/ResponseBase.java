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

public class ResponseBase implements IResponse {

    protected MessageIn msg;

    protected ResponseBase(MessageIn msg) {
        if (msg.cborPayload.getValues().size() < 1 + ProtocolConstants.MSG_TYPE_INDEX) {
            throw new ProtocolException("Encoded message payload is missing a response status");
        }
        this.msg = msg;
    }

    @Override
    public MessageTypeEnum getType() {
        return msg.getType();
    }

    @Override
    public int getResponseStatus() {
        return msg.cborPayload.get(ProtocolConstants.MSG_TYPE_INDEX).AsInt32();
    }

}
