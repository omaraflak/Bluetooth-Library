package me.aflak.bluetooth;

import android.app.Activity;

/**
 * Created by tribe on 1/29/18.
 */

public class ThreadHelper {
    public static void run(boolean runOnUi, Activity activity, Runnable runnable){
        if(runOnUi) {
            activity.runOnUiThread(runnable);
        }
        else{
            runnable.run();
        }
    }
}
