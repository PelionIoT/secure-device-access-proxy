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

import org.spongycastle.util.encoders.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.arm.mbed.dbauth.proxysdk.operation.Operation;
import com.arm.mbed.dbauth.proxysdk.operation.OperationArgumentType;
import com.arm.mbed.dbauth.proxysdk.operation.OperationBundle;
import com.arm.mbed.dbauth.proxysdk.operation.OperationTypeEnum;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.NonceRequest;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationRequest;
import com.upokecenter.cbor.CBORObject;

public class ProxyProtocolRequestTest {

    private final static String accessTokenB64 = "2D3ShEOhASagWQE5qQxrdHVybi1sZWQtb24YGaEBpCJYIGCsD9h0lnHVrm+Kep6QuxHF0Y7VG/FZZ85eKraw9ujzAQIhWCCpd2miVfz3BnbysdZOkgqWQnagbsMjBcK5EoTnadIloSABAngecG9wLXB1YmxpYy1rZXkgRVEgdHJ1c3RfYW5jaG9yA3hWZGV2aWNlLWlkOmY5MGIxMDE3ZTUyZjRjNzBhZDkyNjg0ZTgwMmM5MjQ5LGRldmljZS1pZDogYWJjZGVmYWJjZGVmYWJjZGVmYWJjZGVmYWJjZGVmYWIEGphbqX8FGlnQMAEGGln+xH0BeDs0NjpDMjpDNzpGMjpFNjowMDo0QTpEQjo5NDpCRTpDMjo0RTowNTpENDoxNjo4NjpBMDo4RDo5RjozMwdQAV+LL4u4UHudSK2yAAAAAFhAC2lab5P2MPxHJrxSb32uKdocfpspJTKnx1z7kY4JVhxfmJzNIWVlW/DWqqXTBsy/TIimNnC/wiU5tBytClamtw==";
    private final static long noncePayload = 1234L;

    @Test
    public void nonceRequestTest() {
        NonceRequest request = new NonceRequest();
        byte[] bytes = request.getEncoded();
        Assert.assertTrue(RequestMessageValidator.parseNonceRequestMessage(bytes));
    }

    @Test
    public void operationRequestTest() {

        CBORObject nonce = CBORObject.FromObject(noncePayload);
        ParamElement[] params = {new ParamElement(OperationArgumentType.STR, "Hello World"), new ParamElement(OperationArgumentType.INT, "5")};
        Operation op = new Operation.Builder()
                .setType(OperationTypeEnum.FUNCTION_CALL.getValue())
                .setFunction("lcd-print")
                .setParams(params)               
                .build();
        OperationBundle opb = new OperationBundle.Builder()
                .setToken(Base64.decode(accessTokenB64))
                .setOperation(op)
                .setNonce(nonce)
                .build();
        OperationRequest request = new OperationRequest(opb.getEncoded());
        byte[] bytes = request.getEncoded();
        Assert.assertTrue(RequestMessageValidator.parseOperationRequestMessage(
                bytes, 
                accessTokenB64, 
                op, 
                noncePayload));
    }

}
