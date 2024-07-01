package com.example.movebetter3;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> entries;
    private LineDataSet dataSet;
    private LineData lineData;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private Handler handler;
    private int xIndex = 0; // x-axis index for the entries

    private static final int DATA_POINT_COUNT = 200; // Number of data points for 20 seconds if updating every 100ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        lineChart = findViewById(R.id.lineChart);
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "X-Axis Acceleration");
        lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        inputStream = MainActivity.inputStream;
        handler = new Handler();

        if (inputStream != null) {
            beginListenForData();
        } else {
            Toast.makeText(this, "No Bluetooth connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWorker = true;
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
                                    handler.post(new Runnable() {
                                        public void run() {
                                            processIncomingData(data);
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

    private void processIncomingData(String data) {
        try {
            float xValue = parseXValue(data); // Parse the X-axis acceleration value from the data
            addEntry(xValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void addEntry(float xValue) {
        entries.add(new Entry(xIndex++, xValue));

        if (entries.size() > DATA_POINT_COUNT) {
            entries.remove(0);
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).setX(i);
            }
            xIndex = entries.size();
        }

        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private float parseXValue(String data) {
        // Parse the data string to extract the X-axis acceleration value.
        // Assuming the data string is formatted correctly, e.g., "X:12.34"
        String[] parts = data.split(":");
        if (parts.length == 2 && parts[0].trim().equals("X")) {
            return Float.parseFloat(parts[1].trim());
        } else {
            throw new NumberFormatException("Invalid data format");
        }
    }
}
