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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.ApplicationData;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.LedConfiguration;
import com.arm.armsda.data.SerialDeviceCmd;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.serial.DeviceConnection;
import com.arm.armsda.data.CommandConstants;
import com.arm.armsda.settings.ActionBarDrawerActivity;
import com.arm.mbed.sda.proxysdk.IDevice;
import com.arm.mbed.sda.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.sda.proxysdk.operation.OperationArgumentType;
import com.arm.mbed.sda.proxysdk.operation.ParamElement;
import com.arm.mbed.sda.proxysdk.protocol.OperationResponse;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SendMessageUsbActivity extends ActionBarDrawerActivity {

    private static String TAG = "SendMessageUsbActivity";

    //Views
    private static ImageView mSerialConnectIndecationImageView;
    private ScrollView mResultScrollView;
    private TextView mResultScrollViewTextView;
    private Button mFirstActionButton;
    private Button mSecondActionButton;
    private Button mThirdActionButton;

    //Shared Ref
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String sharedPreferencesKeyValue = "AccessTokens";
    private AccessTokens accessTokens = new AccessTokens();
    private final static String accessTokenKeyNameInMap = "AccessToken";
    private LedConfiguration ledConfiguration = new LedConfiguration();
    private static final String sharedPreferencesKeyValueLed = "ledDetails";
    private String demoProfile;
    private static final String appDataSharedPreferencesKeyValue = "appDataDetails";
    private ApplicationData applicationData;

    //Device
    private IDevice device;
    private DeviceConnection dv = new DeviceConnection();

    //a little bit ugly
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting Drawer
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_send_message_usb, null, false);
        mDrawerLayout.addView(contentView, 0);

        mSerialConnectIndecationImageView = findViewById(R.id.SerialConnectIndicationImageView);
        mFirstActionButton = findViewById(R.id.ReadDataButton);
        mSecondActionButton = findViewById(R.id.ConfigureButton);
        mThirdActionButton = findViewById(R.id.UpdateButton);
        mResultScrollView = findViewById(R.id.ResultScrollView);
        mResultScrollViewTextView = findViewById(R.id.ResultScrollViewTextView);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ledConfiguration = new LedConfiguration();

        //Get the Access token map
        JSONObject jsonStored = dataHandler.getJsonStringData(
                sharedPreferencesKeyValue,
                SendMessageUsbActivity.this);
        accessTokens.setAccessTokensMap(jsonStored);

        mFirstActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!DeviceConnection.isDeviceConnected) {
                    printToScrollbar(getString(R.string.before_connect_device));
                    onButtonShowPopupWindowClick(
                            Color.parseColor("#4527A0"),
                            getString(R.string.op_device_not_connected_popup));
                    return;
                }

                device = DeviceConnection.device;
                SerialDeviceCmd serialDeviceCmd;

                if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
                    ParamElement[] cmdParams = new ParamElement[] {};
                    serialDeviceCmd = new SerialDeviceCmd(
                            CommandConstants.RESTART,
                            cmdParams);

                    printToScrollbar(getString(R.string.restart_to_device));
                } else {
                    ParamElement[] cmdParams = new ParamElement[] {};
                    serialDeviceCmd = new SerialDeviceCmd(
                            CommandConstants.READ_DATA,
                            cmdParams);

                    printToScrollbar(getString(R.string.read_data_to_device));
                }

                new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
            }
        });

        mSecondActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!DeviceConnection.isDeviceConnected) {
                    printToScrollbar(getString(R.string.before_connect_device));
                    onButtonShowPopupWindowClick(
                            Color.parseColor("#4527A0"),
                            getString(R.string.op_device_not_connected_popup));
                    return;
                }

                device = DeviceConnection.device;
                if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
                    SerialDeviceCmd serialDeviceCmd;
                    ParamElement[] cmdParams = new ParamElement[] {};
                    serialDeviceCmd = new SerialDeviceCmd(
                            CommandConstants.DIAGNOSTICS,
                            cmdParams);

                    printToScrollbar(getString(R.string.diagnostics_to_device));
                    new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
                } else {
                    degreePickerDialog(activity);
                }
            }
        });

        mThirdActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call to sent message Async thread

                if (!DeviceConnection.isDeviceConnected) {
                    printToScrollbar(getString(R.string.before_connect_device));
                    onButtonShowPopupWindowClick(
                            Color.parseColor("#4527A0"),
                            getString(R.string.op_device_not_connected_popup));
                    return;
                }

                device = DeviceConnection.device;
                SerialDeviceCmd serialDeviceCmd;

                if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
                    ParamElement[] cmdParams = new ParamElement[] {};
                    serialDeviceCmd = new SerialDeviceCmd(
                            CommandConstants.UPDATE,
                            cmdParams);

                    printToScrollbar(getString(R.string.update_to_device));
                } else {
                    ParamElement[] cmdParams = new ParamElement[] {};
                    serialDeviceCmd = new SerialDeviceCmd(
                            CommandConstants.UPDATE,
                            cmdParams);

                    printToScrollbar(getString(R.string.update_to_device));
                }

                new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
            }
        });
        mResultScrollView.setVerticalScrollBarEnabled(true);


        printToScrollbar(getString(R.string.before_connect_device));

        Log.d("","");

        //A little bit ugly
        activity = this;
    }

    @Override
    protected void onResume() {

        loadAppConfiguration();
        setDeviceConnectionIndication(DeviceConnection.isDeviceConnected);
        dv.registerDevice(this);

        if (demoProfile.equals(ApplicationData.HANNOVER_MESSE)) {
            mFirstActionButton.setText(R.string.restart_button);
            mSecondActionButton.setText(R.string.diagnostics_button);
        } else {
            mFirstActionButton.setText(R.string.read_data_button);
            mSecondActionButton.setText(R.string.configure_button);
        }

        super.onResume();
    }


    @Override
    protected void onStop() {
        dv.unregisterDevice(this);
        super.onStop();
    }

    private void loadAppConfiguration() {

        JSONObject jsonStored =  dataHandler.getJsonStringData(
                appDataSharedPreferencesKeyValue,
                SendMessageUsbActivity.this);

        if (null != jsonStored) {
            applicationData = new ApplicationData(jsonStored);
            demoProfile = applicationData.getDemoMode();
        } else {
            Log.d("","Stored app data is empty");
        }
    }

    public static void setDeviceConnectionIndication(boolean isConnected) {
        if (null != mSerialConnectIndecationImageView) {
            if (isConnected) {
                mSerialConnectIndecationImageView.setImageResource(R.drawable.connected);

            } else {
                mSerialConnectIndecationImageView.setImageResource(R.drawable.not_connected);
            }
        }
    }

    public void sendConfigureMessageToDevice(int degreeNumber) {
        //Retrieve login details from SharedRef
        JSONObject jsonStored =  dataHandler.getJsonStringData(
                sharedPreferencesKeyValueLed,
                SendMessageUsbActivity.this);

        //Set login details from SharedRef if exist
        if (null != jsonStored) {
            ledConfiguration = new LedConfiguration(jsonStored);
        }

        //TODO:: Create Device ENUM for leds and colors
        ParamElement[] cmdParams = new ParamElement[] {
                new ParamElement(OperationArgumentType.INT, Integer.toString(degreeNumber))
        };

        SerialDeviceCmd serialDeviceCmd = new SerialDeviceCmd(
                CommandConstants.CONFIGURE,
                cmdParams);

        printToScrollbar(getString(R.string.configure_to_device));
        new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
    }

    public void degreePickerDialog(Context mContext) {
        RelativeLayout linearLayout = new RelativeLayout(mContext);
        ContextThemeWrapper cw = new ContextThemeWrapper(mContext, R.style.NumberPickerText);
        final NumberPicker aNumberPicker = new NumberPicker(cw);
        aNumberPicker.setMaxValue(40);
        aNumberPicker.setMinValue(0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Target temperature");
        alertDialogBuilder.setMessage("Set target temperature:");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Log.e("","New Quantity Value : "+ aNumberPicker.getValue());
                                sendConfigureMessageToDevice(aNumberPicker.getValue());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void printToScrollbarWithColor(String text, int color) {

        String line = text + "\n";
        mResultScrollViewTextView.append(line);
        mResultScrollViewTextView.setTextColor(color);
        mResultScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void printToScrollbar(String text) {
        printToScrollbarWithColor(text, Color.parseColor("#000000"));
    }


    public class sendMessageToUsbSerialDevice extends AsyncTask<SerialDeviceCmd, Void, OperationResponse> {

        @Override
        protected OperationResponse doInBackground(SerialDeviceCmd... param) {

            SerialDeviceCmd serialDeviceCmd = param[0];

            //Check if the user input key exists
            String accessToken = accessTokens.getToken(accessTokenKeyNameInMap);
            if (null == accessToken) {
                return null;
            }

            OperationResponse op = null;

            try {
                op = SecuredDeviceAccess.sendMessage(
                        accessToken,
                        serialDeviceCmd.getCommand(),
                        serialDeviceCmd.getCommandParams(),
                        device);
            } catch(Exception e) {
                System.out.println("");
            }

            return op;
        }

        @Override
        protected void onPostExecute(OperationResponse result) {
            if (null != result ) {
                printToScrollbar(getString(R.string.op_result));
                printToScrollbar(getString(R.string.access_granted));
//                printToScrollbar("getResponseStatus: " + String.valueOf(result.getResponseStatus()));
//                printToScrollbar("getValue: " + String.valueOf(result.getType().getValue()));
                System.out.println(String.valueOf(result.getResponseStatus()));
                System.out.println(String.valueOf(result.getType().getValue()));

                if (null != result.getBlob()) {
                    saveBlobToFile(result.getBlob());
                }

                onButtonShowPopupWindowClick(
                        Color.parseColor("#388E3C"),
                        getString(R.string.op_ended_successfully_popup));
            } else {
                //TODO: return the real error?
                printToScrollbar(getString(R.string.op_result));
                printToScrollbar(getString(R.string.access_denied));
                onButtonShowPopupWindowClick(
                        Color.parseColor("#F44336"),
                        getString(R.string.op_access_denied_popup));
            }
        }
    }

    public void onButtonShowPopupWindowClick(int color, String text) {

        // get a reference to the already created main layout
        RelativeLayout mainLayout =
                findViewById(R.id.activity_main_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        popupView.setBackgroundColor(color);

        TextView mResultText = (TextView) popupView.findViewById(R.id.PopupTextView);
        mResultText.setText(text);
        mResultText.setTextColor(Color.WHITE);

//        TextView tx = findViewById(R.id.PopupTextView);
//        tx.setText(text);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void saveBlobToFile(byte[] blob) {

        FileOutputStream outputStream;
        File logFile;

        logFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Blob.txt");
        try {
            Log.d("BLOB_FILE", "Opening log file");
            outputStream = new FileOutputStream(logFile, true);
            Log.i("BLOB_FILE_WRITE", "Writning Blob");
            printToScrollbar("#Up to 100 first Chars from Blob STR");
            String blobStr = new String(blob, "UTF-8");
            printToScrollbar(blobStr.substring(0, Math.min(100, blobStr.length())));
            printToScrollbar("#End Print Blob STR");
            outputStream.write(blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
