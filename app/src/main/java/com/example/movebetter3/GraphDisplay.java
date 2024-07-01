package com.example.movebetter3;

import android.content.Context;
import android.graphics.Color;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphDisplay {
    private Context context;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private double lastXValue = 0;

    public GraphDisplay(Context context, GraphView graphView) {
        this.context = context;
        this.graphView = graphView;
        series = new LineGraphSeries<>();
        series.setColor(Color.BLUE);
        graphView.addSeries(series);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(10); // Change the value based on your requirement
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100); // Change the value based on your requirement
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);
    }

    public void addDataPoint(double y) {
        lastXValue += 1;
        series.appendData(new DataPoint(lastXValue, y), true, 100);
    }

    public void clearData() {
        series.resetData(new DataPoint[]{});
    }
}
