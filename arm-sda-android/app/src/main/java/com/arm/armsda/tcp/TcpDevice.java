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
package com.arm.armsda.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.arm.mbed.dbauth.proxysdk.ProxyException;
import com.arm.mbed.dbauth.proxysdk.devices.AbstractDevice;
import com.arm.mbed.dbauth.proxysdk.protocol.ProtocolConstants;
import com.arm.mbed.dbauth.proxysdk.utils.ConsoleLogger;

public class TcpDevice extends AbstractDevice {

    private String ip;
    private int port;

    /**
     * ---[ Constructor ]---
     * 
     * @param ip
     * @param port
     */
    public TcpDevice(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public byte[] sendMessage(byte[] operationMessage) {
        return TcpHandler.sendMessage(operationMessage, ip, port);
    }
    
    
    
    private static class TcpHandler {

        private static final ConsoleLogger logger = new ConsoleLogger(TcpHandler.class);
        private static final int socketTimeout = 3000;

        public synchronized static byte[] sendMessage(byte[] message, String ip, int port) {
            logger.debug(String.format("Sending Message to IP:PORT [%s:%d]", ip,port));

            if (message == null) {
                throw new ProxyException("InputMessage to device is null");
            }

            Socket deviceSocket = new Socket();

            //Prepare socket
            try {
                logger.debug("Connecting to Device...");
                deviceSocket.connect(new InetSocketAddress(ip, port), socketTimeout);
                logger.debug(String.format("Connected to [%s:%d].", ip,port));
            }
            catch (SocketTimeoutException e) {
                logger.error("TIMEOUT: " + e.getMessage());
                if (!deviceSocket.isClosed()){
                    try {
                        deviceSocket.close();
                    } catch (IOException e1) {}
                }
                throw new ProxyException("TIMEOUT: Failed to open socket " + ip + ":" + port, e);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
                if (!deviceSocket.isClosed()){
                    try {
                        deviceSocket.close();
                    } catch (IOException e1) {}
                }
                throw new ProxyException("Failed to open socket " + ip + ":" + port, e);
            }

            //Set In/Out streams
            DataOutputStream output;
            DataInputStream input;
            //TODO:: Consider using try-with-resource statements for automatic resource management
            //http://javarevisited.blogspot.co.uk/2014/10/right-way-to-close-inputstream-file-resource-in-java.html
            try {
                logger.debug("Getting Input and Output Streams from the Device...");
                output = new DataOutputStream( new BufferedOutputStream(deviceSocket.getOutputStream()) );
                input = new DataInputStream( new BufferedInputStream( deviceSocket.getInputStream()) );
            }
            catch (IOException e) {
                logger.error(e.getMessage());
                try {
                    logger.debug("Closing Connection due to error...");
                    deviceSocket.close();
                    logger.debug("Connection Closed due to error.");
                } 
                catch (IOException e1) {
                    // ignore if unable to close the socket. exception is raised anyway now...
                    logger.error("Unable to close the device socket after error");
                }
                throw new ProxyException("Failed to open I/O streams on the socket " + ip + ":" + port, e);
            }

            //Build message to send
            logger.debug("Building the Biunary Message to send...");
            ByteBuffer bb = ByteBuffer.allocate( ProtocolConstants.LENGTH_SIZE + message.length)
                    .order(ByteOrder.BIG_ENDIAN);

            bb.putInt(message.length);
            bb.put(message);
            byte[] messageToSend = bb.array();

            byte[] receivedMessage;
            //Write message
            try {
                logger.debug(String.format("About to send the message as Big-Endian, "
                        + "Declared Content Length: %d.  "
                        + "Total Size including Header: %d", 
                        message.length, messageToSend.length));
                
                output.write(messageToSend);
                output.flush();
                logger.debug("Message Sent.  Getting Response...");
                //Read message
                int count = input.readInt();
                receivedMessage = new byte[count];
                logger.debug(String.format("Getting the Response from the Device.  Total size to read: %d bytes.",count) );

                if (count > 0) {
                    ((DataInputStream) input).readFully(receivedMessage);
                    logger.debug("Successfully read the response.");
                }
            } 
            catch (IOException e) {
                throw new ProxyException("Failed to send/receive message to " + ip + ":" + port, e);
            }
            finally {
                try {
                    if (output != null){
                        logger.debug("Finally, Closing the Output Stream to the device...");
                        output.close();
                        logger.debug("Ouput Stream to the device - Closed.");
                    }
                    if (input != null){
                        logger.debug("Finally, Closing the Input Stream from the device...");
                        input.close();
                        logger.debug("Input Stream from the device - Closed.");
                    }
                    if (!deviceSocket.isClosed()){
                        logger.debug("Finally, Closing the Socket to the device...");
                        deviceSocket.close();
                        logger.debug("Socket to the device - Closed.");
                    }
                } catch  (IOException e) {
                    logger.error(String.format("Unexpected IO-Exception while trying to colse communication resources from the device.  Message: %s",e.toString()));
                }
            } 

            logger.debug(String.format("Response from the device received.  Total Bytes: %d.", receivedMessage.length));
            return receivedMessage;
        }
    }
}
