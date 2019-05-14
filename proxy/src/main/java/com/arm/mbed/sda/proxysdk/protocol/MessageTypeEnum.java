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
