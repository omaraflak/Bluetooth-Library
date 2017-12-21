package me.aflak.libraries.ui.chat.interactor;

import android.bluetooth.BluetoothDevice;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.CommunicationCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public class ChatInteractorImpl implements ChatInteractor {
    private Bluetooth bluetooth;

    public ChatInteractorImpl(Bluetooth bluetooth) {
        this.bluetooth = bluetooth;
    }

    @Override
    public boolean isBluetoothEnabled() {
        return bluetooth.isEnabled();
    }

    @Override
    public void enableBluetooth() {
        bluetooth.enable();
    }

    @Override
    public void connectToDevice(BluetoothDevice device, CommunicationCallback callback) {
        bluetooth.setCommunicationCallback(callback);
        bluetooth.connectToDevice(device);
    }

    @Override
    public void sendMessage(String message) {
        bluetooth.send(message);
    }

    @Override
    public void onStart(BluetoothCallback bluetoothCallback) {
        bluetooth.onStart();
        bluetooth.setBluetoothCallback(bluetoothCallback);
    }

    @Override
    public void onStop() {
        bluetooth.onStop();
    }
}
