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

import java.io.Serializable;
import java.util.ArrayList;

public class LedConfiguration implements Serializable {

    private String ledColor;
    private String ledType;

    private ArrayList<String> ledTypes = new ArrayList<String>() {{
        add("POWER");
        add("WIFI");
        add("CLOUD");
        add("FWUP");
        add("LIGHT");
        add("TEMP");
        add("HUMIDITY");
        add("SOUND");
    }};

    private ArrayList<String> ledColors = new ArrayList<String>() {{
        add("GREEN");
        add("RED");
        add("ORANGE");
        add("PURPLE");
        add("SNOW_YELLOW");
    }};

    public LedConfiguration() {
        ledColor = ledColors.get(3);
        ledType = ledTypes.get(3);
    }

    public LedConfiguration(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        LedConfiguration res = gson.fromJson(jsonObj.toString(), LedConfiguration.class);
        setLedColor(res.ledColor);
        setLedType(res.ledType);
    }

    public String getLedColor() {
        return ledColor;
    }

    public int getLedColorIndex() {
        return ledColors.indexOf(ledColor);
    }

    public void setLedColor(String ledColor) {
        this.ledColor = ledColor;
    }

    public String getLedType() {
        return ledType;
    }

    public int getLedTypeIndex() {
        return ledTypes.indexOf(ledType);
    }

    public void setLedType(String ledType) {
        this.ledType = ledType;
    }

    public ArrayList<String> getLedTypes() {
        return ledTypes;
    }

    public ArrayList<String> getLedColors() {
        return ledColors;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.put("ledColor", ledColor);
        jsonString.put("ledType", ledType);

        return jsonString;
    }
}
