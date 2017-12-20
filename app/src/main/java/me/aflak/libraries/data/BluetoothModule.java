package me.aflak.libraries.data;

import android.app.Activity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.aflak.bluetooth.Bluetooth;

/**
 * Created by Omar on 20/12/2017.
 */

@Module
public class BluetoothModule {
    private Activity activity;

    public BluetoothModule(Activity activity) {
        this.activity = activity;
    }

    @Provides @Singleton
    public Activity provideActivity(){
        return activity;
    }

    @Provides @Singleton
    public Bluetooth provideBluetooth(Activity activity){
        return new Bluetooth(activity);
    }
}
