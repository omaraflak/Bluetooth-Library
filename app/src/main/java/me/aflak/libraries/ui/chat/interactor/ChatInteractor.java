package me.aflak.libraries.ui.chat.interactor;

import android.bluetooth.BluetoothDevice;

import me.aflak.bluetooth.CommunicationCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ChatInteractor {
    void connectToDevice(BluetoothDevice device, CommunicationCallback callback);
    void sendMessage(String message);
}
