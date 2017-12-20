package me.aflak.libraries.ui.chat.interactor;

import android.bluetooth.BluetoothDevice;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.CommunicationCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public class ChatInteractorImpl implements ChatInteractor {
    private Bluetooth bluetooth;

    public ChatInteractorImpl(Bluetooth bluetooth) {
        this.bluetooth = bluetooth;
        this.bluetooth.initialize();
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
}
