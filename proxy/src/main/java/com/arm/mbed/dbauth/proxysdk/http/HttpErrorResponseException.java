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
package com.arm.mbed.dbauth.proxysdk.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class HttpErrorResponseException extends RuntimeException {

    private static final long serialVersionUID = -5127944914044744005L;
    private int httpErrorStatusCode;
    private String httpErrorMessage;


    public HttpErrorResponseException(String msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ErrorObjectResponse errorObjectResponse = objectMapper.readValue(msg, ErrorObjectResponse.class);
            httpErrorStatusCode = errorObjectResponse.getCode();
            httpErrorMessage = errorObjectResponse.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getHttpErrorStatusCode() {
        return httpErrorStatusCode;
    }

    public String getHttpErrorMessage() {
        return httpErrorMessage;
    }
}
