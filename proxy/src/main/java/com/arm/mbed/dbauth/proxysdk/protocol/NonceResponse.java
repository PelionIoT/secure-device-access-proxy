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

import com.upokecenter.cbor.CBORObject;

public class NonceResponse extends ResponseBase {

    public NonceResponse(MessageIn msg) {
        super(msg);
        if (getResponseStatus() == ProtocolConstants.STATUS_OK &&
                (msg.cborPayload.getValues().size() < 1 + ProtocolConstants.RESPONSE_STATUS_INDEX)) {
            throw new ProtocolException("Encoded nonce response message is missing the nonce");
        }
    }

    public CBORObject getNonce() {
        return  msg.cborPayload.get(ProtocolConstants.RESPONSE_STATUS_INDEX);
    }

}
