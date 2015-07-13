package jp.plen.plenconnect.ble.dummy;

import android.app.Activity;

public class BLEDevice {
    private static final String TAG = BLEDevice.class.getSimpleName();

    public BLEDevice(Activity activity) {
    }

    public void setBLECallbacks(BLECallbacks callbacks) {
    }

    public void write(String command) {
    }

    public void close() {
    }

    public interface BLECallbacks {
        void onConnected(String deviceName);
    }


}
