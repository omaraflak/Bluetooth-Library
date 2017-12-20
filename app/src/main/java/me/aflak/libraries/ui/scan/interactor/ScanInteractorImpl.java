package me.aflak.libraries.ui.scan.interactor;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
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
        this.bluetooth.initialize();
        this.bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
            @Override
            public void onFinish() {
                presenterDiscoveryCallback.onFinish();
            }

            @Override
            public void onDevice(BluetoothDevice device) {
                presenterDiscoveryCallback.onDevice(device);
                discoveredDevices.add(device);
            }

            @Override
            public void onPair(BluetoothDevice device) {
                presenterDiscoveryCallback.onPair(device);
            }

            @Override
            public void onUnpair(BluetoothDevice device) {
                presenterDiscoveryCallback.onUnpair(device);
            }

            @Override
            public void onError(String message) {
                presenterDiscoveryCallback.onError(message);
            }
        });
    }

    @Override
    public void enable() {
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
        bluetooth.connectToDevice(discoveredDevices.get(position));
    }

    @Override
    public void scanDevices(DiscoveryCallback callback) {
        presenterDiscoveryCallback = callback;
        discoveredDevices = new ArrayList<>();
        bluetooth.scanDevices();
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
