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
package com.arm.armsda.serial;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.util.Arrays;

public class SerialHandler {

    public static final int INPUT_BUFFER_LENGTH = 4096;
    private final String TAG = SerialHandler.class.getSimpleName();

    private UsbSerialPort serialPort;
    private boolean startRead = false;
    private ISerialDataSink serialDataSink;
    private Thread thread;

    public SerialHandler(UsbSerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public void startRead(ISerialDataSink serialDataSink) {
        if (!this.startRead) {
            this.startRead = true;
            thread = new Thread(mLoop);
            thread.start();
        }
        this.serialDataSink = serialDataSink;
    }

    public void stopRead() {
        this.startRead = false;
    }

    private Runnable mLoop = new Runnable() {

        @Override
        public synchronized void run() {
            for (; ; ) {
                byte[] buffer = new byte[INPUT_BUFFER_LENGTH];
                try {
                    int numBytesRead = serialPort.read(buffer, 200);
                    if (numBytesRead > 0) {
                        byte[] tmp = Arrays.copyOf(buffer, numBytesRead);
                        String str = new String(tmp, "UTF-8");
                        //Log.d( TAG,"####### READ: mLoop:" + str);
                        serialDataSink.onNewData(tmp);
                    } else if (numBytesRead == 0 && !startRead) {
                        Log.d( TAG,"## Stop Reading from Device");
                        return;
                    }
                } catch (IOException e) {
                    Log.e( TAG,"####### IOException - serialPort.read:" + e.getMessage());
                    return;
                }
            }
        }
    };

    public void write(byte[] bufferOut) {
        try {
            int numOfBytesWritten = serialPort.write(bufferOut, 1000);
            String str = new String(bufferOut, "UTF-8");
            Log.d( TAG,"####### Write Str:" + str);
            Log.d( TAG,"####### Write Byte Array:" + Arrays.toString(bufferOut));
            Log.d( TAG,"WRITTEN TO DEVICE! : " + numOfBytesWritten);
        } catch (IOException e) {
            Log.e( TAG,"IOException writing:" + e.getMessage());
        }
    }
}
