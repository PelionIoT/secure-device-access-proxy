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
