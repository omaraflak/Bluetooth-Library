package me.aflak.bluetooth.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Omar on 20/12/2017.
 */

public interface DiscoveryCallback{
    void onDiscoveryStarted();
    void onDiscoveryFinished();
    void onDeviceFound(BluetoothDevice device);
    void onDevicePaired(BluetoothDevice device);
    void onDeviceUnpaired(BluetoothDevice device);
    void onError(int errorCode);
    void onPairingFailed(BluetoothDevice device);
}