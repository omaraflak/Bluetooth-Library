package me.aflak.libraries;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

/**
 * Created by Omar on 20/12/2017.
 */

public class ChatActivity extends AppCompatActivity {
    @BindView(R.id.activity_chat_status) TextView state;
    @BindView(R.id.activity_chat_messages) TextView messages;
    @BindView(R.id.activity_chat_hello_world) Button helloWorld;

    private Bluetooth bluetooth;
    private BluetoothDevice device;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        device = getIntent().getParcelableExtra("device");
        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setDeviceCallback(deviceCallback);
        helloWorld.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        bluetooth.connectToDevice(device);
        state.setText("Connecting...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.disconnect();
        bluetooth.onStop();
    }

    private void appendToChat(String msg){
        String text = messages.getText().toString()+"\n"+msg;
        messages.setText(text);
    }

    @OnClick(R.id.activity_chat_hello_world)
    public void onHelloWorld(){
        String msg = "Hello World !";
        bluetooth.send(msg);
        appendToChat("-> "+msg);
    }

    private DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            state.setText("Connected !");
            helloWorld.setEnabled(true);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            state.setText("Device disconnected !");
            helloWorld.setEnabled(false);
        }

        @Override
        public void onMessage(byte[] message) {
            String str = new String(message);
            appendToChat("<- "+str);
        }

        @Override
        public void onError(int errorCode) {

        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            state.setText("Could not connect, next try in 3 sec...");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetooth.connectToDevice(device);
                }
            }, 3000);
        }
    };
}
