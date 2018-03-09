package me.aflak.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface DeviceCallback {
    void onDeviceConnected(BluetoothDevice device);
    void onDeviceDisconnected(BluetoothDevice device, String message);
    void onMessage(String message);
    void onError(String message);
    void onConnectError(BluetoothDevice device, String message);
}