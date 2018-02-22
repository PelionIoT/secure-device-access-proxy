package com.arm.mbed.dbauth.proxysdk.cose;

import com.upokecenter.cbor.CBORObject;

public class CounterSign extends Signer {
    
    public CounterSign() {
        contextString = "CounterSignature";
    }
    
    public CounterSign(byte[] rgb) throws CoseException {
        contextString = "CounterSignature";
        DecodeFromBytes(rgb);
    }
    
    public void DecodeFromBytes(byte[] rgb) throws CoseException
    {
        CBORObject obj = CBORObject.DecodeFromBytes(rgb);
        
        DecodeFromCBORObject(obj);
    }
    
    public byte[] EncodeToBytes() throws CoseException {
        return EncodeToCBORObject().EncodeToBytes();
    }
    
    public void Sign(Message message) throws CoseException {
        byte[] rgbBodyProtect;
        if (message.objProtected.size() > 0) rgbBodyProtect = message.objProtected.EncodeToBytes();
        else rgbBodyProtect = new byte[0];
        
        sign(rgbBodyProtect, message.rgbContent);        
    }
    
    public boolean Validate(Message message) throws CoseException {
        byte[] rgbBodyProtect;
        if (message.objProtected.size() > 0) rgbBodyProtect = message.objProtected.EncodeToBytes();
        else rgbBodyProtect = new byte[0];
        
        return validate(rgbBodyProtect, message.rgbContent);
    }

}
