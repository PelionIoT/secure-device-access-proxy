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
package com.arm.mbed.dbauth.proxysdk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private Class<? extends Object> logClass;
    private static Boolean isDebugEnabled = Boolean.TRUE;
    private static Boolean isErrorEnabled = Boolean.TRUE;

    public ConsoleLogger(Class<? extends Object> logClass) {
        this.logClass = logClass;
    }

    public void debug(String msg) {
        if (isDebugEnabled) {
            System.out.println( formatLog(msg, "DEBUG") );
        }
    }

    public void error(String msg) {
        if (isErrorEnabled) {
            System.err.println( formatLog(msg, "ERROR") );
        }
    }

    private String formatLog(String msg, String logLevel) {
        String log = String.format(
                "[%s|%s| %s] %s", 
                sdf.format(new Date()), 
                logLevel, 
                this.logClass.getSimpleName(), 
                msg );
        return log;
    }

    public void setDebugEnabled(Boolean isEnabled) {
        ConsoleLogger.isDebugEnabled = isEnabled;

        // When enabling DEBUG, the ERROR is also enabled.
        if (isEnabled) {
            ConsoleLogger.isErrorEnabled = Boolean.TRUE;
        }

    }

    public static Boolean isDebugEnabled() {
        return ConsoleLogger.isDebugEnabled;
    }

    public static void setErrorEnabled(Boolean isEnabled) {
        ConsoleLogger.isErrorEnabled = isEnabled;
    }

    public static Boolean isErrorEnabled() {
        return ConsoleLogger.isErrorEnabled;
    }

    public void forceInfo(String msg) {
        System.out.println( formatLog(msg, "INFO") );
    }

}
