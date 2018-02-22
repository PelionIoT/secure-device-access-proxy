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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.serial.ArmUsbDevice;
import com.arm.armsda.serial.SerialHandler;
import com.arm.armsda.data.CommandConstants;
import com.arm.mbed.dbauth.proxysdk.IDevice;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.dbauth.proxysdk.operation.OperationArgumentType;
import com.arm.mbed.dbauth.proxysdk.operation.ParamElement;
import com.arm.mbed.dbauth.proxysdk.protocol.OperationResponse;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldSendMessageUsbActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private SerialHandler serialHandler;
    private IDevice device;
    private AccessTokens accessTokens = new AccessTokens();
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String TokenSharedRefKey = "AccessTokens";
    private static boolean isUsbDeviceConnected = false;
    private List<UsbSerialDriver> availableDrivers;
    private String accessToken;

    private static String TAG = "OldSendMessageUsbActivity";
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                        }
                    } else {
                        System.out.println("permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zold_activity_send_message_usb);

        Button loadUsbDevices = findViewById(R.id.LoadUsbDevicesButton);
        Button connectDevice = findViewById(R.id.UsbButton);
        Button readDataButton = findViewById(R.id.ReadDataButton);
        Button configureButton = findViewById(R.id.ConfigureButton);


        loadUsbDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call to sent message Async thread
                new sendConfigureMessageToUsbDevice().execute();
            }
        });

        connectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermission()) {
                    isUsbDeviceConnected = true;
                    workWithDevice();
                }
                else {
                    if (checkPermission()) {
                        isUsbDeviceConnected = true;
                        workWithDevice();
                    }
                }
                //Toast.makeText(this, "Device Permission:" + hasPermision, Toast.LENGTH_LONG).show();
            }
        });

        readDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call to sent message Async thread
                new sendReadDataMessageToUsbDevice().execute();
            }
        });

        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call to sent message Async thread
                new sendConfigureMessageToUsbDevice().execute();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        //Get the Access token map
        JSONObject jsonStored = dataHandler.getJsonStringData(
                TokenSharedRefKey,
                OldSendMessageUsbActivity.this);
        accessTokens.setAccessTokensMap(jsonStored);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }

    }

    public class sendConfigureMessageToUsbDevice extends AsyncTask<Void, Void, OperationResponse> {

        @Override
        protected OperationResponse doInBackground(Void... voids) {

            //Check if the user input key exists
            accessToken = accessTokens.getToken("conf");
            if (null == accessToken) {
                return null;
            }

            OperationResponse op = null;

            ParamElement[] cmdParams = new ParamElement[] {
                    new ParamElement(OperationArgumentType.STR, "FWUP"),
                    new ParamElement(OperationArgumentType.STR, "GREEN")
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
                System.out.println(String.valueOf(result.getResponseStatus()));
                System.out.println(String.valueOf(result.getType().getValue()));
            } else {
                //responseStatus.setText(R.string.socket_timeout);
                //responseType.setText("N/A");
            }
        }
    }

    public class sendReadDataMessageToUsbDevice extends AsyncTask<Void, Void, OperationResponse> {

        @Override
        protected OperationResponse doInBackground(Void... voids) {

            //Check if the user input key exists
            accessToken = accessTokens.getToken("read");
            if (null == accessToken) {
                return null;
            }

            OperationResponse op = null;

            String[] cmdParams = new String[] {};

            try {
                op = SecuredDeviceAccess.sendMessage(
                        accessToken,
                        CommandConstants.READ_DATA,
                        new ParamElement[] {},
                        device);
            } catch(Exception e) {
                System.out.println("");
            }

            return op;
        }

        @Override
        protected void onPostExecute(OperationResponse result) {
            if (null != result ) {
                System.out.println(String.valueOf(result.getResponseStatus()));
                System.out.println(String.valueOf(result.getType().getValue()));
            } else {
                //responseStatus.setText(R.string.socket_timeout);
                //responseType.setText("N/A");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        try {
//            serialPort.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();

//        try {
//            serialPort.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    private boolean checkPermission() {

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        boolean hasPermision;
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        Map.Entry<String,UsbDevice> entry = usbDevices.entrySet().iterator().next();
        UsbDevice usbDevice;
        usbDevice = usbDevices.get(entry.getKey());
        usbManager.requestPermission(usbDevice, permissionIntent);
        hasPermision = usbManager.hasPermission(usbDevice);
        Toast.makeText(this, "Device Permission:" + hasPermision, Toast.LENGTH_LONG).show();
        return hasPermision;
    }

    private void workWithDevice() {

        if (isUsbDeviceConnected) {

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
            if (connection == null) {

                boolean hasPermision = checkPermission();
                // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                Toast.makeText(this, "Device Permission before exit:" + hasPermision, Toast.LENGTH_LONG).show();
                return;
            }

            // Read some data! Most have just one port (port 0).
            serialPort = driver.getPorts().get(0);
            try {
                serialPort.open(connection);
                serialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //serialHandler = new SerialHandler(serialPort);

            //Prepare device
            device = new ArmUsbDevice(serialPort);
        }
    }


}
