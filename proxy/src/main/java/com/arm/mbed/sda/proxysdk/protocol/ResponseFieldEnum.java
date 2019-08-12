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

public enum ResponseFieldEnum {
    INVALID(0),
    FIELD_TYPE(1),
    FIELD_STATUS(2),
    FIELD_NONCE(3),
    FIELD_BLOB(4);

    private int value;

    ResponseFieldEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public CBORObject getValueAsCbor() {
        return CBORObject.FromObject(value).Untag();
    }

    public static ResponseFieldEnum parse(int value) {
        ResponseFieldEnum type = INVALID;
        for (ResponseFieldEnum item : ResponseFieldEnum.values()) {
            if (item.getValue() == value) {
                type = item;
                break;
            }
        }
        return type;
    }

}
