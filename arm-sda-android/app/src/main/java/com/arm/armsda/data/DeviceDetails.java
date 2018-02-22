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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.io.Serializable;

public class DeviceDetails implements Serializable {

    private String deviceIp;
    private String devicePort;

    public DeviceDetails(String deviceIp, String devicePort) {
        this.deviceIp = deviceIp;
        this.devicePort = devicePort;
    }

    public DeviceDetails(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        DeviceDetails res = gson.fromJson(jsonObj.toString(), DeviceDetails.class);
        setDeviceIp(res.deviceIp);
        setDevicePort(res.devicePort);
    }

    public String getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(String devicePort) {
        this.devicePort = devicePort;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.put("deviceIp", deviceIp);
        jsonString.put("devicePort", devicePort);

        return jsonString;
    }

    @Override
    public String toString() {
        return "ApiGwLoginDetails [deviceIp=" + deviceIp + ", devicePort=" + devicePort
                + "]";
    }

    public boolean isEmpty() {

        if (StringUtils.isEmpty(deviceIp) || StringUtils.isEmpty(devicePort)) {
            return true;
        }

        return false;
    }

}
