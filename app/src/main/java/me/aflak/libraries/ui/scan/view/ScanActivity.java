package me.aflak.libraries.ui.scan.view;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.aflak.libraries.MyApp;
import me.aflak.libraries.data.BluetoothModule;
import me.aflak.libraries.ui.chat.view.ChatActivity;
import me.aflak.libraries.R;
import me.aflak.libraries.ui.scan.data.DaggerScanComponent;
import me.aflak.libraries.ui.scan.data.ScanModule;
import me.aflak.libraries.ui.scan.presenter.ScanPresenter;

public class ScanActivity extends AppCompatActivity implements ScanView {
    @BindView(R.id.activity_scan_paired_list) ListView pairedDeviceList;
    @BindView(R.id.activity_scan_list) ListView deviceList;
    @BindView(R.id.activity_scan_state) TextView state;
    @BindView(R.id.activity_scan_progress) ProgressBar progress;
    @BindView(R.id.activity_scan_button) Button scanButton;

    @Inject ScanPresenter presenter;

    private ArrayAdapter<String> scanListAdapter;
    private ArrayAdapter<String> pairedListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        DaggerScanComponent.builder()
            .bluetoothModule(MyApp.app().bluetoothModule())
            .scanModule(new ScanModule(this))
            .build().inject(this);

        ButterKnife.bind(this);

        pairedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        pairedDeviceList.setAdapter(pairedListAdapter);
        pairedDeviceList.setOnItemClickListener(onPairedListItemClick);

        scanListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        deviceList.setAdapter(scanListAdapter);
        deviceList.setOnItemClickListener(onScanListItemClick);
    }

    private AdapterView.OnItemClickListener onPairedListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            presenter.pairedItemClick(i);
        }
    };

    private AdapterView.OnItemClickListener onScanListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            presenter.scanItemClick(i);
        }
    };

    @OnClick(R.id.activity_scan_button)
    public void onScan(){
        presenter.startScanning();
    }

    @Override
    public void showPairedList(List<String> items) {
        pairedListAdapter.addAll(items);
        pairedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void addDeviceToScanList(String item) {
        scanListAdapter.add(item);
        scanListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setScanStatus(String status) {
        state.setText(status);
    }

    @Override
    public void setScanStatus(int resId) {
        state.setText(resId);
    }

    @Override
    public void clearScanList() {
        scanListAdapter.clear();
        scanListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress(boolean enabled) {
        progress.setVisibility(enabled?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void enableScanButton(boolean enabled) {
        scanButton.setEnabled(enabled);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToChat(String extraName, BluetoothDevice extraDevice) {
        Intent intent = new Intent(ScanActivity.this, ChatActivity.class);
        intent.putExtra(extraName, extraDevice);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }
}
