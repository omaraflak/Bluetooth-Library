package me.aflak.libraries.ui.scan.data;

import javax.inject.Singleton;

import dagger.Component;
import me.aflak.libraries.data.BluetoothModule;
import me.aflak.libraries.ui.scan.view.ScanActivity;

/**
 * Created by Omar on 20/12/2017.
 */

@Singleton
@Component(modules = {BluetoothModule.class, ScanModule.class})
public interface ScanComponent {
    void inject(ScanActivity scanActivity);
}
