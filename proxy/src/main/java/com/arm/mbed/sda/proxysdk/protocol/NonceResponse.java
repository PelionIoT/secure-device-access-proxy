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
package com.arm.mbed.sda.proxysdk.protocol;

import com.upokecenter.cbor.CBORObject;

public class NonceResponse extends ResponseBase {

	private CBORObject nonceCbor;
	
    public NonceResponse(MessageIn msg) {
        super(msg);
        if (getResponseStatus() == ProtocolConstants.STATUS_OK) {
            this.nonceCbor = msg.cborPayload.get(ResponseFieldEnum.FIELD_NONCE.getValueAsCbor());
            if (null == this.nonceCbor) {
                throw new ProtocolException("Encoded nonce response message is missing the nonce");
            }
        }
    }

    public CBORObject getNonce() {
        if (getResponseStatus() != ProtocolConstants.STATUS_OK) {
            throw new ProtocolException("Encoded nonce response message status is: " + getResponseStatus());
        }
        return this.nonceCbor;
    }

}
