package me.aflak.bluetooth;

/**
 * Created by Omar on 20/12/2017.
 */

public interface BluetoothCallback {
    void onBluetoothTurningOn();
    void onBluetoothOn();
    void onBluetoothTurningOff();
    void onBluetoothOff();
    void onUserDeniedActivation();
}
