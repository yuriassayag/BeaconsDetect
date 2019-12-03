package com.example.detectbeacons;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    Map<String, ArrayList<Integer>> beaconsRssis = new HashMap<String, ArrayList<Integer>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isPermissionsGranted() && bleSupport() && activateBle()) startScan();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    public void startScan() {
        start();
    }

    public void stopScan() {
        stop();
    }

    public void start() {
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        bluetoothLeScanner.startScan(filters, settings, mLeScanCallback);
    }

    public void stop() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (Objects.requireNonNull(result.getScanRecord()).getDeviceName() != null && Objects.equals(result.getScanRecord().getDeviceName(), "$(#")){

                String mac  = result.getDevice().getAddress();
                int rssi    = result.getRssi();

                if(beaconsRssis.get(mac) == null) beaconsRssis.put(mac, new ArrayList<Integer>());

                beaconsRssis.get(mac).add(rssi);

                Log.i("resultado", "MAC: " + mac + " RSSI: " + rssi);
                //Log.i("resultado", "MAP: " + beaconsRssis );
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i("xxx", results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public boolean isPermissionsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  1);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ( bleSupport()  && activateBle()) {
                        Log.i("resultado", "onRequestPermission OK");
                    }
                }
                else {
                    finish();
                    Toast.makeText(MainActivity.this, "Permissão não finalizada.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean bleSupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public boolean activateBle() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 2);

                return false;
            }
            return true;
        }
        return false;
    }
}
