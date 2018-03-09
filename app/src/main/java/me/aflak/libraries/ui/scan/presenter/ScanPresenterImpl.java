package me.aflak.libraries.ui.scan.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DiscoveryCallback;
import me.aflak.libraries.R;
import me.aflak.libraries.ui.scan.interactor.ScanInteractor;
import me.aflak.libraries.ui.scan.view.ScanView;

/**
 * Created by Omar on 20/12/2017.
 */

public class ScanPresenterImpl implements ScanPresenter{
    private ScanView view;
    private ScanInteractor interactor;
    private boolean canceledDiscovery = false;

    public ScanPresenterImpl(ScanView view, ScanInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void onStart(Activity activity) {
        interactor.onStart(bluetoothCallback, activity);
        if(interactor.isBluetoothEnabled()){
            startScanning();
            view.showPairedList(interactor.getPairedDevices());
        }
        else{
            interactor.enableBluetooth();
        }
    }

    @Override
    public void onStop() {
        interactor.onStop();
    }

    @Override
    public void startScanning() {
        view.clearScanList();
        view.showProgress(true);
        view.enableScanButton(false);
        view.setScanStatus(R.string.bluetooth_scanning);
        interactor.scanDevices(discoveryCallback);
        canceledDiscovery = false;
    }

    @Override
    public void scanItemClick(int position) {
        canceledDiscovery = true;
        interactor.stopScanning();
        interactor.pair(position);
        view.setScanStatus(R.string.bluetooth_pairing);
        view.showProgress(true);
    }

    @Override
    public void pairedItemClick(int position) {
        BluetoothDevice device = interactor.getPairedDevice(position);
        view.navigateToChat("device", device);
    }

    private DiscoveryCallback discoveryCallback = new DiscoveryCallback() {
        @Override
        public void onDiscoveryStarted() {
            view.showToast("Discovery started");
        }

        @Override
        public void onDiscoveryFinished() {
            if(!canceledDiscovery){
                view.setScanStatus(R.string.bluetooth_scan_finished);
                view.showProgress(false);
                view.enableScanButton(true);
            }
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            view.addDeviceToScanList(device.getAddress()+" : "+device.getName());
        }

        @Override
        public void onDevicePaired(BluetoothDevice device) {
            view.navigateToChat("device", device);
        }

        @Override
        public void onDeviceUnpaired(BluetoothDevice device) {
        }

        @Override
        public void onError(String message) {
            view.setScanStatus(message);
        }
    };

    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
            view.setScanStatus(R.string.bluetooth_turning_on);
        }

        @Override
        public void onBluetoothOn() {
            startScanning();
            view.showPairedList(interactor.getPairedDevices());
        }

        @Override
        public void onBluetoothTurningOff() {
            interactor.stopScanning();
            view.showToast("You need to enable your bluetooth...");
        }

        @Override
        public void onBluetoothOff() {
        }

        @Override
        public void onUserDeniedActivation() {
        }
    };
}
