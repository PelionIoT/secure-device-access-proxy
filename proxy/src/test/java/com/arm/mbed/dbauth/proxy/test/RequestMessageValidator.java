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

import com.arm.mbed.dbauth.proxysdk.operation.Operation;
import com.arm.mbed.dbauth.proxysdk.operation.OperationArgumentType;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.MessageTypeEnum;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

public class RequestMessageValidator {

    public static boolean parseNonceRequestMessage(byte[] msgPayload) {

        CBORObject cborMsg = CBORObject.DecodeFromBytes(msgPayload);
        if (!cborMsg.getType().equals(CBORType.Array)) {
            return false;
        }
        if (cborMsg.getValues().size() != 1) {
            return false;
        }
        if (cborMsg.get(0).AsInt32() != MessageTypeEnum.NONCE_REQUEST.getValue()) { 
            return false;
        }
        return true;
    }

    public static boolean parseOperationRequestMessage( byte[] msgPayload,
            String expectedAccessToken,
            Operation expectedOp,
            long expectedNonce) {

        CBORObject cborMsg = CBORObject.DecodeFromBytes(msgPayload);
        if (!cborMsg.getType().equals(CBORType.Array)) {
            return false;
        }
        if (cborMsg.getValues().size() != 2) {
            return false;
        }
        if (cborMsg.get(0).AsInt32() != MessageTypeEnum.OPERATION_REQUEST.getValue()) { 
            return false;
        }
        CBORObject payload = cborMsg.get(1);
        if (!payload.getType().equals(CBORType.ByteString)) {
            return false;
        }
        CBORObject cborPayload = CBORObject.DecodeFromBytes(payload.GetByteString());
        if (!cborPayload.getType().equals(CBORType.Array)) {
            return false;
        }
        if (cborPayload.getValues().size() != 3) {
            return false;
        }
        CBORObject cborNonce = cborPayload.get(0);
        if (!cborNonce.getType().equals(CBORType.Number)) {
            return false;
        }
        CBORObject cborOperation = cborPayload.get(1);
        if (!cborOperation.getType().equals(CBORType.Array)) {
            return false;
        }
        CBORObject cborAccessToken = cborPayload.get(2);
        if (!cborAccessToken.getType().equals(CBORType.ByteString)) {
            return false;
        }
        if (null == cborAccessToken || null == cborOperation || null == cborNonce) {
            return false;
        }
        // validate the AccessToken
        String accessToken = Base64.toBase64String(cborAccessToken.GetByteString());
        Assert.assertTrue(accessToken.equals(expectedAccessToken));
        // Validate the Nonce
        long nonce = cborNonce.AsInt64();
        Assert.assertTrue(nonce == expectedNonce);
        // validate the Operation
        if (!cborMsg.getType().equals(CBORType.Array)) {
            return false;
        }
        if ((expectedOp.getParams() == null && cborOperation.getValues().size() != 2) ||
                (expectedOp.getParams() != null && cborOperation.getValues().size() != 3)) {
            return false;
        }
        if (cborOperation.get(0).AsInt32() != expectedOp.getType()) {
            return false;
        }
        if (!cborOperation.get(1).AsString().equals(expectedOp.getFunction())) {
            return false;
        }
        if (expectedOp.getParams() != null) {
            CBORObject cborOpParams = cborOperation.get(2);
            if (!cborOpParams.getType().equals(CBORType.Array)) {
                return false;
            }
            if (expectedOp.getParams().length != cborOpParams.getValues().size()) {
                return false;
            }
            Object[] params = cborOpParams.getValues().toArray();
            for (int i = 0; i < expectedOp.getParams().length; i ++) {
            	ParamElement expectedParam = expectedOp.getParams()[i];
            	CBORObject o = (CBORObject)params[i];
            	
            	if (expectedParam.getType().equals(OperationArgumentType.INT)) {
            		if (expectedParam.getIntValue() != o.AsInt32()) return false;
            	}
            	if (expectedParam.getType().equals(OperationArgumentType.STR)) {
            		if  (!expectedParam.getStringValue().equals(o.AsString())) return false;
            	}            	
            }
        }
        return true;
    }

}
