package com.example.movebetter3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {

    private static BluetoothService instance;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Context context;

    private BluetoothService(Context context) {
        this.context = context.getApplicationContext(); // Use application context to avoid leaks
    }

    public static synchronized BluetoothService getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothService(context);
        }
        return instance;
    }

    public void connect(String deviceName) throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions if necessary
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();
                Toast.makeText(context, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (bluetoothSocket != null) {
            bluetoothSocket.close();
        }
    }
}
