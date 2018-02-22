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
package com.arm.armsda.data;

import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;

public class SerialDeviceCmd {

    private String command;
    private ParamElement[] commandParams;


    public SerialDeviceCmd(String command, ParamElement[] commandParams) {
        this.command = command;
        this.commandParams = commandParams;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ParamElement[] getCommandParams() {
        return commandParams;
    }

    public void setCommandParams(ParamElement[] commandParams) {
        this.commandParams = commandParams;
    }
}
