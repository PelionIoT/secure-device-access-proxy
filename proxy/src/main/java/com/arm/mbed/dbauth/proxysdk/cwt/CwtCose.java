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
package com.arm.mbed.dbauth.proxysdk.cwt;

import com.arm.mbed.dbauth.proxysdk.cose.AlgorithmID;
import com.arm.mbed.dbauth.proxysdk.cose.Attribute;
import com.arm.mbed.dbauth.proxysdk.cose.CoseException;
import com.arm.mbed.dbauth.proxysdk.cose.HeaderKeys;
import com.arm.mbed.dbauth.proxysdk.cose.OneKey;
import com.arm.mbed.dbauth.proxysdk.cose.Sign1Message;

public class CwtCose {

    /**
     * Encodes this CWT with sign1.
     */
    public static byte[] sign1(byte[] cborObj, OneKey privateKey) throws CoseException {

        Sign1Message coseS1 = new Sign1Message();
        coseS1.addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED);
        coseS1.SetContent(cborObj);
        coseS1.sign(privateKey);

        return coseS1.EncodeToCBORObject().EncodeToBytes();
    }

}
