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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;


import com.arm.armsda.R;
import com.arm.armsda.data.AccessTokens;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.LedConfiguration;
import com.arm.armsda.data.SerialDeviceCmd;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.serial.ArmUsbDevice;
import com.arm.armsda.utils.AndroidUtils;
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

//TODO:: many open issues
// 1) fix device button indication "Disconnect device"
// 2) fix when pressing the connect button and no device connected
// 3) scroll view add

public class SendMessageUsbActivity extends AppCompatActivity {

    private static String TAG = "SendMessageUsbActivity";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if ((mUsbDevice.getVendorId() == 3368) && (mUsbDevice.getProductId() == 516)) {
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                connectToDevice();
                            }
                        } else {
                            AndroidUtils.customToast(
                                    SendMessageUsbActivity.this,
                                    "Device Pearmission denied",
                                    Color.RED
                            );
                        }
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // Device removed
                synchronized (this) {
                    cleanDeviceConnection();
                    printToScrollbar("Serial device DISCONNECTED!");
                }
            }
//            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                // Device attached
//                synchronized (this) {
//                    // Qualify the new device to suit your needs and request permission
//                    if ((mUsbDevice.getVendorId() == 1111) && (mUsbDevice.getProductId() == 222)) {
//                        mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
//                    }
//                }
//            }


        }
    };

    //Views
    private Button mConnectSerialDeviceButton;
    private Button mReadDataButton;
    private Button mConfigureButton;
    private Button mUpdateButton;
    private static ImageView mSerialConnectIndecationImageView;
    private ScrollView mResultScrollView;
    private TextView mResultScrollViewTextView;

    //Shared Ref
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    private static final String sharedPreferencesKeyValue = "AccessTokens";
    private AccessTokens accessTokens = new AccessTokens();
    private final static String accessTokenKeyNameInMap = "AccessToken";
    private LedConfiguration ledConfiguration = new LedConfiguration();
    private static final String sharedPreferencesKeyValueLed = "ledDetails";

    //Device
    private IDevice device;
    private static boolean permissionGranted = false;
    private PendingIntent mPermissionIntent = null;
    private UsbManager mUsbManager = null;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mConnection;
    private UsbSerialPort mSerialPort;
    private boolean isDeviceConnected = false;

    //a little bit ugly
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message_usb);

        mConnectSerialDeviceButton = findViewById(R.id.ConnectSerialDeviceButton);
        mSerialConnectIndecationImageView = findViewById(R.id.SerialConnectIndecationImageView);
        mReadDataButton = findViewById(R.id.ReadDataButton);
        mConfigureButton = findViewById(R.id.ConfigureButton);
        mUpdateButton = findViewById(R.id.UpdateButton);
        mResultScrollView = findViewById(R.id.ResultScrollView);
        mResultScrollViewTextView = findViewById(R.id.ResultScrollViewTextView);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ledConfiguration = new LedConfiguration();

        //Get the Access token map
        JSONObject jsonStored = dataHandler.getJsonStringData(
                sharedPreferencesKeyValue,
                SendMessageUsbActivity.this);
        accessTokens.setAccessTokensMap(jsonStored);

        mConnectSerialDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //registerReceiver(mUsbReceiver, filter);
                //Call to get access token Async thread
                //new AccessTokenActivity.httpCallAccessToken().execute();

                if (isDeviceConnected) {
                    cleanDeviceConnection();
                } else {
                    checkPermissionAndConnectDevice();
                }

                Log.d("","");
            }
        });


        mReadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDeviceConnected) {
                    printToScrollbar("Please connect Serial device");
                    onButtonShowPopupWindowClick(Color.CYAN,"Device is not connected.");
                    return;
                }

                ParamElement[] cmdParams = new ParamElement[] {};
                SerialDeviceCmd serialDeviceCmd = new SerialDeviceCmd(
                        CommandConstants.READ_DATA,
                        cmdParams);

                printToScrollbar("Sending Read data message to Serial device");
                new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
            }
        });

        mConfigureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDeviceConnected) {
                    printToScrollbar("Please connect Serial device");
                    onButtonShowPopupWindowClick(Color.CYAN,"Device is not connected.");
                    return;
                }

                degreePickerDialog(activity);
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call to sent message Async thread

                if (!isDeviceConnected) {
                    printToScrollbar("Please connect Serial device");
                    onButtonShowPopupWindowClick(Color.CYAN,"Device is not connected.");
                    return;
                }

                ParamElement[] cmdParams = new ParamElement[] {};
                SerialDeviceCmd serialDeviceCmd = new SerialDeviceCmd(
                        CommandConstants.UPDATE,
                        cmdParams);

                printToScrollbar("Sending UPDATE message to Serial device");
                new SendMessageUsbActivity.sendMessageToUsbSerialDevice().execute(serialDeviceCmd);
            }
        });
        mResultScrollView.setVerticalScrollBarEnabled(true);

        printToScrollbar("Please Connect Serial Device");
        Log.d("","");

        //A little bit ugly
        activity = this;
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

        printToScrollbar("Sending Configure message to Serial device");
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
        alertDialogBuilder.setTitle("Select a number:");
        alertDialogBuilder.setMessage("Is it cold or warm?");
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
    @Override
    protected void onResume() {
        super.onResume();

        registerDevice();
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(mUsbReceiver);
        cleanDeviceConnection();
        super.onStop();
    }

    public void printToScrollbarWithColor(String text, int color) {

        String line = text + "\n";
        mResultScrollViewTextView.append(line);
        mResultScrollViewTextView.setTextColor(color);
        mResultScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void printToScrollbar(String text) {
        printToScrollbarWithColor(text, Color.parseColor("#A4C639"));
    }

    private void cleanDeviceConnection() {
        if (isDeviceConnected) {
            try {
                if (null != mConnection) {
                    mConnection.close();
                }
                if (null != mSerialPort) {
                    mSerialPort.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mSerialConnectIndecationImageView.setImageResource(R.drawable.not_connected);
            isDeviceConnected = false;
            mConnectSerialDeviceButton.setText("Connect Serial Device");
            printToScrollbar("Serial device DISCONNECTED!");
        }

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
                printToScrollbar("Result:");
                printToScrollbar("ACCESS GRANTED");
//                printToScrollbar("getResponseStatus: " + String.valueOf(result.getResponseStatus()));
//                printToScrollbar("getValue: " + String.valueOf(result.getType().getValue()));
                System.out.println(String.valueOf(result.getResponseStatus()));
                System.out.println(String.valueOf(result.getType().getValue()));
                onButtonShowPopupWindowClick(Color.GREEN,"Action ended Successfully");
            } else {
                //TODO: return the real error?
                printToScrollbar("Result:");
                printToScrollbar("ACCESS DENIED");
                onButtonShowPopupWindowClick(Color.RED,"ACCESS DENIED!");
            }
        }
    }

    private void registerDevice() {
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        // Register an intent filter so we can get permission to connect
        // to the device and get device attached/removed messages
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }


    private void checkPermissionAndConnectDevice() {

        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        if (usbDevices.size() > 0) {
            Map.Entry<String, UsbDevice> entry = usbDevices.entrySet().iterator().next();
            mUsbDevice = usbDevices.get(entry.getKey());
            boolean hasPermision = mUsbManager.hasPermission(mUsbDevice);
            if (!hasPermision) {
                mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
            } else {
                connectToDevice();
            }
        }
        else {
            AndroidUtils.customToast(
                    SendMessageUsbActivity.this,
                    "Unrecognized OR no device connected",
                    Color.RED
            );
        }
    }

    private void connectToDevice() {
        // Open a connection to the first available driver.

        List<UsbSerialDriver> availableDrivers;
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        mConnection = mUsbManager.openDevice(driver.getDevice());
        if (mConnection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            Toast.makeText(this, "Device Permission before exit:", Toast.LENGTH_LONG).show();
            return;
        }

        // Read some data! Most have just one port (port 0).
        mSerialPort = driver.getPorts().get(0);
        try {
            mSerialPort.open(mConnection);
            mSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //serialHandler = new SerialHandler(serialPort);

        //Prepare device
        device = new ArmUsbDevice(mSerialPort);
        mSerialConnectIndecationImageView.setImageResource(R.drawable.connected);
        mConnectSerialDeviceButton.setText("Disconnect Device");
        isDeviceConnected = true;
        printToScrollbar("Serial device CONNECTED!");
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

}
