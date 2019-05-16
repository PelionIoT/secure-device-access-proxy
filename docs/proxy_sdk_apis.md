
# Using the SDA Proxy SDK

There is an SDK to help you create a proxy application for an Android device so that a user (such as service technician) can configure or maintain an IoT device. Android developers should use the (SDA Proxy SDK)[https://github.com/ARMmbed/secure-device-access-proxy-restricted/tree/master/proxy] as a library. There is an (Android demo app)[https://github.com/ARMmbed/secure-device-access-proxy-restricted/tree/master/arm-sda-android] using this SDK.

<span class="notes">**Note** Secure Device Access is a premium feature, so you will only have access to the SDK if you have paid for this feature.  If you believe that you should have access to this repository, contact the [Pelion Device Management support team](https://cloud.mbed.com/contact) to get access. </span>

## Proxy SDK APIs

A developer wanting to create an application to implement Secure Device Access will need to know the following class: 
https://github.com/ARMmbed/secure-device-access-proxy-restricted/blob/master/proxy/src/main/java/com/arm/mbed/sda/proxysdk/SecuredDeviceAccess.java

This class is the interface used to access the SDK functionality, you should call it from its application and use its methods as described below.

**Getting the access token from Pelion**

Use the following method to get an access token from Pelion:

`public static String getAccessToken( IAuthServer authServer, List<String> audience, String scope)`

* **Response:** String representation of the CWT access token OR "access denied".
* **Purpose:** This method requests an access token in CWT format from the AS (the authorization service in Pelion) using specific parameters. The AS is implemented in Pelion and is abstracted and given to the method as an object whose initialization is the responsibility of the application. The examples show how to create the parameters.
* **Parameters:**
    * `IAuthServer authServer` This object facilitates connection to the account in Pelion. See the implementing class `com.arm.mbed.dbauth.proxysdk.server.UserPasswordServer`, which can be instantiated by the application using its user and password to Pelion (requires login from the user in the app login screen and instantiate the object, or hard-code it, which is the less secure option). The login details should be of the user, such as a service technician - the person who wants to use the proxy to perform actions on the device. The Identity and Access Management (IAM) service should have policy to allows this user to get permission for this to end up in a token returned.

     **Example**

    ```
    ...
    private String accountId;
    private String username;
    private String password;
    ...
    IAuthServer authServer = new UserPasswordServer( this.authServerBaseUrl,
                    this.accountId,
                    this.username,
                    this.password );
    ...
    ```
    * `List<String> audience` Each element in the list is a legal audience member (device id or endpoint), for example:

    ```
    List audList = new ArrayList();
    audList.add("ep:name with a space");
    audList.add("id:1234567890abcdef1234567890abcdef");
    ...
    ```

    * `String scope` A string, even if a list of scopes (functions) is provided (delimited by space). For example:

    ```
    String scope = "lights doors fw-upgrade";
    ...
    ```

**Sending a message to the IoT device**

Use the following method to send a function to the IoT device:

`public static OperationResponse sendMessage( String accessToken, String cmd, ParamElement[] params, IDevice device )`

* **Response**: The `OperationResponse` object shows the response coming from the IoT device and it includes the following attributes:
     * Success or error code; at the IoT device side, the value is defined at: https://github.com/ARMmbed/secure-device-access-client-restricted/blob/master/secure-device-access/secure-device-access/sda_status.h
     * The optional blob (of type byte[]) returned from the device

    In the Proxy SDK it is part of the proprietary message protocol used with the device in the package: `com.arm.mbed.dbauth.proxysdk.protocol`. Relevant classes are:

    * `OperationResponse`
    * `ResponseBase`
    * `IResponse`
    * `OperationTypeEnum`

* **Purpose:** This method:

    * Connects to the device.
    * Creates an operation bundle, signed with the private PoP, including:
        * Access token.
        * Operation bundle.
        * Arguments.
        * Nonce.
    * Sends the operation bundle to the IoT device. The device is abstracted using the IDevice java interface, which you, as the application developer, will need to implement. We provide code examples for device using serial over USB and TCP in our repository - both are tested and working in our tests and demo.  You can choose to send this over whichever transport protocol is appropriate for your IoT device.

* **Parameters:**
    * `String accessToken` The base64 representation of the CWT token received from the SDA authorization service in the previous step.
    * `String cmd`  The action (operation) to perform on the device (originating from customer client application), the operation should match the scope requested in the access token.
    * `ParamElement[] params` Tuple list of optional command arguments. Two argument types are available (see the example below):
        * String (STR).
        * Int (INT).

    ```
    ParamElement[] cmdParams = new ParamElement[] {
                     new ParamElement(OperationArgumentType.STR, "Wan"),
                     new ParamElement(OperationArgumentType.INT, 130)
             };
    ```

     * `IDevice device`  This interface sends the bundle to the device. You should implement the single interface method according to your device type and chosen transport layer. We supply a code example for TCP device and Serial over a USB device. The interface is:

    ```
    IDevice
    package com.arm.mbed.dbauth.proxysdk;

    public interface IDevice {
        byte[] sendMessage(byte[] operationMsg);
    }
    ```

**Working with the OperationResponse**

***APIs***

** * MessageTypeEnum getType() ** - The type of response
** * int getResponseStatus(); ** - Status code number
** * byte[] getBlob(); ** - byte array representing optional binary data returned from the device
