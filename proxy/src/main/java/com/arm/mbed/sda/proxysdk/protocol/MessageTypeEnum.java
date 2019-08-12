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

public enum MessageTypeEnum {
    INVALID(0),
    NONCE_REQUEST(1),
    NONCE_RESPONSE(2),
    OPERATION_REQUEST(3),
    OPERATION_RESPONSE(4),
    ERROR_RESPONSE(5);

    private int value;

    MessageTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageTypeEnum parse(int value) {
        MessageTypeEnum type = INVALID;
        for (MessageTypeEnum item : MessageTypeEnum.values()) {
            if (item.getValue() == value) {
                type = item;
                break;
            }
        }
        return type;
    }

}
