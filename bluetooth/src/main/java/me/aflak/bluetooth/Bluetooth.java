package me.aflak.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.aflak.bluetooth.constants.DeviceError;
import me.aflak.bluetooth.constants.DiscoveryError;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;
import me.aflak.bluetooth.reader.LineReader;
import me.aflak.bluetooth.reader.SocketReader;
import me.aflak.bluetooth.utils.ThreadHelper;

/**
 * Created by Omar on 14/07/2015.
 */
public class Bluetooth {
    private final static String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private final static int REQUEST_ENABLE_BT = 1111;

    private Activity activity;
    private Context context;
    private UUID uuid;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private DeviceCallback deviceCallback;
    private DiscoveryCallback discoveryCallback;
    private BluetoothCallback bluetoothCallback;

    private ReceiveThread receiveThread;
    private boolean connected, runOnUi;

    private Class readerClass;

    /**
     * Init Bluetooth object. Default UUID will be used.
     * @param context Context to be used.
     */
    public Bluetooth(Context context){
        initialize(context, UUID.fromString(DEFAULT_UUID));
    }

    /**
     * Init Bluetooth object.
     * @param context Context to be used.
     * @param uuid Socket UUID to be used.
     */
    public Bluetooth(Context context, UUID uuid){
        initialize(context, uuid);
    }

    private void initialize(Context context, UUID uuid){
        this.context = context;
        this.uuid = uuid;
        this.readerClass = LineReader.class;
        this.deviceCallback = null;
        this.discoveryCallback = null;
        this.bluetoothCallback = null;
        this.connected = false;
        this.runOnUi = false;
    }

