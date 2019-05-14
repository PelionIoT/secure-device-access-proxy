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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.arm.mbed.sda.proxysdk.ProxyException;

public class FileUtil {

    private static ConsoleLogger logger = new ConsoleLogger(FileUtil.class);

    /**
     * ---[  Locate File  ]---
     */
    public static String locateFilePath(String fileName) throws FileNotFoundException {
        // Null, unless found.
        String filePath;

        // Look for in the default location.
        File file = new File(fileName);

        // Try to look for anywhere in the classpath.
        if (!file.exists()) {
            ClassLoader classLoader = FileUtil.class.getClassLoader();
            if (null != classLoader) {
                URL resource = classLoader.getResource(fileName);
                if (resource != null) {
                    filePath = resource.getFile();
                } else {
                    throw new FileNotFoundException(fileName);
                }
            } else {
                throw new ProxyException("Failed to get ClassLoader while locating path for file " + fileName);
            }
        }
        else {
            filePath = file.getAbsolutePath();
        }

        logger.debug("File Name: " + fileName + " located at: " + filePath);
        return filePath;
    }


    /**
     * ---[  Save File  ]---
     */
    public static void saveFile(String fileName, String content) throws IOException {

        OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(fileName, false),
                                                        StandardCharsets.UTF_8);
        BufferedWriter bufWriter = new BufferedWriter(osr);

        try {
            bufWriter.write(content);
            bufWriter.flush();
            logger.debug("File Saved: " + new File(fileName).getAbsolutePath());
        } catch(IOException ex) {
            String errorMsg = String.format("Failed to write to file %s with error: %s",
                                            new File(fileName).getAbsolutePath(), ex.getMessage());
            logger.error(errorMsg);
            throw ex;
        } finally {
        	bufWriter.close();
        }
    }

    public static void saveBinaryFile(String fileName, byte[] bytes) throws IOException {

        FileOutputStream fOut = new FileOutputStream(fileName, false);
        BufferedOutputStream bufOut = new BufferedOutputStream(fOut);
        try {
            bufOut.write(bytes);
            bufOut.flush();
            logger.debug("File Saved: " + new File(fileName).getAbsolutePath());
        } catch(IOException ex) {
            String errorMsg = String.format("Failed to save file %s with error: %s",
                                            new File(fileName).getAbsolutePath(), ex.getMessage());
            logger.error(errorMsg);
            throw ex;
        } finally {
            bufOut.close();
        }
    }

    public static byte[] readBinaryFile(String fileName) throws IOException {

        Path path = new File(locateFilePath(fileName)).toPath();
        byte[] bytes = Files.readAllBytes(path);

        logger.debug("Got " + bytes.length + " bytes from: " + path.toString());
        return bytes;
    }

    /**
     * ---[  Read File  ]--- 
     */
    public static String readFile(String fileName) throws IOException {

        StringBuilder sb = new StringBuilder();
        String line;

        InputStreamReader isr = new InputStreamReader(new FileInputStream(locateFilePath(fileName)),
                                                      StandardCharsets.UTF_8);
        BufferedReader bufReader = new BufferedReader(isr);
        try {
            while ((line = bufReader.readLine()) != null) {
                sb.append(line);
            }
            logger.debug("Got " + sb.length() + " chars from: " + new File(fileName).getAbsolutePath());
            return sb.toString();
        } catch (IOException ex) {
            String errorMsg = String.format("Failed to read file %s with error: %s",
                                            new File(fileName).getAbsolutePath(), ex.getMessage());
            logger.error(errorMsg);
            throw ex;
        } finally {
            bufReader.close();
        }
    }
}
