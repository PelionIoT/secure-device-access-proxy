// ----------------------------------------------------------------------------
// Copyright 2017-2019 ARM Ltd.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ----------------------------------------------------------------------------
package com.arm.mbed.aceauth.cose;

import com.arm.mbed.aceauth.cose.CoseConstants.MessageTag;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

public abstract class CoseMessage {

    public static final int PROTECTED = 1;
    public static final int UNPROTECTED = 2;
    
    /**
     * Internal map of protected attributes
     */
    protected CBORObject objProtected = CBORObject.NewMap();
    
    /**
     * Internal map of unprotected attributes
    */
    protected CBORObject objUnprotected = CBORObject.NewMap();
    
    /**
     * The encoded byte string for the protected attributes.  If this variable is 
     * set then the message was either decoded or as been signed.
     * If it is set, then do not allow objProtected to be modified.
     */
    protected byte[] rgbProtected;
    
    /**
     * The plain text content of the message.
     */
    protected byte[] rgbContent = null;
    
    /**
     * Holder for the external data object that is authenticated as part of the 
     * message
     */
    protected byte[] externalData = new byte[0];

    private MessageTag messageTag;
    
    protected CoseMessage(MessageTag messageTag) {
    	this.messageTag = messageTag;
    }

    /**
     * Return the content bytes of the message
     * 
     * @return bytes of the content
     */
    public byte[] GetContent() {
        return rgbContent;
    }
    
    /**
     * Set an attribute in the COSE object.  
     * Setting an attribute in one map will remove it from all other maps as a side effect.
     * 
     * @param label CBOR object which identifies the attribute in the map
     * @param value CBOR object which contains the value of the attribute
     * @param where Identifies which of the buckets to place the attribute in.
     *      ProtectedAttributes - attributes cryptographically protected
     *      UnprotectedAttributes - attributes not cryptographically protected
     * @exception CoseException COSE Package exception
     */
    public void addAttribute(CBORObject label, CBORObject value, int where) throws CoseException {
    	
        removeAttribute(label);
        if ((label.getType() != CBORType.Number) &&  (label.getType() != CBORType.TextString)) {
            throw new CoseException("Labels must be integers or strings");
        }
        switch (where) {
            case PROTECTED:
                if (rgbProtected != null) throw new CoseException("Cannot modify protected attribute if signature has been computed");
                objProtected.Add(label, value);
                break;
                
            case UNPROTECTED:
                objUnprotected.Add(label, value);
                break;
                
            default:
                throw new CoseException("Invalid attribute location given");
        }
    }

    /**
     * Remove an attribute from the set of all attribute maps.
     * 
     * @param label attribute to be removed
     * @exception CoseException if integrity protection would be modified.
     */
    public void removeAttribute(CBORObject label) throws CoseException {
        if (objProtected.ContainsKey(label)) {
            if (rgbProtected != null) throw new CoseException("Operation would modify integrity protected attributes");
            objProtected.Remove(label);
        }
        if (objUnprotected.ContainsKey(label)) objUnprotected.Remove(label);
    }
    
    /**
     * Encode the COSE message object to a CBORObject tree.  This function call will force cryptographic operations to be executed as needed.
     * This is an internal function, as such it does not add the tag on the front and is implemented on a per message object.
     * 
     * @return CBORObject representing the message.
     * @throws CoseException 
     */
    protected abstract CBORObject EncodeCBORObject() throws CoseException;
    
    /**
     * Encode the COSE message object to a CBORObject tree.  This function call will force cryptographic operations to be executed as needed.
     * 
     * @return CBORObject representing the message.
     * @throws CoseException 
     */
    public CBORObject EncodeToCBORObject() throws CoseException {
        
        CBORObject obj = EncodeCBORObject();
        
        obj = CBORObject.FromObjectAndTag(obj, messageTag.value());
        
        return obj;
    }
    
    /**
     * Decode a COSE message object. Use a value of {@code MessageTag.Unknown}
     * to decode a generic structure with tagging.  Use a specific value if
     * the tagging is absent or if a known structure is passed in.
     * 
     * @param rgbData byte stream to be decoded
     * @param defaultTag assumed message type to be decoded
     * @return the decoded message object
     * @throws CoseException on a decode failure.
     */
    @SuppressWarnings("deprecation")
    public static CoseMessage DecodeFromBytes(byte[] rgbData, MessageTag defaultTag) throws CoseException {
    	
        CBORObject messageObject = CBORObject.DecodeFromBytes(rgbData);
        
        if (messageObject.getType() != CBORType.Array)  throw new CoseException("Message is not a COSE security Message");
        
        if (messageObject.isTagged()) {
            if (messageObject.GetTags().length != 1) throw new CoseException("Malformed message - too many tags");
            
            if (defaultTag == MessageTag.Unknown) {
                defaultTag = MessageTag.FromInt(messageObject.getMostInnerTag().ToInt32Checked());
            }
            else if (defaultTag != MessageTag.FromInt(messageObject.getMostInnerTag().ToInt32Checked())) {
                throw new CoseException("Passed in tag does not match actual tag");
            }
        }
        
        CoseMessage msg;
        
        switch (defaultTag) {
            case Unknown: // Unknown
                throw new CoseException("Message was not tagged and no default tagging option given");
                
            case Sign1:
                msg = new CoseMessageSign1();
                break;
                
            default:
                throw new CoseException("Message is not recognized as a COSE security Object");
        }
    
        msg.DecodeFromCBORObject(messageObject);
        return msg;
        
    }

    /**
     * Given a CBOR tree, parse the message.  This is an abstract function that is implemented for each different supported COSE message. 
     * 
     * @param messageObject CBORObject to be converted to a message.
     * @throws CoseException 
     */
    
    protected abstract void DecodeFromCBORObject(CBORObject messageObject) throws CoseException;

}
