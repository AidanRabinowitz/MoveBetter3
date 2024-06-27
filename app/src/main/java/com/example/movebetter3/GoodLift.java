package com.example.movebetter3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GoodLift extends AppCompatActivity {
    private TextView receivedDataTextView;
    private TextView statusTextView;
    private String receivedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_lift);

        // Find the TextViews in the layout
        receivedDataTextView = findViewById(R.id.receivedDataTextView);
        statusTextView = findViewById(R.id.statusTextView);

        // Retrieve the received data from the intent
        receivedData = getIntent().getStringExtra("received_data");

        // Display the received data in the receivedDataTextView
        receivedDataTextView.setText("Received Data: " + receivedData);

        // Update the status TextView based on received data
        updateStatusTextView();
    }

    private void updateStatusTextView() {
        if (receivedData != null) {
            // Parse the received data and determine if it's a good lift or busy lifting
            int receivedValue = Integer.parseInt(receivedData);
            if (receivedValue > 50) {
                // Display "Good Lift" if the received value indicates a good lift
                statusTextView.setText("Good Lift");
            } else {
                // Display "Busy Lifting" if the received value indicates the user is lifting
                statusTextView.setText("Busy Lifting");
            }
        }
    }
}
