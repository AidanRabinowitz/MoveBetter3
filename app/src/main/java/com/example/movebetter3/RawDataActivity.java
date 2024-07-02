package com.example.movebetter3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class RawDataActivity extends AppCompatActivity {

    private TextView rawDataTextView;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);

        rawDataTextView = findViewById(R.id.rawDataTextView);

        try {
            inputStream = BluetoothService.getInstance(this).getInputStream();
            if (inputStream != null) {
                beginListenForData();
            } else {
                rawDataTextView.setText("No data available");
            }
        } catch (Exception e) {
            rawDataTextView.setText("Failed to get input stream");
            e.printStackTrace();
        }
    }

    private void beginListenForData() {
        final byte delimiter = 10; // ASCII code for newline character
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            rawDataTextView.append(data + "\n");
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException e) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWorker = true;
    }
}
