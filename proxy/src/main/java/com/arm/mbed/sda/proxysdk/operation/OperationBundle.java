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
package com.arm.mbed.sda.proxysdk.operation;

import org.apache.commons.lang3.ArrayUtils;

import com.upokecenter.cbor.CBORObject;

public class OperationBundle {

    private byte[] token;
    private CBORObject nonce;
    private Operation operation;

    private OperationBundle(byte[] token, CBORObject nonce, Operation operation) {
        this.token = token;
        this.nonce = nonce;
        this.operation = operation;
    }

    public byte[] getEncoded() {
        CBORObject cborOpB = CBORObject.NewArray();
        cborOpB.Add(nonce);
        cborOpB.Add(operation.getCBOREncoded());
        cborOpB.Add(CBORObject.FromObject(token));

        return cborOpB.EncodeToBytes();
    }

    public static class Builder {
        private byte[] token;
        private CBORObject nonce;
        private Operation operation;

        public Builder setToken(byte[] token) {
            this.token = token;
            return this;
        }

        public Builder setNonce(CBORObject nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder setOperation(Operation operation) {
            this.operation = operation;
            return this;
        }

        public OperationBundle build() {
            validate();

            return new OperationBundle(token, nonce, operation);
        }

        private void validate() {
            if (ArrayUtils.isEmpty(token) ||
                    nonce.isZero() ||
                    null == operation) {
                throw new OperationException("OperationBundle token and/or nonce and/or operation is missing");
            }
        }
    }

}
