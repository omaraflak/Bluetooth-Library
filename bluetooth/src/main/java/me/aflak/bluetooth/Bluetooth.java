package me.aflak.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Omar on 14/07/2015.
 */
public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device, devicePair;
    private BufferedReader input;
    private OutputStream out;

    private final String TAG = "<Bluetooth>";

    private boolean connected=false;
    private CommunicationCallback communicationCallback=null;
    private DiscoveryCallback discoveryCallback=null;

    private Activity activity;

    private boolean crossThread;
    private InputDataType dataType;

    public Bluetooth(Activity activity){
        this.activity=activity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        crossThread = false;
    }

    public Bluetooth(Activity activity, boolean isCrossThread){
        this.activity=activity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        crossThread = isCrossThread;
    }

    public void enableBluetooth(){
        if(bluetoothAdapter!=null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    public void disableBluetooth(){
        if(bluetoothAdapter!=null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void setCrossThread(boolean value){
        crossThread = value;
    }

    public void setDataType(InputDataType type){
        dataType = type;
    }

    public void connectToAddress(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        new ConnectThread(device).start();
    }

    public void connectToName(String name) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToAddress(blueDevice.getAddress());
                return;
            }
        }
    }

    public void connectToDevice(BluetoothDevice device){
        new ConnectThread(device).start();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            if(communicationCallback!=null)
                dispatchOnCommunicationError(e.getMessage());
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public void send(String msg){
        try {
            out.write(msg.getBytes());
        } catch (IOException e) {
            connected=false;
            if(communicationCallback!=null)
                dispatchOnDisconnect(device, e.getMessage());
        }
    }

    public void cancelScan(){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            activity.unregisterReceiver(mReceiverScan);
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            try {
                switch(dataType){
                    case TEXT:
                        String msg;
                        while ((msg = input.readLine()) != null) {
                            if (communicationCallback != null)
                                dispatchOnMessage(msg);
                        }
                        break;

                    case NUMBER:
                        long value;
                        while ((value = input.read()) != -1) {
                            if (communicationCallback != null)
                                dispatchOnMessage(value);
                        }
                }



            } catch (IOException e) {
                connected=false;
                if (communicationCallback != null)
                    dispatchOnDisconnect(device, e.getMessage());
            }

        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            Bluetooth.this.device=device;
            try {
                Bluetooth.this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(communicationCallback!=null)
                    dispatchOnCommunicationError(e.getMessage());
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                out = socket.getOutputStream();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected=true;

                new ReceiveThread().start();

                if(communicationCallback!=null)
                   dispatchOnConnect(device);
            } catch (IOException e) {
                if(communicationCallback!=null)
                    dispatchOnConnectError(device, e.getMessage());

                try {
                    socket.close();
                } catch (IOException closeException) {
                    if (communicationCallback != null)
                        dispatchOnCommunicationError(closeException.getMessage());
                }
            }
        }
    }

    public List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            devices.add(blueDevice);
        }
        return devices;
    }

    public BluetoothSocket getSocket(){
        return socket;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public void scanDevices(){
        IntentFilter filter = new IntentFilter();

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        activity.registerReceiver(mReceiverScan, filter);
        bluetoothAdapter.startDiscovery();
    }

    public void pair(BluetoothDevice device){
        activity.registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        devicePair=device;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            if(discoveryCallback!=null)
                dispatchOnDiscoveryError(e.getMessage());
        }
    }

    public void unpair(BluetoothDevice device) {
        devicePair=device;
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            if(discoveryCallback!=null)
                dispatchOnDiscoveryError(e.getMessage());
        }
    }

    private BroadcastReceiver mReceiverScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        if (discoveryCallback != null)
                            dispatchOnDiscoveryError("Bluetooth turned off");
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    context.unregisterReceiver(mReceiverScan);
                    if (discoveryCallback != null)
                        dispatchOnFinish();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (discoveryCallback != null)
                        dispatchOnDevice(device);
                    break;
            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(mPairReceiver);
                    if(discoveryCallback!=null)
                        dispatchOnPair(devicePair);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    context.unregisterReceiver(mPairReceiver);
                    if(discoveryCallback!=null)
                        dispatchOnUnpair(devicePair);
                }
            }
        }
    };

    public interface CommunicationCallback{
        void onConnect(BluetoothDevice device);
        void onDisconnect(BluetoothDevice device, String message);
        void onMessage(String message);
        void onMessage(long value);
        void onError(String message);
        void onConnectError(BluetoothDevice device, String message);
    }

    private void dispatchOnConnect(final BluetoothDevice device){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onConnect(device);
                }
            });
        }
        else{
            communicationCallback.onConnect(device);
        }
    }

    private void dispatchOnDisconnect(final BluetoothDevice device, final String message){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onDisconnect(device, message);
                }
            });
        }
        else{
            communicationCallback.onDisconnect(device, message);
        }
    }

    private void dispatchOnMessage(final String message){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onMessage(message);
                }
            });
        }
        else{
            communicationCallback.onMessage(message);
        }
    }

    private void dispatchOnMessage(final long value){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onMessage(value);
                }
            });
        }
        else{
            communicationCallback.onMessage(value);
        }
    }

    private void dispatchOnConnectError(final BluetoothDevice device, final String message){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onConnectError(device, message);
                }
            });
        }
        else{
            communicationCallback.onConnectError(device, message);
        }
    }

    private void dispatchOnCommunicationError(final String message){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    communicationCallback.onError(message);
                }
            });
        }
        else{
            communicationCallback.onError(message);
        }
    }

    public void setCommunicationCallback(CommunicationCallback communicationCallback) {
        this.communicationCallback = communicationCallback;
    }

    public void removeCommunicationCallback(){
        this.communicationCallback = null;
    }

    public interface DiscoveryCallback{
        void onFinish();
        void onDevice(BluetoothDevice device);
        void onPair(BluetoothDevice device);
        void onUnpair(BluetoothDevice device);
        void onError(String message);
    }

    private void dispatchOnFinish(){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    discoveryCallback.onFinish();
                }
            });
        }
        else{
            discoveryCallback.onFinish();
        }
    }

    private void dispatchOnDevice(final BluetoothDevice device){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    discoveryCallback.onDevice(device);
                }
            });
        }
        else{
            discoveryCallback.onDevice(device);
        }
    }

    private void dispatchOnPair(final BluetoothDevice device){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    discoveryCallback.onPair(device);
                }
            });
        }
        else{
            discoveryCallback.onPair(device);
        }
    }

    private void dispatchOnUnpair(final BluetoothDevice device){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    discoveryCallback.onUnpair(device);
                }
            });
        }
        else{
            discoveryCallback.onUnpair(device);
        }
    }

    private void dispatchOnDiscoveryError(final String message){
        if(crossThread) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    discoveryCallback.onError(message);
                }
            });
        }
        else{
            discoveryCallback.onError(message);
        }
    }

    public void setDiscoveryCallback(DiscoveryCallback discoveryCallback){
        this.discoveryCallback=discoveryCallback;
    }

    public void removeDiscoveryCallback(){
        this.discoveryCallback=null;
    }

    public enum InputDataType { TEXT, NUMBER }

}


