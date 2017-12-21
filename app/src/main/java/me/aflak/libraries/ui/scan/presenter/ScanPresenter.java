package me.aflak.libraries.ui.scan.presenter;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ScanPresenter {
    void scanItemClick(int position);
    void pairedItemClick(int position);
    void startScanning();
    void onStart();
    void onStop();
}
