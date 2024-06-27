package com.example.movebetter3;

import android.content.Context;
import android.graphics.Color;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GraphDisplay {
    private Context context;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;

    public GraphDisplay(Context context, GraphView graphView) {
        this.context = context;
        this.graphView = graphView;
        series = new LineGraphSeries<>();
        series.setColor(Color.BLUE);
        graphView.addSeries(series);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(10); // Change the value based on your requirement
    }

    public void addDataPoint(double x, double y) {
        series.appendData(new DataPoint(x, y), true, 100);
    }

    public void clearData() {
        series.resetData(new DataPoint[]{});
    }

    // Method to update the data dynamically
    public void updateData(double newX, double newY) {
        List<DataPoint> currentDataPoints = new ArrayList<>((Collection) series.getValues(Double.NaN, Double.NaN)); // Get the current data points
        currentDataPoints.add(new DataPoint(newX, newY)); // Add the new data point
        DataPoint[] newDataPoints = currentDataPoints.toArray(new DataPoint[0]); // Convert list to array

        // Reset the series with the updated data
        series.resetData(newDataPoints);

        // Scroll to the end of the graph to show the latest data point
        graphView.getViewport().scrollToEnd();
    }
}
