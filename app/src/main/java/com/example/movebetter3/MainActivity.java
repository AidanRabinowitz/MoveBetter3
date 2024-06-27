package com.example.movebetter3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jjoe64.graphview.GraphView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<String> receivedDataList = new ArrayList<>();
    double previousX = 0;


    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 3;
    private TextView receivedDataTextView;

    private static final String TAG = "MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> deviceListAdapter;
    private boolean isConnected = false;
    private String connectedDeviceName = "";
    private GraphDisplay graphDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the button and set its onClickListener
        Button goToGoodLiftButton = findViewById(R.id.btn_go_to_good_lift);
        goToGoodLiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the GoodLift activity
                Intent intent = new Intent(MainActivity.this, GoodLift.class);
                startActivity(intent);
            }
        });

        receivedDataTextView = findViewById(R.id.receivedDataTextView);
        GraphView graphView = findViewById(R.id.graphView);
        graphDisplay = new GraphDisplay(this, graphView);

        ListView listViewDevices = findViewById(R.id.listViewDevices);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listViewDevices.setAdapter(deviceListAdapter);
        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (!isConnected) {
                String deviceName = deviceList.get(position);
                connectToDevice(deviceName);
            } else {
                Toast.makeText(MainActivity.this, "Already connected to a device", Toast.LENGTH_SHORT).show();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            // Request Bluetooth permissions if not granted
            requestBluetoothPermissions();
        }
    }

    private void connectToDevice(String deviceName) {
        BluetoothDevice device = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName() != null && pairedDevice.getName().equals(deviceName)) {
                device = pairedDevice;
                break;
            }
        }
        if (device != null) {
            ConnectThread connectThread = new ConnectThread(device);
            connectThread.start();
        } else {
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopDeviceDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    private void requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_SCAN_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discoverDevices(null);
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void discoverDevices(View view) {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            deviceList.clear();
            deviceListAdapter.notifyDataSetChanged();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.startDiscovery();
        } else {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    String deviceName = device.getName();
                    if (deviceName != null) {
                        deviceList.add(deviceName);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket = null;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request Bluetooth permission
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
                    // Returning here indicates that permission is not granted, and the request has been made.
                    return;
                }
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                isConnected = true;
                connectedDeviceName = mmDevice.getName();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connected to: " + connectedDeviceName, Toast.LENGTH_SHORT).show();
//                    updateUI();
                });

                // Stop device discovery after successful connection
                stopDeviceDiscovery();

                startReadingData(mmSocket);
            } catch (IOException connectException) {
                // Connection attempt failed
                handleConnectionFailure();
            }
        }
        private void handleConnectionFailure() {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private void startReadingData(BluetoothSocket mmSocket) {
        new Thread(() -> {
            try {
                InputStream inputStream = mmSocket.getInputStream();

                byte[] buffer = new byte[1024];
                int bytes;

                while (isConnected) {
                    bytes = inputStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);
                    runOnUiThread(() -> processReceivedData(receivedData));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processReceivedData(String data) {
        try {
            // Trim any leading/trailing whitespace
            data = data.trim();

            // Pass the received data to analyzeLift method
            analyzeLift(Double.parseDouble(data));

            // Update received data TextView if it's not null
            if (receivedDataTextView != null) {
                String finalData = data;
                runOnUiThread(() -> receivedDataTextView.setText("Received Data: " + finalData));
            } else {
                Log.e(TAG, "ReceivedDataTextView is null");
            }

            // Start the GoodLift activity and pass the received data
            Intent intent = new Intent(MainActivity.this, GoodLift.class);
            intent.putExtra("received_data", data);
            startActivity(intent);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing received data", e);
        }
    }


    private void analyzeLift(double xValue) {
        // Call the method from LiftLogic class to analyze the lift
        double topArcAcceleration = LiftLogic.calculateTopArcAcceleration(new double[]{xValue});

        // Check if the top arc acceleration is above 50
        if (topArcAcceleration > 50) {
            // Show "Good Lift" message for 2 seconds
            showToastMessage("Good Lift", 2000);
        }
    }

    private void showToastMessage(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    // Modify the displayReceivedData method to format the received data and update the TextView
// Modify the displayReceivedData method to format the received data and start the GoodLift activity
    private void displayReceivedData(String data) {
        try {
            // Parse the received data into an integer x value
            int x = Integer.parseInt(data.trim()); // Trim any leading/trailing whitespace

            // Add the data point to the graph
            // Assuming you have a method to handle adding data points to the graph
            graphDisplay.addDataPoint(x, 0); // Since y value is not provided, assuming it's 0

            // Start the GoodLift activity and pass Bluetooth data
            Intent intent = new Intent(MainActivity.this, GoodLift.class);
            intent.putExtra("bluetooth_data", data); // Pass Bluetooth data
            startActivity(intent);

            // Update the previousX variable
            previousX = x;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing received data", e);
        }
    }
    public static class LiftLogic {
        private static final double ARC_THRESHOLD = 0.2; // Threshold for detecting an arc
        private static final int TOP_PERCENT = 20; // Percentage of top acceleration values to consider

        // Method to calculate the average acceleration of the top 20 percent of an arc
        public static double calculateTopArcAcceleration(double[] accelerometerData) {
            int dataSize = accelerometerData.length;
            int startIndex = -1;
            int endIndex = -1;

            // Find the start and end indices of the arc
            for (int i = 1; i < dataSize - 1; i++) {
                double prevValue = accelerometerData[i - 1];
                double currentValue = accelerometerData[i];
                double nextValue = accelerometerData[i + 1];

                // Check for an arc pattern (increase then decrease in acceleration within ARC_THRESHOLD seconds)
                if (currentValue > prevValue && currentValue > nextValue &&
                        nextValue < currentValue * (1 - ARC_THRESHOLD)) {
                    startIndex = i;
                    break;
                }
            }

            if (startIndex != -1) {
                for (int i = startIndex + 1; i < dataSize - 1; i++) {
                    double prevValue = accelerometerData[i - 1];
                    double currentValue = accelerometerData[i];
                    double nextValue = accelerometerData[i + 1];

                    // Check for the end of the arc pattern
                    if (currentValue < prevValue && currentValue < nextValue &&
                            prevValue > currentValue * (1 - ARC_THRESHOLD)) {
                        endIndex = i;
                        break;
                    }
                }
            }

            // Calculate the top 20 percent of the arc
            if (startIndex != -1 && endIndex != -1) {
                int arcSize = endIndex - startIndex + 1;
                int topPercentSize = (int) Math.ceil((double) arcSize * TOP_PERCENT / 100);

                double[] topValues = new double[topPercentSize];
                System.arraycopy(accelerometerData, startIndex, topValues, 0, topPercentSize);

                // Calculate the average of the top values
                double sum = 0;
                for (double value : topValues) {
                    sum += value;
                }
                return sum / topPercentSize;
            } else {
                return 0; // No arc found
            }
        }
    }

}

