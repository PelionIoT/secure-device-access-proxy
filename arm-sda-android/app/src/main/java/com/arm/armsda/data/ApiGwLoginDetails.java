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

public class ApiGwLoginDetails implements Serializable {

    private String useranme;
    private String password;
    private String accountId;

    public ApiGwLoginDetails(String useranme, String password, String accountId) {
        this.useranme = useranme;
        this.password = password;
        this.accountId = accountId;
    }

    public ApiGwLoginDetails(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        ApiGwLoginDetails res = gson.fromJson(jsonObj.toString(), ApiGwLoginDetails.class);
        setUseranme(res.useranme);
        setPassword(res.password);
        setAccountId(res.accountId);
    }

    public String getUseranme() {
        return useranme;
    }

    public void setUseranme(String useranme) {
        this.useranme = useranme;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.put("useranme", useranme);
        jsonString.put("password", password);
        jsonString.put("accountId", accountId);

        return jsonString;
    }

    public boolean isEmpty() {

        if (StringUtils.isEmpty(useranme) ||
                StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(accountId)) {
            return true;
        }

        return false;
    }

    public void clean() {
        useranme = "";
        password = "";
        accountId = "";

    }

}
