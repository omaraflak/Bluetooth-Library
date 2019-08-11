# Android Bluetooth Client Library [ ![Download](https://api.bintray.com/packages/omaflak/maven/bluetooth/images/download.svg) ](https://bintray.com/omaflak/maven/bluetooth/_latestVersion)

<img src="Logotype primary.png" width="60%" height="60%" />

This is an Android library that simplifies the process of bluetooth communication, **client side**.

# Important

I used a BufferedReader to read data from the bluetooth socket. As I'm reading with readLine(), each message you're sending to the Android must end with a **\n**. Otherwise it won't be received.

***This library does not support BLE devices***

# Install

Add to your gradle dependencies:

```
implementation 'me.aflak.libraries:bluetooth:1.3.7'
```

## Basic Settings

### Asking user for bluetooth activation

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    // ...
    // Need to ask for bluetooth permissions before calling constructor !
    // Permissions are {BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION}
    bluetooth = new Bluetooth(this);
    bluetooth.setBluetoothCallback(bluetoothCallback);
}

@Override
protected void onStart() {
    super.onStart();
    bluetooth.onStart();
    if(bluetooth.isEnabled()){
        // doStuffWhenBluetoothOn() ...
    } else {
        bluetooth.showEnableDialog(ScanActivity.this);
    }
}

@Override
protected void onStop() {
    super.onStop();
    bluetooth.onStop();
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    bluetooth.onActivityResult(requestCode, resultCode);
}

private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
    @Override public void onBluetoothTurningOn() {}
    @Override public void onBluetoothTurningOff() {}
    @Override public void onBluetoothOff() {}

    @Override
    public void onBluetoothOn() {
        // doStuffWhenBluetoothOn() ...
    }

    @Override
    public void onUserDeniedActivation() {
        // handle activation denial...
    }
};
```

### Without asking user for bluetooth activation

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    // ...
    // Need to ask for bluetooth permissions before calling constructor !
    // Permissions are {BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION}
    bluetooth = new Bluetooth(this);
    bluetooth.setBluetoothCallback(bluetoothCallback);
}

@Override
protected void onStart() {
    super.onStart();
    bluetooth.onStart();
    if(bluetooth.isEnabled()){
        // doStuffWhenBluetoothOn() ...
    } else {
        bluetooth.enable()
    }
}

@Override
protected void onStop() {
    super.onStop();
    bluetooth.onStop();
}

private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
    @Override public void onBluetoothTurningOn() {}
    @Override public void onBluetoothTurningOff() {}
    @Override public void onBluetoothOff() {}
    @Override public void onUserDeniedActivation() {}
    
    @Override
    public void onBluetoothOn() {
        // doStuffWhenBluetoothOn() ...
    }
};
```
	
## Listen on bluetooth state changes

```java
bluetooth.setBluetoothCallback(new BluetoothCallback() {
    @Override public void onBluetoothTurningOn() {}
    @Override public void onBluetoothOn() {}
    @Override public void onBluetoothTurningOff() {}
    @Override public void onBluetoothOff() {}
    @Override public void onUserDeniedActivation() {}
});
```
	
## Listen on discovery and pairing

```java
bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
    @Override public void onDiscoveryStarted() {}
    @Override public void onDiscoveryFinished() {}
    @Override public void onDeviceFound(BluetoothDevice device) {}
    @Override public void onDevicePaired(BluetoothDevice device) {}
    @Override public void onDeviceUnpaired(BluetoothDevice device) {}
    @Override public void onError(String message) {}
});
```

## Scan and Pair

```java
bluetooth.startScanning();
bluetooth.pair(device);
```
	
## Listen on bluetooth socket

```java
bluetooth.setDeviceCallback(new DeviceCallback() {
    @Override public void onDeviceConnected(BluetoothDevice device) {}
    @Override public void onDeviceDisconnected(BluetoothDevice device, String message) {}
    @Override public void onMessage(byte[] message) {}
    @Override public void onError(String message) {}
    @Override public void onConnectError(BluetoothDevice device, String message) {}
});
```
	
## Connect to device

```java
// three options
bluetooth.connectToName("name");
bluetooth.connectToAddress("address");
bluetooth.connectToDevice(device);
```

## Connect to device using port trick

See this post for details: https://stackoverflow.com/a/25647197/5552022
```java
bluetooth.connectToNameWithPortTrick("name");
bluetooth.connectToAddressWithPortTrick("address");
bluetooth.connectToDeviceWithPortTrick(device);
```

*Should be avoided*
	
## Send a message

```java
bluetooth.send("hello, world");
```
	
## Get paired devices

```java
List<BluetoothDevice> devices = bluetooth.getPairedDevices();
```

# Download Demo App

<p align="center">
<a href='https://play.google.com/store/apps/details?id=me.aflak.libraries&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="50%"/></a>
</p>

# License

```
MIT License

Copyright (c) 2017 Michel Omar Aflak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
