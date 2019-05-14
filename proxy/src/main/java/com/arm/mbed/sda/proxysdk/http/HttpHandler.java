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
package com.arm.mbed.sda.proxysdk.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.utils.ConsoleLogger;

public class HttpHandler {

    ConsoleLogger logger = new ConsoleLogger(this.getClass());

    private static HttpHandler instance;
    private static final int HTTP_ERROR_CODES = 400;

    public static synchronized HttpHandler getInstance() {
        if (null == instance) {
            instance = new HttpHandler();
        }
        return instance;
    }

    public String httpRequest(
            String url,
            String json,
            int timeoutMillis,
            String method) {

        return httpRequest(null, url, json, timeoutMillis, method, false);
    }

    public String httpRequest(
            String jwt,
            String url,
            String json,
            int timeout,
            String method,
            boolean isAuthRequired) {

        logger.debug("Sending HTTP request, url: " + url + ", json: " + json + ", method:" + method +
                "\n jwt: " + (!isAuthRequired ? "N/A" : ((null == jwt) ? "jwt is null" : jwt)));

        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod(method);
            //set the sending type and receiving type to json
            connection.setRequestProperty("Content-Type", "application/json");

            //Temporary until we will define the APIGW flow.
            if (isAuthRequired && null != jwt) {
                connection.setRequestProperty("Authorization", "Bearer " + jwt);
            }

            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            if (json != null) {
                //set the content length of the body
                connection.setRequestProperty("Content-length", json.getBytes().length + "");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //send the json as body of the request
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(json.getBytes("UTF-8"));
                outputStream.close();
            }

            //Connect to the server
            connection.connect();

            int status = connection.getResponseCode();

            BufferedReader bufferedReader;
            if (status < HTTP_ERROR_CODES ) {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //sb appender warn you when you append string in its input.
                String msg = line + "\n";
                sb.append(msg);
            }
            bufferedReader.close();

            logger.debug("HTTP response, status: " + status + ", payload: " + sb.toString());
            switch (status) {
                case 200:
                case 201:
                    return sb.toString();
                case 400:
                case 401:
                case 404:
                    logger.error("HTTP ERROR response: " + sb.toString());
                    throw new HttpErrorResponseException(sb.toString());
                default:
                    logger.error("HTTP response with unexpected error code, status code: " +
                            status + ", error msg: " + sb.toString());
                    throw new HttpErrorResponseException(sb.toString());
            }
        } catch (IOException e) {
            logger.error("HttpHandler: Error handling http request: " + e.toString() );
            throw new ProxyException("HttpHandler: Error handling http request:\n " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {}
            }
        }
    }
}
