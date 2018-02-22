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

import com.google.gson.Gson;

import org.json.simple.JSONObject;

public class DeviceName {

    String deviceName;

    public DeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceName(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        DeviceName res = gson.fromJson(jsonObj.toString(), DeviceName.class);
        setDeviceName(res.getDeviceName());
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.put("deviceName", deviceName);

        return jsonString;
    }

}
