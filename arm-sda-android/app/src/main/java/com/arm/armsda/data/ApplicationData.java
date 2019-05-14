package com.arm.armsda.data;

import com.google.gson.Gson;
import org.json.simple.JSONObject;

//application context itself gets destroyed if left unused for a long time in the background.
//I am storing the application data in Shared Preferences
public class ApplicationData {

    public static final String HANNOVER_MESSE = "HANNOVER_MESSE";
    public static final String MWC = "MWC";

    private String demoMode;
    private String accountId;
    private String cloudUrl;;

    public ApplicationData(String demoMode, String accountId, String cloudUrl) {
        this.demoMode = demoMode;
        this.accountId = accountId;
        this.cloudUrl = cloudUrl;
    }

    public ApplicationData(JSONObject jsonObj) {
        if (null == jsonObj) {
            return;
        }
        Gson gson = new Gson();
        ApplicationData res = gson.fromJson(jsonObj.toString(), ApplicationData.class);
        this.demoMode = res.demoMode;
        this.accountId = res.accountId;
        this.cloudUrl = res.cloudUrl;
    }

//    public enum DemoMode {
//        HANNOVER_MESSE("HANNOVER_MESSE", 0),
//        MWC("MWC", 1);
//
//        private String stringValue;
//        private int intValue;
//        private DemoMode(String toString, int value) {
//            stringValue = toString;
//            intValue = value;
//        }
//
//        @Override
//        public String toString() {
//            return stringValue;
//        }
//    }

    public JSONObject toJsonObject() {
        JSONObject jsonString = new JSONObject();
        jsonString.put("demoMode", demoMode);
        jsonString.put("accountId", accountId);
        jsonString.put("cloudUrl", cloudUrl);
        return jsonString;
    }

    public String getDemoMode() {
        return demoMode;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

}
