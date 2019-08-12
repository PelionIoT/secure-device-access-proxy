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

public class OperationResponse extends ResponseBase {

	private byte[] blob = null;
	
    public OperationResponse(MessageIn msg) {
        super(msg);
        if (getResponseStatus() == ProtocolConstants.STATUS_OK) {
            CBORObject blobCbor = msg.cborPayload.get(ResponseFieldEnum.FIELD_BLOB.getValueAsCbor());
            if (null != blobCbor) {
            	try {
            		this.blob = blobCbor.GetByteString();
            	} catch(Exception ex) {
            		throw new ProtocolException("Encoded response message blob is invalid: " + ex.getMessage());
            	}
            }
        }
    }

    public byte[] getBlob() {
        if (getResponseStatus() != ProtocolConstants.STATUS_OK) {
            throw new ProtocolException("Encoded response message status is: " + getResponseStatus());
        }
    	return this.blob;
    }
}
