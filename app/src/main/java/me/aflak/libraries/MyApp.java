package me.aflak.libraries;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import me.aflak.libraries.data.BluetoothModule;

public class MyApp extends Application {
    private static MyApp app;
    private BluetoothModule bluetoothModule;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        bluetoothModule = new BluetoothModule(this);

        // Normal app init code...

    }

    public static MyApp app() {
        return app;
    }

    public BluetoothModule bluetoothModule() {
        return bluetoothModule;
    }
}
