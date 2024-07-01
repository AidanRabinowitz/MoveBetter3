package com.example.movebetter3;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> entries;
    private LineDataSet dataSet;
    private LineData lineData;
    private Handler handler;
    private Runnable runnable;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph); // Ensure this is the correct layout file

        lineChart = findViewById(R.id.lineChart); // This should resolve correctly if the ID is in the XML file
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "X Axis");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        lineChart.getAxisRight().setEnabled(false);

        handler = new Handler();
        startTime = System.currentTimeMillis();

        runnable = new Runnable() {
            @Override
            public void run() {
                updateGraph();
                handler.postDelayed(this, 50);
            }
        };
        handler.post(runnable);
    }

    private void updateGraph() {
        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) / 1000f;

        if (MainActivity.inputStream != null) {
            try {
                int available = MainActivity.inputStream.available();
                if (available > 0) {
                    byte[] buffer = new byte[available];
                    MainActivity.inputStream.read(buffer);
                    String data = new String(buffer).trim();
                    String[] lines = data.split("\n");

                    for (String line : lines) {
                        if (line.startsWith("X:")) {
                            float xValue = Float.parseFloat(line.substring(2).trim());
                            entries.add(new Entry(elapsedTime, xValue));
                        }
                    }
                    if (entries.size() > 0) {
                        while (entries.size() > 0 && entries.get(0).getX() < elapsedTime - 2) {
                            entries.remove(0);
                        }
                        dataSet.notifyDataSetChanged();
                        lineData.notifyDataChanged();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
