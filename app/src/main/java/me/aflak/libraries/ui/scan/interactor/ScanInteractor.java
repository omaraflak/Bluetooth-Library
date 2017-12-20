package me.aflak.libraries.ui.scan.interactor;

import android.bluetooth.BluetoothDevice;

import java.util.List;

import me.aflak.bluetooth.DiscoveryCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ScanInteractor {
    List<String> getPairedDevices();
    BluetoothDevice getPairedDevice(int position);
    void scanDevices(DiscoveryCallback callback);
    void enable();
    void stopScanning();
    void pair(int position);
}
