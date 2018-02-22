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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
            URL resource = FileUtil.class.getClassLoader().getResource(fileName);
            if (resource != null) {
                filePath = resource.getFile();
            }
            else {
                throw new FileNotFoundException(fileName);
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

        FileWriter fileWriter = new FileWriter(fileName, false);
        BufferedWriter bufWriter = new BufferedWriter(fileWriter);

        bufWriter.write(content);
        bufWriter.flush();
        bufWriter.close();

        logger.debug("File Saved: " + new File(fileName).getAbsolutePath());
    }

    public static void saveBinaryFile(String fileName, byte[] bytes) throws IOException {
        FileOutputStream fOut = new FileOutputStream(fileName, false);
        BufferedOutputStream bufOut = new BufferedOutputStream(fOut);
        bufOut.write(bytes);
        bufOut.flush();
        bufOut.close();

        logger.debug("File Saved: " + new File(fileName).getAbsolutePath());
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

        FileReader fileReader = new FileReader(locateFilePath(fileName));
        BufferedReader bufReader = new BufferedReader(fileReader);
        while ((line = bufReader.readLine()) != null) {
            sb.append(line);
        }

        bufReader.close();

        logger.debug("Got " + sb.length() + " chars from: " + new File(fileName).getAbsolutePath());
        return sb.toString();
    }
}
