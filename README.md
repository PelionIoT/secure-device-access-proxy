# Secure Device Access Proxy SDK

This repository contains the Secure Device Access Proxy SDK which you can use in Android applications to implement a "proxy". A "proxy" is an application which logs into Mbed Cloud, requests an access token and then connects to the IoT device to instruct it to perform the requested operation. This is a Java/Android repository.


## Prerequisites

To build the SDK, you need -
* JDK 1.8 and above. It can be downloaded from http://www.oracle.com/technetwork/java/javase/downloads/index.html.
* Android SDK version 26, minimum supported 19. See our dependencies under */arm-sda-android/app/build.gradle

## Building the Proxy SDK

When developing your own Android application, you need to use the Secure Device Access SDK as a library in your project.

To build the SDK jar:

1. Make sure your JAVA_HOME is pointing to the Java JDK. For example -

Windows -

`> set JAVA_HOME=C:\Program Files\java\jdk1.8.0_131

Linux -

`> export JAVA_HOME=/usr/lib/java/jdk1.8.0_131

1. Make sure your ANDROID_HOME is pointing to the Android SDK. For example -

Windows -

`> set ANDROID_HOME=C:\Users\nimzim01\appdata\local\Android\Sdk

Linux -

`> export ANDROID_HOME=/usr/lib/android/sdk




1. In the root directory of the repo, run the following command:

`> gradlew build -x test -x check`

1. The SDK library that you need to include in your Android application can be found under **`_proxy/build/libs/proxy-all.jar_`**.

## Building The Demo Android APK

1. `cd` into **`_arm-sda-android_`**
1. Run:
```
> gradlew clean build -x test -x check
```
1. The Android application file is **`_arm-sda-android/app/build/outputs/apk/debug/app-debug.apk`**


## Installing the APK on an Android device

There are a number of ways to install applications on an Android device. We suggest using the following steps:

1. Enable **Unknown sources** in the settings of your Android device. This is typically found under **Lock screen and security**.
1. Connect your Android device to your PC using a USB cable.
1. Copy the APK file to your Android device.
1. On your Andorid device, uninstall a previous version of the APK, if one exists.
1. On your Android device, use a file manager to find the file you just copied, and click to install it.
