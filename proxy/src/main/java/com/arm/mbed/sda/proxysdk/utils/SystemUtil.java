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
package com.arm.mbed.sda.proxysdk.utils;

public class SystemUtil {

    private static ConsoleLogger logger = new ConsoleLogger(SystemUtil.class);

    public static boolean isAndroid() {

        String javaVendor = System.getProperty("java.vendor");
        String javaVmVendor =  System.getProperty("java.vm.vendor");
        String javaVendorUrl =  System.getProperty("java.vm.vendor");

        boolean isAndroid = 
                (javaVendor != null && javaVendor.toLowerCase().contains("android")) ||
                (javaVmVendor != null && javaVmVendor.toLowerCase().contains("android")) ||
                (javaVendorUrl != null && javaVendorUrl.toLowerCase().contains("android"));

        logger.debug("Android Detected: " + isAndroid);
        return isAndroid;
    }

}
