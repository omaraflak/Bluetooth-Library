package me.aflak.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface CommunicationCallback{
    void onConnect(BluetoothDevice device);
    void onDisconnect(BluetoothDevice device, String message);
    void onMessage(String message);
    void onError(String message);
    void onConnectError(BluetoothDevice device, String message);
}