package me.aflak.libraries.ui.scan.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

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

    public ScanPresenterImpl(ScanView view, ScanInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void onCreate(final Activity activity) {
        startScanning();
        view.showPairedList(interactor.getPairedDevices());
    }

    @Override
    public void startScanning() {
        interactor.scanDevices(discoveryCallback);
        view.clearScanList();
        view.showProgress(true);
        view.enableScanButton(false);
        view.setScanStatus(R.string.bluetooth_scanning);
    }

    @Override
    public void scanItemClick(int position) {
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
        public void onFinish() {
            view.setScanStatus(R.string.bluetooth_scan_finished);
            view.showProgress(false);
            view.enableScanButton(true);
        }

        @Override
        public void onDevice(BluetoothDevice device) {
            view.addDeviceToScanList(device.getAddress()+" : "+device.getName());
        }

        @Override
        public void onPair(BluetoothDevice device) {
            view.navigateToChat("device", device);
        }

        @Override
        public void onUnpair(BluetoothDevice device) {
        }

        @Override
        public void onError(String message) {
            view.setScanStatus(message);
        }
    };
}
