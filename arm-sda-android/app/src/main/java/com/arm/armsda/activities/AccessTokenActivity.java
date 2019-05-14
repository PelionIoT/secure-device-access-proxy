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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.ApplicationData;
import com.arm.armsda.data.DeviceName;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.serial.DeviceConnection;
import com.arm.armsda.settings.ActionBarDrawerActivity;
import com.arm.armsda.utils.AndroidUtils;
import com.arm.armsda.data.CommandConstants;
import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.sda.proxysdk.http.CreateAccessTokenRequest;
import com.arm.mbed.sda.proxysdk.http.HttpErrorResponseException;
import com.arm.mbed.sda.proxysdk.server.UserPasswordServer;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccessTokenActivity extends ActionBarDrawerActivity {

    //Views
    private UserPasswordServer mAuthServer;
    private CheckBox mFirstCheckBox;
    private CheckBox mSecondCheckBox;
    private CheckBox mThirdCheckBox;
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

    private DeviceConnection dv = new DeviceConnection();
    private String demoProfile;
    private static final String appDataSharedPreferencesKeyValue = "appDataDetails";
    private ApplicationData applicationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting Drawer
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_access_token, null, false);
        mDrawerLayout.addView(contentView, 0);

        Log.d("","");

        mAuthServer = (UserPasswordServer)getIntent().getSerializableExtra("authServer");
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFirstCheckBox = findViewById(R.id.ReadDataCheckBox);
        mSecondCheckBox = findViewById(R.id.ConfigureCheckBox);
        mThirdCheckBox = findViewById(R.id.UpdateCheckBox);
        mDeviceNameEditText = findViewById(R.id.DeviceNameEditText);

        mGetPermissionsButton = findViewById(R.id.GetPermissionsButton);
        mGetPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String scope;
                StringBuffer scopeSb = new StringBuffer();

                //Must stay the first one.
                if (mFirstCheckBox.isChecked()) {
                    if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
                        scopeSb.append(CommandConstants.RESTART);
                    } else {
                        scopeSb.append(CommandConstants.READ_DATA);
                    }
                }

                if (mSecondCheckBox.isChecked()) {
                    if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
                        if (!StringUtils.isEmpty(scopeSb)) {
                            scopeSb.append(" " + CommandConstants.DIAGNOSTICS);
                        } else {
                            scopeSb.append(CommandConstants.DIAGNOSTICS);
                        }
                    } else {
                        if (!StringUtils.isEmpty(scopeSb)) {
                            scopeSb.append(" " + CommandConstants.CONFIGURE);
                        } else {
                            scopeSb.append(CommandConstants.CONFIGURE);
                        }
                    }
                }

                if (mThirdCheckBox.isChecked()) {
                    if (!StringUtils.isEmpty(scopeSb)) {
                        scopeSb.append(" " + CommandConstants.UPDATE);
                    } else {
                        scopeSb.append(CommandConstants.UPDATE);
                    }
                }

                if (StringUtils.isEmpty(scopeSb)) {
                    Context context = AccessTokenActivity.this;
                    AndroidUtils.customToast(context,
                            "Please choose one or more permissions",
                            Color.BLUE);
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
    protected void onResume() {

        loadAppConfiguration();
        dv.registerDevice(this);

        if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
            mFirstCheckBox.setText(R.string.restart_button);
            mSecondCheckBox.setText(R.string.diagnostics_button);
        } else {
            mFirstCheckBox.setText(R.string.read_data_button);
            mSecondCheckBox.setText(R.string.configure_button);
        }
        mFirstCheckBox.setChecked(false);
        mSecondCheckBox.setChecked(false);
        mThirdCheckBox.setChecked(false);

        super.onResume();
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
        dv.unregisterDevice(this);
        super.onStop();
    }

    private void loadAppConfiguration() {

        JSONObject jsonStored =  dataHandler.getJsonStringData(
                appDataSharedPreferencesKeyValue,
                AccessTokenActivity.this);

        if (null != jsonStored) {
            applicationData = new ApplicationData(jsonStored);
            demoProfile = applicationData.getDemoMode();
        } else {
            Log.d("","Stored app data is empty");
        }
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
                res = "Error: Please check your Internet connection or URL configuration";
                Log.d("AccessTokenActivity", "exception: " + e.getMessage());
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
