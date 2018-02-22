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
import com.arm.armsda.data.DeviceDetails;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.tcp.TcpDevice;
import com.arm.armsda.data.CommandConstants;
import com.arm.mbed.dbauth.proxysdk.IDevice;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.dbauth.proxysdk.operation.OperationArgumentType;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationResponse;

import org.json.simple.JSONObject;

public class SendMessageActivity extends AppCompatActivity {

    private IDevice device;
    private TextView responseStatus;
    private TextView responseType;
    private DeviceDetails deviceDetails;
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String keyValue = "sendMessageDetails";
    private static final String TokenSharedRefKey = "AccessTokens";
    private AccessTokens accessTokens = new AccessTokens();
    private EditText inputAccessTokenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        //Setting Views and Widgets
        responseStatus = findViewById(R.id.ResponseStatusResult);
        responseType = findViewById(R.id.ResponseTypeResult);
        Button sendMsgButton = findViewById(R.id.SendMsgButton);
        EditText deviceIpEditText = findViewById(R.id.DeviceIpInput);
        EditText devicePortEditText = findViewById(R.id.DevicePortInput);
        inputAccessTokenName = findViewById(R.id.ChooseTokenInput);

        //Retrieve device details from SharedRef
        JSONObject jsonStored = dataHandler.getJsonStringData(
                keyValue,
                SendMessageActivity.this);

        //Set device details from SharedRef if exist
        if (null != jsonStored) {
            deviceDetails = new DeviceDetails(jsonStored);

            if  (!deviceDetails.isEmpty()) {
                deviceIpEditText.setText(deviceDetails.getDeviceIp());
                devicePortEditText.setText(deviceDetails.getDevicePort());
            }
        }

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get the Access token map
                JSONObject jsonStored = dataHandler.getJsonStringData(
                        TokenSharedRefKey,
                        SendMessageActivity.this);
                accessTokens.setAccessTokensMap(jsonStored);

                //Check if the user input key exists
                String accessToken = accessTokens.getToken(inputAccessTokenName.getText().toString());
                if (null == accessToken) {
                    return;
                }

                deviceDetails = new DeviceDetails(
                        deviceIpEditText.getText().toString(),
                        devicePortEditText.getText().toString()
                );

                //If details changed before pressing the Go button - save the changes
                dataHandler.saveJsonStringData(
                        keyValue,
                        deviceDetails.toJsonObject(),
                        SendMessageActivity.this);

                //Prepare device
                device = new TcpDevice(
                        deviceDetails.getDeviceIp(),
                        Integer.parseInt(deviceDetails.getDevicePort()));

                //Call to sent message Async thread
                new sendMessageToDevice().execute(accessToken);
            }
        });

    }

    public class sendMessageToDevice extends AsyncTask<String, Void, OperationResponse> {

        @Override
        protected OperationResponse doInBackground(String... params) {

            String accessToken = params[0];
            OperationResponse op = null;

            ParamElement[] cmdParams = {
                    new ParamElement(OperationArgumentType.STR, "FWUP"),
                    new ParamElement(OperationArgumentType.STR, "SNOW_YELLOW")
            };

            try {
                op = SecuredDeviceAccess.sendMessage(
                        accessToken,
                        CommandConstants.CONFIGURE,
                        cmdParams,
                        device);
            } catch(Exception e) {
                System.out.println("");
            }

            return op;
        }

        @Override
        protected void onPostExecute(OperationResponse result) {
            if (null != result ) {
                responseStatus.setText(String.valueOf(result.getResponseStatus()));
                responseType.setText(String.valueOf(result.getType().getValue()));
            } else {
                responseStatus.setText(R.string.socket_timeout);
                responseType.setText("N/A");
            }
        }
    }

}
