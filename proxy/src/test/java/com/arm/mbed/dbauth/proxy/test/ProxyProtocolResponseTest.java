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
package com.arm.mbed.dbauth.proxy.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.arm.mbed.dbauth.proxysdk.protocol.MessageIn;
import com.arm.mbed.dbauth.proxysdk.protocol.MessageTypeEnum;
import com.arm.mbed.dbauth.proxysdk.protocol.NonceResponse;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationResponse;
import com.upokecenter.cbor.CBORObject;

public class ProxyProtocolResponseTest {

    private final static long noncePayload = 1234L;

    @Test
    public void nonceResponseTest() {
        CBORObject expectedNonce = CBORObject.FromObject(1234L);

        byte[] response = ResponseMessageFactory.createNonceResponseMessage(0, noncePayload);
        MessageIn msgIn = new MessageIn(response);
        Assert.assertTrue(msgIn.getType().equals(MessageTypeEnum.NONCE_RESPONSE));
        NonceResponse nonceRspns = new NonceResponse(msgIn);
        Assert.assertTrue(nonceRspns.getResponseStatus() == 0);
        Assert.assertEquals(nonceRspns.getNonce(), expectedNonce);
    }

    @Test
    public void operationResponseTest() {
        byte[] response = ResponseMessageFactory.createOperationResponseMessage(0);
        MessageIn msgIn = new MessageIn(response);
        Assert.assertTrue(msgIn.getType().equals(MessageTypeEnum.OPERATION_RESPONSE));
        OperationResponse nonceRspns = new OperationResponse(msgIn);
        Assert.assertTrue(nonceRspns.getResponseStatus() == 0);
    }

}
