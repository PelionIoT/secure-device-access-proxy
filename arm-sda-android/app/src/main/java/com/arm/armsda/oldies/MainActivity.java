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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arm.armsda.R;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;

/*
FYI - few details that should be sync with the service side upon blob generation and (obviously) before demo run.

Device End-point name:  dev1

Device TA value:
    0x04, 0x88, 0x22, 0x2f, 0xe4, 0x15, 0x87, 0xfe, 0x3d, 0x40, 0x9b, 0x9c, 0x2e, 0xac, 0xdf,
    0x2a, 0x17, 0xa4, 0x74, 0xc8, 0x33, 0x90, 0xc9, 0xd8, 0x3c, 0xf5, 0x7d, 0xbb, 0x55, 0x65,
    0xc8, 0x3b, 0x0f, 0xa8, 0x23, 0x34, 0x84, 0xd3, 0x2e, 0x19, 0xfe, 0x0b, 0x5b, 0x5a, 0xc5,
    0x26, 0x24, 0x53, 0xeb, 0x9e, 0x8a, 0xde, 0xfe, 0xb9, 0x75, 0xb5, 0x26, 0x2d, 0x56, 0xd3,
    0xa2, 0x05, 0xb3, 0x2d, 0x3e

The demo flow (Configure and Read-data) outlined here.

In the "Configure" flow, the user provides a scope in form of demo_callback_configure-XXX, where XXX is the target LED name to toggle.
The optional LED names are listed below (in their string representation - as they should be):
1.	POWER
2.	WIFI
3.	CLOUD
4.	FWUP
5.	LIGHT
6.	TEMP
7.	HUMIDITY
8.	SOUND

The user also provides the LED colour to set.
The optional LED colour names are listed below (in their string representation - as they should be):
1.	GREEN
2.	RED
3.	ORANGE
4.	PURPLE
5.	SNOW_YELLOW

        --Motti
 */


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zold_activity_main);

        /* Views */
        Button generateAccessTokenButton;
        Button sendMessageButton;
        Button sendMessageUsbButton;
        Button exitAppButton;
        TextView versionNum;

        /* Set Permissions */
        verifyStoragePermissions(this);

        /* Send filepath to SDK so it could initialize the keystore */
        String androidPath = this.getFilesDir().toString();
        SecuredDeviceAccess.setKeyStorePath(androidPath);

        /* Generate access token activity */
        generateAccessTokenButton = findViewById(R.id.accessTokenButton);
        generateAccessTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = MainActivity.this;

                Class destinationActivity = OldAccessTokenActivity.class;

                Intent accessTokenActivityIntent = new Intent(context, destinationActivity);

                startActivityForResult(accessTokenActivityIntent, 1);
            }
        });

        /* Send Message activity */
        sendMessageButton = findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Context context = MainActivity.this;

                Class destinationActivity = SendMessageActivity.class;

                Intent startSendMessageActivityIntent = new Intent(context, destinationActivity);

                startActivity(startSendMessageActivityIntent);
            }
        });

        sendMessageUsbButton = findViewById(R.id.SendMessageUsb);
        sendMessageUsbButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Context context = MainActivity.this;

                Class destinationActivity = OldSendMessageUsbActivity.class;

                Intent startSendMessageUsbActivityIntent = new Intent(context, destinationActivity);

                startActivity(startSendMessageUsbActivityIntent);
            }
        });


        /* Exit Application*/
        exitAppButton = findViewById(R.id.exitButton);
        exitAppButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);
            }
        });

        //Version Number
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionNum = findViewById(R.id.versionNumber);
            String ver = version + " BETA!";
            versionNum.setText(ver);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch(requestCode) {
//            case (1) : {
//                if (resultCode == Activity.RESULT_OK) {
//                    returnedAccessToken = data.getStringExtra("ReturnedAccessToken");
//                }
//                break;
//            }
//        }
//    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}
