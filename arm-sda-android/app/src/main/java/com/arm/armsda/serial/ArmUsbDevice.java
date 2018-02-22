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

import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.devices.AbstractDevice;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ArmUsbDevice extends AbstractDevice implements ISerialDataSink {

    private static final byte[] BOM = new byte[] {0x6d, 0x62, 0x65, 0x64, 0x64, 0x62, 0x61, 0x70};
    private static final int SHA256_LENGTH = 32;

    private SerialHandler serialHandler;
    private IUsbDeviceLogger logger;

    private enum State {
        BomSearch,
        MsgLenSearch,
        InMessage,
        DigestSearch;
    };

    byte[] workBuffer = new byte[SerialHandler.INPUT_BUFFER_LENGTH * 2];
    State state;
    byte[] msgBuffer;
    int mark;
    int expectedMsgLength;
    byte[] response;

    public ArmUsbDevice(UsbSerialPort serialPort) {
        this(serialPort, new IUsbDeviceLogger() {
                            @Override
                            public void log(byte[] logStream) {
                            }
            }
        );
    }

    public ArmUsbDevice(UsbSerialPort serialPort, IUsbDeviceLogger logger) {
        this.serialHandler = new SerialHandler(serialPort);
        this.logger = logger;
    }

    @Override   // from AbstractDevice
    public byte[] sendMessage(byte[] operationMessage) {

        // Write message to usb
        byte[] serialProtocolMessage = formatSerialProtocolMessage(operationMessage);
        serialHandler.write(serialProtocolMessage);

        // Read response (Read message from usb)
        state = State.BomSearch;
        msgBuffer = null;
        workBuffer = new byte[SerialHandler.INPUT_BUFFER_LENGTH * 2];
        mark = 0;
        expectedMsgLength = 0;
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
        return response;
    }

    @Override   // from ISerialDataSink
    public void onNewData(byte[] data) {
        System.out.println("ArmUsbDevice: onNewData");

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
            //serialHandler.stopRead();
            byte[] msgDigest = Arrays.copyOfRange(tmpBuffer, 0, SHA256_LENGTH);
            if (null == msgBuffer) {
                System.out.println("ArmUsbDevice: msgBuffer is null?");
            }
            byte[] expectedDigest = getDigest(msgBuffer);
            if (!Arrays.equals(msgDigest, expectedDigest)) {
                System.out.println("msgDigest:" + Arrays.toString(msgDigest) + " Size: " + msgDigest.length );
                System.out.println("expectedDigest:" + Arrays.toString(expectedDigest)  + " Size: " + expectedDigest.length );
                System.out.println("tmpBuffer:" + Arrays.toString(tmpBuffer)  + " Size: " + tmpBuffer.length );
                throw new ProxyException("Device message has invalid digest");
            }
            System.out.println("ArmUsbDevice: Returning Message!");
            Log.i("", "ArmUsbDevice: Returning Message!");

            serialHandler.stopRead();

            response = msgBuffer;
        }
    }

    private boolean findBom(byte[] tmpBuffer) {
        boolean found = false;
        int i = 0;
        for (i = 0; i <= tmpBuffer.length - BOM.length; i ++) {
            found = true;
            for (int j = 0; j < BOM.length; j ++) {
                if (tmpBuffer[i + j] != BOM[j]) {
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
        logger.log(logBuffer);
        if (!found) {
            mark = tmpBuffer.length - i;
            System.arraycopy(tmpBuffer, i, workBuffer, 0, mark);
        } else {
            mark = tmpBuffer.length - (i+BOM.length);
            System.arraycopy(tmpBuffer, i+BOM.length, workBuffer, 0, mark);
        }
        return found;
    }

    private byte[] formatSerialProtocolMessage(byte[] operationMsg) {

        byte[] digest = getDigest(operationMsg);
        byte[] msgSize = new byte[4];
        msgSize = toBytes(operationMsg.length);

        byte[] serialMsg = new byte[BOM.length + msgSize.length + operationMsg.length + digest.length];
        System.arraycopy(BOM, 0, serialMsg, 0, BOM.length);
        System.arraycopy(msgSize, 0, serialMsg, BOM.length, msgSize.length);
        System.arraycopy(operationMsg, 0, serialMsg, BOM.length + msgSize.length, operationMsg.length);
        System.arraycopy(digest, 0, serialMsg, BOM.length + msgSize.length + operationMsg.length, digest.length);
        return serialMsg;
    }

    private byte[] getDigest(byte[] msg) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException ex) {
            throw new ProxyException("Failed to get SHA256 message digest factory");
        }
        md.update(msg);
        return md.digest();
    }

    private byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    private void sleeping() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
