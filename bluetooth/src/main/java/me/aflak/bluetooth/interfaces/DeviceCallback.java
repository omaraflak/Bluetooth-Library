package me.aflak.bluetooth.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Omar on 20/12/2017.
 */

public interface DeviceCallback {
    void onDeviceConnected(BluetoothDevice device);
    void onDeviceDisconnected(BluetoothDevice device, String message);
    void onMessage(byte[] message);
    void onError(int errorCode);
    void onConnectError(BluetoothDevice device, String message);
}