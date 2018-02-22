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
package com.arm.mbed.dbauth.proxysdk.protocol;

public class ProtocolConstants {

    public final static int BYTES_IN_INT            = Integer.SIZE/8;
    public final static int LENGTH_SIZE             = BYTES_IN_INT;
    public final static int CBOR_TAG_SIZE           = BYTES_IN_INT; // TBD
    public final static int RESPONSE_STATUS_SIZE    = BYTES_IN_INT;

    public final static int MIN_MSG_SIZE            = RESPONSE_STATUS_SIZE;

    public final static int MSG_TYPE_INDEX          = 1;
    public final static int RESPONSE_STATUS_INDEX   = 2;
    public final static int RESPONSE_PAYLOAD_INDEX  = 3;

    public final static int STATUS_OK               = 0;
}
