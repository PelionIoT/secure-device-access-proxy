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

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.devices.AbstractDevice;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ArmUsbDevice extends AbstractDevice implements ISerialDataSink {

    private static final int SHA256_LENGTH = 32;
    private SerialHandler serialHandler;
    private File logFile;
    private FileOutputStream outputStream;
    private Context ctx;
    private StringBuffer logStringBuffer = new StringBuffer("");
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private  byte[] workBuffer = new byte[SerialHandler.INPUT_BUFFER_LENGTH * 2];
    private State state;
    private byte[] msgBuffer;
    private int mark;
    private int expectedMsgLength;
    private byte[] response;

    private enum State {
        BomSearch,
        MsgLenSearch,
        InMessage,
        DigestSearch;
    };

    public ArmUsbDevice(UsbSerialPort serialPort, Context ctx) {
        this.serialHandler = new SerialHandler(serialPort);
        logStringBuffer.setLength(0);

        this.ctx = ctx;
        logFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Mbed_log.txt");
        logFile.delete();
    }

    @Override   // from AbstractDevice
    public byte[] sendMessage(byte[] operationMessage) {

        // Write message to usb
        byte[] serialProtocolMessage = SerialMessage.formatSerialProtocolMessage(operationMessage);
        serialHandler.write(serialProtocolMessage);

        // Read response (Read message from usb)
        state = State.BomSearch;
        msgBuffer = null;
        workBuffer = new byte[SerialHandler.INPUT_BUFFER_LENGTH * 2];
        mark = 0;
        expectedMsgLength = 0;

        try {
            Log.d("M_FILE", "Opening log file");
            //Opening the log file (append mode), in case that the open file location/path is
            //read-only FileNotFoundException will be thrown
            outputStream = new FileOutputStream(logFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        serialHandler.startRead(this);
        response = null;

        //for (int i = 0; i < 300; i ++) {    // 300 * 100 is 30 secs
        while (true) {    // 300 * 100 is 30 secs
            try {
                Thread.sleep(200);
                if (response != null) {
                    break;
                }
            } catch (InterruptedException ex) {
                throw new ProxyException("Failed to get response from device");
            }
        }

        // initiate media scan and put the new things into the path array to
        // make the scanner aware of the location and the files you want to see
        MediaScannerConnection.scanFile(
                ctx,
                new String[] {logFile.toString()},
                null,
                null);

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override   // from ISerialDataSink
    public void onNewData(byte[] data) {

        byte[] tmpBuffer = new byte[mark + data.length];
        System.arraycopy(workBuffer, 0, tmpBuffer, 0, mark);
        System.arraycopy(data, 0, tmpBuffer, mark, data.length);
        if (state == State.BomSearch) {
            if (!findBom(tmpBuffer)) {
                return;
            } else {
                tmpBuffer = new byte[mark];
                System.arraycopy(workBuffer, 0, tmpBuffer, 0, mark);
                state = State.MsgLenSearch;
            }
        }
        if (state == State.MsgLenSearch) {
            System.out.println("ArmUsbDevice: MsgLenSearch");
            if (tmpBuffer.length < Integer.BYTES) {
                System.arraycopy(tmpBuffer, 0, workBuffer, 0, tmpBuffer.length);
                mark = tmpBuffer.length;
                sleeping();
                return;
            }
            ByteBuffer bb = ByteBuffer.wrap(tmpBuffer).order(ByteOrder.BIG_ENDIAN);
            expectedMsgLength = bb.getInt();
            tmpBuffer = Arrays.copyOfRange(tmpBuffer, Integer.BYTES, tmpBuffer.length);
            state = State.InMessage;
        }
        if (state == State.InMessage) {
            System.out.println("ArmUsbDevice: InMessage");
            if (tmpBuffer.length < expectedMsgLength) {
                System.arraycopy(tmpBuffer, 0, workBuffer, 0, tmpBuffer.length);
                mark = tmpBuffer.length;
                sleeping();
                return;
            }
            msgBuffer = Arrays.copyOfRange(tmpBuffer, 0, expectedMsgLength);
            tmpBuffer = Arrays.copyOfRange(tmpBuffer, expectedMsgLength, tmpBuffer.length);
            state = State.DigestSearch;
        }
        if (state == State.DigestSearch) {
            System.out.println("ArmUsbDevice: DigestSearch");
            if (tmpBuffer.length < SHA256_LENGTH) {
                System.arraycopy(tmpBuffer, 0, workBuffer, 0, tmpBuffer.length);
                mark = tmpBuffer.length;
                sleeping();
                return;
            }
            byte[] msgDigest = Arrays.copyOfRange(tmpBuffer, 0, SHA256_LENGTH);
            if (null == msgBuffer) {
                System.out.println("ArmUsbDevice: msgBuffer is null?");
            }
            byte[] expectedDigest = SerialMessage.getDigest(msgBuffer);
            if (!Arrays.equals(msgDigest, expectedDigest)) {
                System.out.println("msgDigest:" + Arrays.toString(msgDigest) + " Size: " + msgDigest.length);
                System.out.println("expectedDigest:" + Arrays.toString(expectedDigest) + " Size: " + expectedDigest.length);
                System.out.println("tmpBuffer:" + Arrays.toString(tmpBuffer) + " Size: " + tmpBuffer.length);
                throw new ProxyException("Device message has invalid digest");
            }

            printToLogAndStdout(tmpBuffer, true);

            serialHandler.stopRead();
            state = State.BomSearch;

            System.out.println("ArmUsbDevice: Returning Message!");
            Log.i("", "ArmUsbDevice: Returning Message!");

            response = msgBuffer;
        }
    }

    private boolean findBom(byte[] tmpBuffer) {
        boolean found = false;
        int i = 0;
        for (; i <= tmpBuffer.length - SerialMessage.BOM.length; i ++) {
            found = true;
            for (int j = 0; j < SerialMessage.BOM.length; j ++) {
                if (tmpBuffer[i + j] != SerialMessage.BOM[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                System.out.println("### Bom has been found!!");
                break;
            }
        }
        byte[] logBuffer = new byte[i];
        System.arraycopy(tmpBuffer, 0, logBuffer, 0, i);

        printToLogAndStdout(logBuffer, false);

        if (!found) {
            mark = tmpBuffer.length - i;
            System.arraycopy(tmpBuffer, i, workBuffer, 0, mark);
        } else {
            mark = tmpBuffer.length - (i + SerialMessage.BOM.length);
            System.arraycopy(tmpBuffer, i + SerialMessage.BOM.length, workBuffer, 0, mark);
        }
        return found;
    }

    private void sleeping() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printToLogAndStdout(byte[] logBuffer, boolean lastCall) {

        if (lastCall) {
            Log.i("MBED_DEBUG", logStringBuffer.toString());
            try {
                outputStream.write(logStringBuffer.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String logStr = null;
        try {
            logStr = new String(logBuffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logStringBuffer.append(logStr);
        String[] lines = logStringBuffer.toString().split(LINE_SEPARATOR);
        if (lines.length > 1) {
            for (int index = 0; index < lines.length -1; index++) {
                Log.i("MBED_DEBUG", lines[index]);
                try {
                    outputStream.write(lines[index].getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logStringBuffer.setLength(0);
            logStringBuffer.append(lines[lines.length-1]);
        }
    }

}
