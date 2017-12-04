package me.aflak.libraries;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.aflak.bluetooth.Bluetooth;

public class Communication extends AppCompatActivity implements Bluetooth.CommunicationCallback, View.OnClickListener {

    public static final String EXTRA_MAC = "mac";
    public static final String EXTRA_DEVICE_NAME = "dev";

    private final byte COD_CONNECTED = 0;
    private final byte COD_DISCONNECTED = 1;
    private final byte COD_ERROR = 2;
    private final byte COD_CONNECT_ERROR = 3;

    private Button btnSend;
    private EditText etTextToSend;
    private TextView tvLog;
    private ScrollView scroll;

    private Bluetooth bluetooth;

    private MessageHandler messageHandler;

    private final String TAG = "<Communication>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        btnSend = (Button) findViewById(R.id.btnSend);
        etTextToSend = (EditText) findViewById(R.id.etTextToSend);
        tvLog = (TextView) findViewById(R.id.tvLog);
        scroll = (ScrollView) findViewById(R.id.scroll);

        btnSend.setOnClickListener(this);

        bluetooth = new Bluetooth(this, true);
        messageHandler = new MessageHandler();

        bluetooth.setCommunicationCallback(this);

        bluetooth.enableBluetooth();

        bluetoothConnect();
    }

    private void bluetoothConnect(){
        String mac = getIntent().getExtras().getString(EXTRA_MAC);
        String name = getIntent().getExtras().getString(EXTRA_DEVICE_NAME);

        setTitle(this.getTitle() + " - " + name);

        Log.i(TAG, "Connecting to " + mac);

        bluetooth.connectToAddress(mac);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        bluetooth.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Message msg = new Message();
        msg.what = COD_CONNECTED;
        messageHandler.dispatchMessage(msg);
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Message msg = new Message();
        msg.what = COD_DISCONNECTED;
        messageHandler.dispatchMessage(msg);
    }

    @Override
    public void onMessage(String message) {
        if(tvLog.getText().toString().length() > 2048){
            tvLog.setText("");
        }

        tvLog.setText(tvLog.getText().toString() + "\nReceived string: \"" + message + "\"");

        scroll.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void onError(String message) {
        Message msg = new Message();
        msg.what = COD_ERROR;
        msg.obj = message;
        messageHandler.dispatchMessage(msg);
    }

    @Override
    public void onConnectError(BluetoothDevice device, String message) {
        Message msg = new Message();
        msg.what = COD_CONNECT_ERROR;
        msg.obj = message;
        messageHandler.dispatchMessage(msg);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btnSend.getId()){
            String sent = etTextToSend.getText().toString();

            bluetooth.send(sent);

            tvLog.setText(tvLog.getText().toString() + "\nSent: \"" + sent + "\"");
        }
    }

    private class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            final int msgWhat = msg.what;
            final Object msgObj = msg.obj;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (msgWhat){
                        case COD_CONNECTED:
                            Toast.makeText(Communication.this, "Connected", Toast.LENGTH_SHORT).show();
                            break;

                        case COD_CONNECT_ERROR:
                            Toast.makeText(Communication.this, "Could not connect to device: " + msgObj.toString(), Toast.LENGTH_LONG).show();
                            break;

                        case COD_DISCONNECTED:
                            Toast.makeText(Communication.this, "Disconnected", Toast.LENGTH_SHORT).show();
                            break;

                        case COD_ERROR:
                            Toast.makeText(Communication.this, "Error: " + msgObj.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });

        }

    }
}
