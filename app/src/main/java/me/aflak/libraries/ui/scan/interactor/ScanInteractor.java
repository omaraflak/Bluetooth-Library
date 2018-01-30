package me.aflak.libraries.ui.scan.interactor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import java.util.List;

import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DiscoveryCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ScanInteractor {
    List<String> getPairedDevices();
    BluetoothDevice getPairedDevice(int position);
    void scanDevices(DiscoveryCallback callback);
    boolean isBluetoothEnabled();
    void enableBluetooth();
    void stopScanning();
    void pair(int position);
    void onStart(BluetoothCallback bluetoothCallback, Activity activity);
    void onStop();
}
