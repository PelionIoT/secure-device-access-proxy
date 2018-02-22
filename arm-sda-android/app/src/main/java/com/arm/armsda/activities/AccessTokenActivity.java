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
package com.arm.armsda.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.DeviceName;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.utils.AndroidUtils;
import com.arm.armsda.data.CommandConstants;
import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.dbauth.proxysdk.http.CreateAccessTokenRequest;
import com.arm.mbed.dbauth.proxysdk.http.HttpErrorResponseException;
import com.arm.mbed.dbauth.proxysdk.server.UserPasswordServer;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccessTokenActivity extends AppCompatActivity {

    //Views
    private UserPasswordServer mAuthServer;
    private CheckBox mReadDataCheckBox;
    private CheckBox mConfigureCheckBox;
    private CheckBox mUpdateCheckBox;
    private Button mGetPermissionsButton;
    private EditText mDeviceNameEditText;

    //Shared Ref
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String sharedPreferencesKeyValue = "AccessTokens";
    private static final String deviceNameSharedPreferencesKeyValue = "DeviceName";
    private AccessTokens accessTokens = new AccessTokens();
    private final static String accessTokenKeyNameInMap = "AccessToken";
    private DeviceName deviceName;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_token);

        Log.d("","");

        mAuthServer = (UserPasswordServer)getIntent().getSerializableExtra("authServer");
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mReadDataCheckBox = findViewById(R.id.ReadDataCheckBox);
        mConfigureCheckBox = findViewById(R.id.ConfigureCheckBox);
        mUpdateCheckBox = findViewById(R.id.UpdateCheckBox);
        mDeviceNameEditText = findViewById(R.id.DeviceNameEditText);

        mGetPermissionsButton = findViewById(R.id.GetPermissionsButton);
        mGetPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String scope;
                StringBuffer scopeSb = new StringBuffer();

                //Must stay the first one.
                if (mReadDataCheckBox.isChecked()) {
                    scopeSb.append(CommandConstants.READ_DATA);
                }

                if (mConfigureCheckBox.isChecked()) {
                    if (!StringUtils.isEmpty(scopeSb)) {
                        scopeSb.append(" " + CommandConstants.CONFIGURE);
                    } else {
                        scopeSb.append(CommandConstants.CONFIGURE);
                    }
                }

                if (mUpdateCheckBox.isChecked()) {
                    if (!StringUtils.isEmpty(scopeSb)) {
                        scopeSb.append(" " + CommandConstants.UPDATE);
                    } else {
                        scopeSb.append(CommandConstants.UPDATE);
                    }
                }

                    if (StringUtils.isEmpty(scopeSb)) {
                    return;
                }

                scope = scopeSb.toString();
                //TODO:: check if need to remove the first space
                //scope = scope.startsWith(" ") ? scope.substring(1) : scope;

                //Prepare CreateCborWebTokenRequest
                CreateAccessTokenRequest request = new CreateAccessTokenRequest();
                request.setScope(scope);

                //Call to get access token Async thread
                new AccessTokenActivity.httpCallAccessToken().execute(request);

            }
        });

        getDeviceNameFromSharedRef();
        if (deviceName == null) {
            mDeviceNameEditText.setText(R.string.device_ep);
        }
    }

    @Override
    protected void onStop()
    {
        deviceName = new DeviceName(mDeviceNameEditText.getText().toString());

        if (null == deviceName || !StringUtils.isEmpty(deviceName.getDeviceName())) {
            //If details changed before pressing the Go button - save the changes
            dataHandler.saveJsonStringData(
                    deviceNameSharedPreferencesKeyValue,
                    deviceName.toJsonObject(),
                    AccessTokenActivity.this);
        }
        super.onStop();
    }

    public class httpCallAccessToken extends AsyncTask<CreateAccessTokenRequest, Void, String> {

        @Override
        protected String doInBackground(CreateAccessTokenRequest... params) {

            CreateAccessTokenRequest request  = params[0];
            String res;

            if (null == mAuthServer) {
                return "Authorization details are null, unexpected Error";
            }

            deviceName = new DeviceName(mDeviceNameEditText.getText().toString());
            if (StringUtils.isEmpty(deviceName.getDeviceName())) {
                return "Error: Device name cannot be empty.";
            }

            //If details changed before pressing the Go button - save the changes
            dataHandler.saveJsonStringData(
                    deviceNameSharedPreferencesKeyValue,
                    deviceName.toJsonObject(),
                    AccessTokenActivity.this);

            List<String> audienceList = new ArrayList<>();
            String audience = "ep:" + deviceName.getDeviceName();
            audienceList.add(audience);
            request.setAudience(audienceList);


            try {
                res = SecuredDeviceAccess.getAccessToken(
                        mAuthServer,
                        request.getAudience(),
                        request.getScope());

                return res;
            } catch (ProxyException e) {
                res = "Error: Ooops, Something Bad Happened: " + e.getMessage();
                return res;
            } catch (HttpErrorResponseException e) {
                res = "Error: " + e.getHttpErrorStatusCode() + "\nMessage: " + e.getHttpErrorMessage();
                return res;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            //Returning Access token to main Activity

            Context context = AccessTokenActivity.this;

            if (!result.startsWith("Error:")) {

                AndroidUtils.customToast(context, "Get Permission finished Successfully", Color.GREEN);

                //Save the result token to shared ref
                saveAccessToken(result);

                //Go To the Serial activity
                Intent sendMessageUsbActivityIntent = new Intent(context, SendMessageUsbActivity.class);
                startActivityForResult(sendMessageUsbActivityIntent, 1);
            } else {
                AndroidUtils.customToast(context, "Get Permission Failed!", Color.RED);
                AndroidUtils.customToastWithTimer(context, result, Color.GRAY, Toast.LENGTH_LONG);
                return;
            }
        }
    }

    private void saveAccessToken(String accessToken) {
        //Retrieve The access tokens map
        JSONObject jsonStored = dataHandler.getJsonStringData(
                sharedPreferencesKeyValue,
                AccessTokenActivity.this);

        //If tokens list is not empty update -accessTokens- class
        if (null != jsonStored) {
            accessTokens.setAccessTokensMap(jsonStored);
        }

        accessTokens.setNewToken(accessTokenKeyNameInMap, accessToken);
        dataHandler.saveJsonStringData(
                sharedPreferencesKeyValue,
                accessTokens.toJsonObject(),
                AccessTokenActivity.this);

    }

    private void getDeviceNameFromSharedRef() {

        //Retrieve login details from SharedRef
        JSONObject jsonStored =  dataHandler.getJsonStringData(
                deviceNameSharedPreferencesKeyValue,
                AccessTokenActivity.this);

        //Set login details from SharedRef if exist
        if (null != jsonStored) {
            deviceName = new DeviceName(jsonStored);

            if (!StringUtils.isEmpty(deviceName.getDeviceName())) {
                mDeviceNameEditText.setText(deviceName.getDeviceName());
            }
        }
    }

}
