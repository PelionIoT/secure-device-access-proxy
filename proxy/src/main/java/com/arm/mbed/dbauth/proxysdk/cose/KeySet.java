package com.arm.mbed.dbauth.proxysdk.cose;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.upokecenter.cbor.CBORObject;

public class KeySet {
    private List<OneKey> keys;
    
    public KeySet() {
        keys = new ArrayList<OneKey>();
    }

    
    public KeySet(CBORObject keysIn) {
        keys = new ArrayList<OneKey>();
        
        //  Ignore keys which we cannot deal with or are malformed.
        
        for (int i=0; i<keysIn.size(); i++) {
            try {
                keys.add(new OneKey(keysIn.get(i)));
            } catch(CoseException e) {
                ;
            }
        }
    }
    
    public void add(OneKey key) {
        keys.add(key);
    }
    
    public List<OneKey> getList() {
        return keys;
    }
    
    public void remove(OneKey key) {
        keys.remove(key);
    }
    
    public Stream<OneKey> stream() {
        return keys.stream();
    }
    
    public Stream<OneKey> parallelStream() {
        return keys.parallelStream();
    }
}
