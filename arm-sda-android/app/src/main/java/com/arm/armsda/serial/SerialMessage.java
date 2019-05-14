package com.arm.armsda.serial;

import com.arm.mbed.sda.proxysdk.ProxyException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SerialMessage {

    public static final byte[] BOM = new byte[] {0x6d, 0x62, 0x65, 0x64, 0x64, 0x62, 0x61, 0x70};

    public static byte[] formatSerialProtocolMessage(byte[] operationMsg) {

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

    public static byte[] getDigest(byte[] msg) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException ex) {
            throw new ProxyException("Failed to get SHA256 message digest factory");
        }
        md.update(msg);
        return md.digest();
    }

    private static byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

}
