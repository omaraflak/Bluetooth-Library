# Important
I used a BufferedReader to read data from the bluetooth socket. As I'm reading with readLine(), each message you're sending to the Android must end with a \n. Otherwise it won't be received.

The class uses listeners so everything is really easy! Enjoy :)

# Install

Add to your gradle dependencies:

	compile 'me.aflak.libraries:bluetooth:1.2'

## In your Activity

	Bluetooth bluetooth = new Bluetooth();
	bluetooth.enableBluetooth();
	
## Listener to scan and pair

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
	
## Scan and Pair

	bluetooth.scanDevices();
	bluetooth.pair(device);
	
## Listener to communicate

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
	
## Connect to device
	
	// three options
	bluetooth.connectToName("name");
	bluetooth.connectToAddress("address");
	bluetooth.connectToDevice(device);
	
## Send a message

	bluetooth.send("message");
	
## Get paired devices

	List<BluetoothDevice> devices = bluetooth.getPairedDevices();