    /**
     * Start bluetooth service.
     */
    public void onStart(){
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * Stop bluetooth service.
     */
    public void onStop(){
        context.unregisterReceiver(bluetoothReceiver);
    }

    /**
     * Prompt user to enable bluetooth. onActivityResult() must be called to catch response.
     * @param activity Activity used to display the dialog.
     */
    public void showEnableDialog(Activity activity){
        if(bluetoothAdapter!=null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    /**
     * Enable bluetooth without asking the user.
     */
    public void enable(){
        if(bluetoothAdapter!=null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    /**
     * Disable bluetooth without asking the user.
     */
    public void disable(){
        if(bluetoothAdapter!=null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    /**
     * Get BluetoothSocket used for connection.
     * @return BluetoothSocket.
     */
    public BluetoothSocket getSocket(){
        return receiveThread.getSocket();
    }

    /**
     * Get BluetoothManager.
     * @return BluetoothManager.
     */
    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    /**
     * Get BluetoothAdapter.
     * @return BluetoothAdapter.
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Check if bluetooth is enabled.
     * @return true if bluetooth is enabled, false otherwise.
     */
    public boolean isEnabled(){
        if(bluetoothAdapter!=null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**
     * This will call all the listeners on the main thread, so you don't have to runOnUiThread if you need to change some UI.
     * @param activity The main Activity.
     */
    public void setCallbackOnUI(Activity activity){
        this.activity = activity;
        this.runOnUi = true;
    }

    public void setReader(Class readerClass){
        this.readerClass = readerClass;
    }

    /**
     * Handle the result of showEnableDialog().
     * @param requestCode requestCode given in onActivityResult.
     * @param resultCode resultCode given in onActivityResult.
     */
    public void onActivityResult(int requestCode, final int resultCode){
        if(bluetoothCallback!=null){
            if(requestCode==REQUEST_ENABLE_BT){
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        if(resultCode==Activity.RESULT_CANCELED){
                            bluetoothCallback.onUserDeniedActivation();
                        }
                    }
                });
            }
        }
    }

    /**
     * Connect to bluetooth device using its address.
     * @param address Device address.
     * @param insecureConnection True if you don't need the data to be encrypted.
     * @param withPortTrick https://stackoverflow.com/a/25647197/5552022.
     */
    public void connectToAddress(String address, boolean insecureConnection, boolean withPortTrick) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectToDevice(device, insecureConnection, withPortTrick);
    }

    /**
     * Connect to bluetooth device using its address.
     * @param address Device address.
     */
    public void connectToAddress(String address) {
        connectToAddress(address, false, false);
    }

    /**
     * Connect to bluetooth device using its address and setting port to 1: https://stackoverflow.com/a/25647197/5552022.
     * @param address Device address.
     */
    public void connectToAddressWithPortTrick(String address) {
        connectToAddress(address, false, true);
    }

    /**
     * Connect to device already paired, using its name.
     * @param name Device name.
     * @param insecureConnection True if you don't need the data to be encrypted.
     * @param withPortTrick https://stackoverflow.com/a/25647197/5552022.
     */
    public void connectToName(String name, boolean insecureConnection, boolean withPortTrick) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToDevice(blueDevice, insecureConnection, withPortTrick);
                return;
            }
        }
    }

    /**
     * Connect to device already paired, using its name.
     * @param name Device name.
     */
    public void connectToName(String name) {
        connectToName(name, false, false);
    }

    /**
     * Connect to device already paired, using its name and setting port to 1: https://stackoverflow.com/a/25647197/5552022.
     * @param name Device name.
     */
    public void connectToNameWithPortTrick(String name) {
        connectToName(name, false, true);
    }

    /**
     * Connect to bluetooth device.
     * @param device Bluetooth device.
     * @param insecureConnection True if you don't need the data to be encrypted.
     * @param withPortTrick https://stackoverflow.com/a/25647197/5552022.
     */
    public void connectToDevice(final BluetoothDevice device, boolean insecureConnection, boolean withPortTrick){
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        connect(device, insecureConnection, withPortTrick);
    }

    /**
     * Connect to bluetooth device.
     * @param device Bluetooth device.
     */
    public void connectToDevice(BluetoothDevice device){
        connectToDevice(device, false, false);
    }

    /**
     * Connect to bluetooth device.
     * @param device Bluetooth device.
     */
    public void connectToDeviceWithPortTrick(BluetoothDevice device){
        connectToDevice(device, false, false);
    }

    /**
     * Disconnect from bluetooth device.
     */
    public void disconnect() {
        try {
            receiveThread.getSocket().close();
        } catch (final IOException e) {
            if(deviceCallback !=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        deviceCallback.onError(DeviceError.FAILED_WHILE_CLOSING);
                    }
                });
            }
        }
    }

    /**
     * Check if the current device is connected to a bluetooth device.
     * @return True if connected, false otherwise.
     */
    public boolean isConnected(){
        return connected;
    }

    /**
     * Send byte array to the connected device.
     * @param data byte array to be sent.
     */
    public void send(byte[] data){
        OutputStream out = receiveThread.getOutputStream();
        try {
            out.write(data);
        } catch (final IOException e) {
            connected = false;
            if(deviceCallback !=null){
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        deviceCallback.onDeviceDisconnected(receiveThread.getDevice(), e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Send string message to the connected device.
     * @param msg String message.
     * @param charset Charset used to encode the String. Default charset is UTF-8.
     */
    public void send(String msg, Charset charset){
        if(charset==null){
            send(msg.getBytes());
        } else{
            send(msg.getBytes(charset));
        }
    }

    /**
     * Send string message to the connected device.
     * @param msg String message.
     */
    public void send(String msg){
        send(msg, null);
    }

    /**
     * Get list of paired devices.
     * @return List of BluetoothDevice.
     */
    public List<BluetoothDevice> getPairedDevices(){
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }

    /**
     * Start scanning for nearby bluetooth devices.
     */
    public void startScanning(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        context.registerReceiver(scanReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    /**
     * Stop scanning for nearby bluetooth devices.
     */
    public void stopScanning(){
        context.unregisterReceiver(scanReceiver);
        bluetoothAdapter.cancelDiscovery();
    }

    /**
     * Pair with a specific bluetooth device.
     * @param device Bluetooth device.
     * @param pin String pin used for pairing.
     */
    public void pair(BluetoothDevice device, String pin){
        if(pin != null) {
            device.setPin(pin.getBytes());
        }
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if(discoveryCallback!=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallback.onError(DiscoveryError.FAILED_TO_PAIR);
                    }
                });
            }
        }
    }

    /**
     * Pair with a specific bluetooth device.
     * @param device Bluetooth device.
     */
    public void pair(BluetoothDevice device){
        pair(device, null);
    }

    /**
     * Forget a device.
     * @param device Bluetooth device.
     */
    public void unpair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if(discoveryCallback!=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallback.onError(DiscoveryError.FAILED_TO_UNPAIR);
                    }
                });
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, boolean insecureConnection){
        BluetoothSocket socket = null;
        try {
            if(insecureConnection){
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            }
            else{
                socket = device.createRfcommSocketToServiceRecord(uuid);
            }
        } catch (IOException e) {
            if(deviceCallback !=null){
                Log.w(getClass().getSimpleName(), e.getMessage());
                deviceCallback.onError(DeviceError.FAILED_WHILE_CREATING_SOCKET);
            }
        }
        return socket;
    }

    private BluetoothSocket createBluetoothSocketWithPortTrick(BluetoothDevice device){
        BluetoothSocket socket = null;
        try {
            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(getClass().getSimpleName(), e.getMessage());
        }
        return socket;
    }

    private void connect(BluetoothDevice device, boolean insecureConnection, boolean withPortTrick){
        BluetoothSocket socket = null;
        if(withPortTrick){
            socket = createBluetoothSocketWithPortTrick(device);
        }
        if(socket==null){
            try {
                if(insecureConnection){
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                }
                else{
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                if(deviceCallback !=null){
                    Log.w(getClass().getSimpleName(), e.getMessage());
                    deviceCallback.onError(DeviceError.FAILED_WHILE_CREATING_SOCKET);
                }
            }
        }
        connectInThread(socket, device);
    }

    private void connectInThread(final BluetoothSocket socket, final BluetoothDevice device){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect();
                    connected = true;
                    receiveThread = new ReceiveThread(readerClass, socket, device);
                    if(deviceCallback !=null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onDeviceConnected(device);
                            }
                        });
                    }
                    receiveThread.start();
                } catch (final IOException e) {
                    if(deviceCallback !=null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onConnectError(device, e.getMessage());
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private class ReceiveThread extends Thread implements Runnable{
        private SocketReader reader;
        private BluetoothSocket socket;
        private BluetoothDevice device;
        private OutputStream out;

        public ReceiveThread(Class<?> readerClass, BluetoothSocket socket, BluetoothDevice device) {
            this.socket = socket;
            this.device = device;
            try {
                out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                this.reader = (SocketReader) readerClass.getDeclaredConstructor(InputStream.class).newInstance(in);
            } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Log.w(getClass().getSimpleName(), e.getMessage());
            }
        }

        public void run(){
            byte[] msg;
            try {
                while((msg = reader.read()) != null) {
                    if(deviceCallback != null){
                        final byte[] msgCopy = msg;
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onMessage(msgCopy);
                            }
                        });
                    }
                }
            } catch (final IOException e) {
                connected = false;
                if(deviceCallback != null){
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onDeviceDisconnected(device, e.getMessage());
                        }
                    });
                }
            }
        }

        public BluetoothSocket getSocket() {
            return socket;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public OutputStream getOutputStream() {
            return out;
        }
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action!=null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            if (discoveryCallback != null) {
                                ThreadHelper.run(runOnUi, activity, new Runnable() {
                                    @Override
                                    public void run() {
                                        discoveryCallback.onError(DiscoveryError.BLUETOOTH_DISABLED);
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (discoveryCallback != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDiscoveryStarted();
                                }
                            });
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        context.unregisterReceiver(scanReceiver);
                        if (discoveryCallback != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDiscoveryFinished();
                                }
                            });
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (discoveryCallback != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDeviceFound(device);
                                }
                            });
                        }
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(pairReceiver);
                    if(discoveryCallback!=null){
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallback.onDevicePaired(device);
                            }
                        });
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    context.unregisterReceiver(pairReceiver);
                    if(discoveryCallback!=null){
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallback.onDeviceUnpaired(device);
                            }
                        });
                    }
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action!=null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(bluetoothCallback!=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    bluetoothCallback.onBluetoothOff();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    bluetoothCallback.onBluetoothTurningOff();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    bluetoothCallback.onBluetoothOn();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    bluetoothCallback.onBluetoothTurningOn();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    };

    /**
     * Callback to receive device related updates.
     * @param deviceCallback Non-null callback.
     */
    public void setDeviceCallback(DeviceCallback deviceCallback) {
        this.deviceCallback = deviceCallback;
    }

    /**
     * Remove device callback. No updates will be received anymore.
     */
    public void removeDeviceCallback(){
        this.deviceCallback = null;
    }

    /**
     * Callback to receive scanning related updates.
     * @param discoveryCallback Non-null callback.
     */
    public void setDiscoveryCallback(DiscoveryCallback discoveryCallback){
        this.discoveryCallback = discoveryCallback;
    }

    /**
     * Remove discovery callback. No updates will be received anymore.
     */
    public void removeDiscoveryCallback(){
        this.discoveryCallback = null;
    }

    /**
     * Callback to receive bluetooth status related updates.
     * @param bluetoothCallback Non-null callback.
     */
    public void setBluetoothCallback(BluetoothCallback bluetoothCallback){
        this.bluetoothCallback = bluetoothCallback;
    }

    /**
     * Remove bluetooth callback. No updates will be received anymore.
     */
    public void removeBluetoothCallback(){
        this.bluetoothCallback = null;
    }
}