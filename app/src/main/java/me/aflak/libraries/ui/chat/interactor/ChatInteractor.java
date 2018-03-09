package me.aflak.libraries.ui.chat.interactor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DeviceCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ChatInteractor {
    boolean isBluetoothEnabled();
    void enableBluetooth();
    void connectToDevice(BluetoothDevice device, DeviceCallback callback);
    void sendMessage(String message);
    void onStart(BluetoothCallback bluetoothCallback, Activity activity);
    void onStop();
}
