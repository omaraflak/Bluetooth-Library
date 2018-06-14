package me.aflak.libraries.ui.scan.interactor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DiscoveryCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public class ScanInteractorImpl implements ScanInteractor {
    private Bluetooth bluetooth;
    private DiscoveryCallback presenterDiscoveryCallback;
    private List<BluetoothDevice> discoveredDevices;

    public ScanInteractorImpl(Bluetooth bluetooth) {
        this.bluetooth = bluetooth;
        this.bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
            @Override
            public void onDiscoveryStarted() {
                presenterDiscoveryCallback.onDiscoveryStarted();
            }

            @Override
            public void onDiscoveryFinished() {
                presenterDiscoveryCallback.onDiscoveryFinished();
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                presenterDiscoveryCallback.onDeviceFound(device);
                discoveredDevices.add(device);
            }

            @Override
            public void onDevicePaired(BluetoothDevice device) {
                presenterDiscoveryCallback.onDevicePaired(device);
            }

            @Override
            public void onDeviceUnpaired(BluetoothDevice device) {
                presenterDiscoveryCallback.onDeviceUnpaired(device);
            }

            @Override
            public void onError(String message) {
                presenterDiscoveryCallback.onError(message);
            }
        });
    }

    @Override
    public void onStart(BluetoothCallback bluetoothCallback, Activity activity) {
        this.bluetooth.onStart();
        this.bluetooth.setCallbackOnUI(activity);
        this.bluetooth.setBluetoothCallback(bluetoothCallback);
    }

    @Override
    public void onStop() {
        this.bluetooth.onStop();
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
    public List<String> getPairedDevices() {
        List<String> items = new ArrayList<>();
        for(BluetoothDevice device : bluetooth.getPairedDevices()){
            items.add(device.getAddress()+" : "+device.getName());
        }
        return items;
    }

    @Override
    public void pair(int position) {
        bluetooth.pair(discoveredDevices.get(position));
    }

    @Override
    public void scanDevices(DiscoveryCallback callback) {
        presenterDiscoveryCallback = callback;
        discoveredDevices = new ArrayList<>();
        bluetooth.startScanning();
    }

    @Override
    public void stopScanning() {
        bluetooth.stopScanning();
    }

    @Override
    public BluetoothDevice getPairedDevice(int position) {
        if(position<bluetooth.getPairedDevices().size()){
            return bluetooth.getPairedDevices().get(position);
        }
        return null;
    }
}
