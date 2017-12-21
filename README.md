# Android Bluetooth Library [ ![Download](https://api.bintray.com/packages/omaflak/maven/bluetooth/images/download.svg) ](https://bintray.com/omaflak/maven/bluetooth/_latestVersion)

This is an Android library that simplifies the process of bluetooth communication.

# Important

I used a BufferedReader to read data from the bluetooth socket. As I'm reading with readLine(), each message you're sending to the Android must end with a **\n**. Otherwise it won't be received.

The class uses listeners so everything should be really easy! Enjoy :)

# Install

Add to your gradle dependencies:

```gradle
compile 'me.aflak.libraries:bluetooth:1.3.0'
```

## In your Activity

```java
Bluetooth bluetooth = new Bluetooth(this);
if(!bluetooth.isEnabled())
    bluetooth.enableBluetooth();
//
// ...
//
@Override
protected void onStart() {
    super.onStart();
    bluetooth.onStart();
}

@Override
protected void onStop() {
    super.onStop();
    bluetooth.onStop();
}
```
	
## Listen on bluetooth state changes

```java
bluetooth.setBluetoothCallback(new BluetoothCallback() {
    @Override
    public void onBluetoothTurningOn() {}

    @Override
    public void onBluetoothOn() {}

    @Override
    public void onBluetoothTurningOff() {}

    @Override
    public void onBluetoothOff() {}

    @Override
    public void onUserDeniedActivation() {
        // when using bluetooth.showEnableDialog()
        // you will also have to call bluetooth.onActivityResult()
    }
});
```
	
## Listen on discovery and pairing

```java
bluetooth.setDiscoveryCallback(new Bluetooth.DiscoveryCallback() {

      @Override
      public void onFinish() {
          // scan finished
      }
      
      @Override
      public void onDevice(BluetoothDevice device) {
          // device found
      }
      
      @Override
      public void onPair(BluetoothDevice device) {
          // device paired
      }
      
      @Override
      public void onUnpair(BluetoothDevice device) {
          // device unpaired
      }
      
      @Override
      public void onError(String message) {
          // error occurred
      }
});
```

## Scan and Pair

```java
bluetooth.startScanning();
bluetooth.pair(device);
```
	
## Listen on bluetooth socket

```java
bluetooth.setCommunicationCallback(new Bluetooth.CommunicationCallback() {
      @Override
      public void onConnect(BluetoothDevice device) {
          // device connected
      }
      
      @Override
      public void onDisconnect(BluetoothDevice device, String message) {
          // device disconnected
      }
      
      @Override
      public void onMessage(String message) {
          // message received (it has to end with a \n to be received)
      }
      
      @Override
      public void onError(String message) {
          // error occurred 
      }
      
      @Override
      public void onConnectError(BluetoothDevice device, String message) {
          // error during connection
      }
});
```
	
## Connect to device

```java
// three options
bluetooth.connectToName("name");
bluetooth.connectToAddress("address");
bluetooth.connectToDevice(device);
```
	
## Send a message

```java
bluetooth.send("message");
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