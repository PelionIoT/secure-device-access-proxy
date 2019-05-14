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
