package com.arm.armsda.serial;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.arm.armsda.activities.SendMessageUsbActivity;
import com.arm.armsda.utils.AndroidUtils;
import com.arm.mbed.sda.proxysdk.IDevice;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceConnection {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mConnection;
    private UsbSerialPort mSerialPort;
    private PendingIntent mPermissionIntent = null;
    public static boolean isDeviceConnected = false;
    public static IDevice device;

    public void registerDevice(Context ctx) {
        mUsbManager = (UsbManager)ctx.getSystemService(Context.USB_SERVICE);

        // Register an intent filter so we can get permission to connect
        // to the device and get device attached/removed messages
        mPermissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        ctx.registerReceiver(mUsbReceiver, filter);
    }

    public void unregisterDevice(Context ctx) {
        ctx.unregisterReceiver(mUsbReceiver);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    connectDeviceEvent(context, intent);
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // Device removed
                synchronized (this) {
                    cleanDeviceConnection(context);
                    SendMessageUsbActivity.setDeviceConnectionIndication(false);
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                // Device attached
                synchronized (this) {
                    checkPermissionAndConnectDevice(context);
                }
            }
        }
    };

    private void connectDeviceEvent(Context ctx, Intent intent) {
        if ((mUsbDevice.getVendorId() == 3368) && (mUsbDevice.getProductId() == 516)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (device != null) {
                    connectToDevice(ctx);
                }
            } else {
                AndroidUtils.customToast(
                        ctx,
                        "Device permission denied",
                        Color.RED
                );
            }
        }
    }

    private void cleanDeviceConnection(Context ctx) {
        if (isDeviceConnected) {
            try {
                if (null != mConnection) {
                    mConnection.close();
                }
                if (null != mSerialPort) {
                    mSerialPort.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            isDeviceConnected = false;

            AndroidUtils.customToast(
                    ctx,
                    "Device Disconnected Successfully",
                    Color.BLUE
            );
        }
    }

    private void checkPermissionAndConnectDevice(Context ctx) {

        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        if (usbDevices.size() > 0) {
            Map.Entry<String, UsbDevice> entry = usbDevices.entrySet().iterator().next();
            mUsbDevice = usbDevices.get(entry.getKey());
            boolean hasPermision = mUsbManager.hasPermission(mUsbDevice);
            if (!hasPermision) {
                mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
            } else {
                connectToDevice(ctx);
            }
        }
        else {
            //TODO - cant see this print need to find out why
            AndroidUtils.customToast(
                    ctx,
                    "Unrecognized OR no device connected",
                    Color.RED
            );
        }
    }

    public void connectToDevice(Context ctx) {
        // Open a connection to the first available driver.

        List<UsbSerialDriver> availableDrivers;
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        mConnection = mUsbManager.openDevice(driver.getDevice());
        if (mConnection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            AndroidUtils.customToastWithTimer(ctx,
                    "OOPS - No Device Permission",
                    Color.RED,
                    Toast.LENGTH_LONG);
            return;
        }

        // Read some data! Most have just one port (port 0).
        mSerialPort = driver.getPorts().get(0);
        try {
            mSerialPort.open(mConnection);
            mSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        isDeviceConnected = true;
        SendMessageUsbActivity.setDeviceConnectionIndication(true);

        //Prepare device
        device = new ArmUsbDevice(mSerialPort, ctx);

        AndroidUtils.customToast(
                ctx,
                "Device Connected Successfully",
                Color.GREEN
        );
    }

}