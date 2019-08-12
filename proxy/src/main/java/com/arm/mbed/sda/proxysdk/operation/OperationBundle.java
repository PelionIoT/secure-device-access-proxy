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
