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

import com.upokecenter.cbor.CBORObject;

public class ResponseBase implements IResponse {

    protected MessageIn msg;
    private int status;

    protected ResponseBase(MessageIn msg) {
        CBORObject statusCbor = msg.cborPayload.get(ResponseFieldEnum.FIELD_STATUS.getValueAsCbor());
        if (null == statusCbor) {
            throw new ProtocolException("Encoded response message is missing the status");
        }
        try {
        	this.status = statusCbor.AsInt32();
        } catch(Exception ex) {
        	throw new ProtocolException("Encoded response message status is invalid: " + ex.getMessage());
        }
        this.msg = msg;
    }

    @Override
    public MessageTypeEnum getType() {
        return msg.getType();
    }

    @Override
    public int getResponseStatus() {
        return this.status;
    }

}
