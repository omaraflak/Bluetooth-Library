package me.aflak.libraries;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import me.aflak.bluetooth.Bluetooth;

public class MainActivity extends AppCompatActivity implements Bluetooth.DiscoveryCallback, AdapterView.OnItemClickListener {
    private ListView lsDevices;

    private Bluetooth bluetooth;

    private ArrayAdapter<String> adapter;

    private String TAG = "<MainActivity>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lsDevices = (ListView) findViewById(R.id.lvDevicesList);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        lsDevices.setAdapter(adapter);
        lsDevices.setOnItemClickListener(this);

        bluetooth = new Bluetooth(this);

        bluetooth.setDiscoveryCallback(this);

        bluetooth.enableBluetooth();

        bluetooth.scanDevices();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            adapter.clear();
            adapter.notifyDataSetChanged();

            bluetooth.scanDevices();
            Log.i(TAG, "Scanning devices...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //conectar e abrir monitor
        String texto = ((TextView) view).getText().toString();
        String mac = texto.subSequence(texto.indexOf('\n') + 1, texto.length()).toString();
        String nome = texto.subSequence(0, texto.indexOf('\n')).toString();

        Intent monitor = new Intent(this, Communication.class);
        monitor.putExtra(Communication.EXTRA_MAC, mac);
        monitor.putExtra(Communication.EXTRA_DEVICE_NAME, nome);
        startActivity(monitor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetooth.cancelScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetooth.scanDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.cancelScan();
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onDevice(BluetoothDevice device) {
        adapter.add(device.getName() + "\n" + device.getAddress());
        adapter.notifyDataSetChanged();
        Log.i(TAG, "Device found");
    }

    @Override
    public void onPair(BluetoothDevice device) {

    }

    @Override
    public void onUnpair(BluetoothDevice device) {

    }

    @Override
    public void onError(String message) {
        Log.i(TAG, "ERRO: " + message);
    }
}
