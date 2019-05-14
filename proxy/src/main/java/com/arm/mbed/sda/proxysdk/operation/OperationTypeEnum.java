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

public enum OperationTypeEnum {

    INVALID(0),
    FUNCTION_CALL(1);

    private int value;

    private OperationTypeEnum(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public static OperationTypeEnum parse(int value) {
        OperationTypeEnum type = INVALID;
        for (OperationTypeEnum item : OperationTypeEnum.values()) {
            if (item.getValue() == value) {
                type = item;
                break;
            }
        }
        return type;
    }

}
