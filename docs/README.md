# Proxy API documentation

The documentation in this folder is for the proxy SDK and the SDA Service running in Pelion Device Management. The SDK takes care of Device Management access given credentials, but the service APIs are documented here for completeness. The SDK APIs are used by Android application developers to get an access token and send the operation bundle message to the device as part of the SDA flow.

## Proxy SDK documentation
Located here at [proxy_sdk_apis.md](https://github.com/ARMmbed/secure-device-access-proxy-sources-internal/blob/master/docs/proxy_sdk_apis.md)

## SDA Services APIs

the file [sda.yaml](sda.yaml) contains the swagger documentation of the _Secure Device Access_ Service that the proxy SDK is using.

The server API being used is the   **_/ace-auth/token_** to retrieve an access token to be used on the device.

You can find more about swagger in:
[Swagger Page](https://swagger.io/)
[Swagger Editor](https://editor.swagger.io/) : can be used to view the API in an html format.
