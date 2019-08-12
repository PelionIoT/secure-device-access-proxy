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
package com.arm.mbed.sda.proxysdk.http;

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
