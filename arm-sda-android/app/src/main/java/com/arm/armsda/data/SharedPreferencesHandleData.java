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

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SharedPreferencesHandleData implements IDataHandler  {

    private static final String filename = "SharedPreferencesFile";
    private SharedPreferences sharedPref;

    @Override
    public void saveJsonStringData(String key, JSONObject value, Context context) {
        sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value.toString());
        editor.apply();
    }

    @Override
    public JSONObject getJsonStringData(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        String json = settings.getString(key, null);

        if (StringUtils.isEmpty(json)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            return jsonObject;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
