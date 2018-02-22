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
package com.arm.armsda.oldies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.ApiGwLoginDetails;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.data.CommandConstants;
import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.dbauth.proxysdk.http.CreateAccessTokenRequest;
import com.arm.mbed.dbauth.proxysdk.http.HttpErrorResponseException;
import com.arm.mbed.dbauth.proxysdk.server.IAuthServer;
import com.arm.mbed.dbauth.proxysdk.server.UserPasswordServer;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OldAccessTokenActivity extends AppCompatActivity {

    private static IAuthServer authServer;
    private TextView accessTokenResult;
    private String resultToken;
    private ApiGwLoginDetails apiGwLoginDetails;
    private static final String baseUrl = "https://lab-api.mbedcloudintegration.net";
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String keyValue = "apiGwLoginDetails";
    private static final String TokenSharedRefKey = "AccessTokens";
    private AccessTokens accessTokens = new AccessTokens();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zold_activity_access_token);

        //Setting Views and Widgets
        Button goButton = findViewById(R.id.GoButton);
        Button saveButton = findViewById(R.id.SaveTokenButton);
        accessTokenResult = findViewById(R.id.AccessTokenResult);
        EditText usernameEditText = (findViewById(R.id.UsernameInput));
        EditText passwordEditText = (findViewById(R.id.PasswordInput));
        EditText accountIdEditText = (findViewById(R.id.AccountIdInput));
        EditText saveTokenEditText = (findViewById(R.id.TokenNameInput));

        //Retrieve login details from SharedRef
        JSONObject jsonStored =  dataHandler.getJsonStringData(
                keyValue,
                OldAccessTokenActivity.this);

        //Set login details from SharedRef if exist
        if (null != jsonStored) {
            apiGwLoginDetails = new ApiGwLoginDetails(jsonStored);

            if (!apiGwLoginDetails.isEmpty()) {
                usernameEditText.setText(apiGwLoginDetails.getUseranme());
                passwordEditText.setText(apiGwLoginDetails.getPassword());
                accountIdEditText.setText(apiGwLoginDetails.getAccountId());
            }
        }

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                apiGwLoginDetails = new ApiGwLoginDetails(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        accountIdEditText.getText().toString());

                //If details changed before pressing the Go button - save the changes
                dataHandler.saveJsonStringData(
                        keyValue,
                        apiGwLoginDetails.toJsonObject(),
                        OldAccessTokenActivity.this);

                //Prepare authServer

                authServer = new UserPasswordServer(
                        baseUrl,
                        apiGwLoginDetails.getAccountId(),
                        apiGwLoginDetails.getUseranme(),
                        apiGwLoginDetails.getPassword());

                //Prepare CreateCborWebTokenRequest
                CreateAccessTokenRequest request = new CreateAccessTokenRequest();
                List<String> l = new ArrayList<>();
                l.add("endpoint-name:dev1");
                request.setAudience(l);
                request.setScope(CommandConstants.CONFIGURE);

                //Call to get access token Async thread
                new httpCallAccessToken().execute(request);
            }
        });

        //Save token
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Retrieve The access tokens map
                JSONObject jsonStored = dataHandler.getJsonStringData(
                        TokenSharedRefKey,
                        OldAccessTokenActivity.this);

                //If tokens list is not empty update -accessTokens- class
                if (null != jsonStored) {
                    accessTokens.setAccessTokensMap(jsonStored);
                }

                accessTokens.setNewToken(saveTokenEditText.getText().toString(), resultToken);
                dataHandler.saveJsonStringData(
                        TokenSharedRefKey,
                        accessTokens.toJsonObject(),
                        OldAccessTokenActivity.this);
            }
        });

    }

    public class httpCallAccessToken extends AsyncTask<CreateAccessTokenRequest, Void, String> {

        @Override
        protected String doInBackground(CreateAccessTokenRequest... params) {

            CreateAccessTokenRequest request  = params[0];
            String res;

            try {
                res = SecuredDeviceAccess.getAccessToken(
                        authServer,
                        request.getAudience(),
                        request.getScope());

                return res;
            } catch (ProxyException e) {
                res = e.getMessage();
                return res;
            } catch (HttpErrorResponseException e) {
                res = "Error: " + e.getHttpErrorStatusCode() + "\nMessage: " + e.getHttpErrorMessage();
                return res;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            //Returning Access token to main Activity
            accessTokenResult.setText(result);
            resultToken = result;
        }
    }

}
