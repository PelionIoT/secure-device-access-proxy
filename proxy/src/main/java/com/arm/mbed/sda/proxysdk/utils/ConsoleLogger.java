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

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
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
