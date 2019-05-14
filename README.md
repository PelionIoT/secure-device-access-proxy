# Secure Device Access Proxy SDK

This repository contains the Secure Device Access Proxy SDK, which you can use in Android applications to implement a **proxy**. A proxy is an application that logs into Pelion Device Management, requests an access token, and then connects to the IoT device to instruct it to perform the requested operation. This is a Java/Android repository.


## Prerequisites

To build the SDK, you need:
* JDK 1.8 and above. It can be downloaded at http://www.oracle.com/technetwork/java/javase/downloads/index.html.
* Android SDK version 26, minimum supported 22 (5.1 Lollipop). See our dependencies under **/arm-sda-android/app/build.gradle**.

    If you are using Windows, you can download the latest Android Studio and SDK at https://developer.android.com/studio/.

<span class="notes">**Note:** After you install Android Studio, open it to complete the installation of the SDK and license agreement. To complete the installation, select **File** > **Settings** > **Appearance & Behavior** > **System Settings** > **Android SDK**, and install Android 8.0 (API level 26).</span>

## Building the Proxy SDK

When developing your own Android application, you need to use the Secure Device Access SDK as a library in your project.

To build the SDK jar:

1. Make sure your JAVA_HOME is pointing to the Java JDK. For example -

Windows -

`> set JAVA_HOME=C:\Program Files\java\jdk1.8.0_131`

Linux -

`> export JAVA_HOME=/usr/lib/java/jdk1.8.0_131`

Mac -
if /usr/libexec/java_home exists

`> export JAVA_HOME="$(/usr/libexec/java_home -v 1.8)"`

2. Make sure your ANDROID_HOME is pointing to the Android SDK. For example -

Windows -

`> set ANDROID_HOME=C:\Users\user\appdata\local\Android\Sdk`

Linux -

`> export ANDROID_HOME=/usr/lib/android/sdk`

Mac *(note: during the install process, the Android installer tells you where the SDK is)* -

`> export ANDROID_HOME= ~/Library/Android/sdk/`


3. In the root directory of the repo, run the following command:

`> gradlew build -x test -x check`

1. The SDK library that you need to include in your Android application can be found under **`proxy/build/libs/proxy-all.jar`**.

## Building the demo Android Package Kit (APK)

1. `cd` into **`arm-sda-android`**
1. Run:
```
> gradlew clean build -x test -x check
```
1. The Android application file is **`arm-sda-android/app/build/outputs/apk/debug/app-debug.apk`**


## Installing the APK on an Android device

There are a number of ways to install applications on an Android device. We suggest using the following steps:

1. Enable **Unknown sources** in the settings of your Android device. This is typically found under **Lock screen and security**.
1. Connect your Android device to your PC using a USB cable. If you are using a Mac, the Android device will not show up automatically as a USB mass storage device, and you will need to install an app to copy files from your Mac to the Android device.
1. Copy the APK file to your Android device.
1. On your Android device, uninstall a previous version of the APK, if one exists.
1. On your Android device, use a file manager to find the file you just copied, and click to install it.

## Serial log from Android app

This section describes how to retrieve the mbed_device log after connecting with the Android app:

The log is automatically activated & created.

The log is located in the “Download” directory in the device file system, the name of the log file is: Mbed_log.txt

Some guidelines regarding using the log mechanism:
1.	The log deletes itself each time the application starts.
1.	Leave the serial activity (or exit the application) before opening the logfile.
1.	The log can be partly filled with data when watching from PC, here is what need to be done in order to see the full log:
*	Open the file explorer on the mobile phone.
*	Rename the log file to any other name.
*	Watch the file from PC.
*	Note – this issue occurs due to MTP protocol refresh issue.

