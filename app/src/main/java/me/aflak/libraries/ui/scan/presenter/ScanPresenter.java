package me.aflak.libraries.ui.scan.presenter;

import android.app.Activity;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ScanPresenter {
    void scanItemClick(int position);
    void pairedItemClick(int position);
    void startScanning();
    void onStart(Activity activity);
    void onStop();
}
