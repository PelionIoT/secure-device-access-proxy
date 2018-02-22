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
import java.util.HashMap;
import java.util.Map;

public class AccessTokens implements Serializable {

    private HashMap tokenHolders = new HashMap();

    public void setNewToken(String accessTokenName, String accessTokenData) {
        tokenHolders.put(accessTokenName, accessTokenData);
    }

    public String getToken(String accessTokenName) {
        return (String) tokenHolders.get(accessTokenName);
    }

    public void setAccessTokensMap(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        Map<String,Object> map = new HashMap<String,Object>();
        tokenHolders = (HashMap) gson.fromJson(jsonObj.toString(), map.getClass());
    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.putAll(tokenHolders);

        return jsonString;
    }

    public HashMap getTokenHolders() {
        return tokenHolders;
    }

//    public void setTokenHolders() {
//        return tokenHolders;
//    }
}
