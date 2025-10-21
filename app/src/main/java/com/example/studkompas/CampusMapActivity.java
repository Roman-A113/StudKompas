package com.example.studkompas;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CampusMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        String campusName = getIntent().getStringExtra("campus_name");
        String campusAddress = getIntent().getStringExtra("campus_address");

        TextView title = findViewById(R.id.textViewMapTitle);
        title.setText(String.format("%s\n%s", campusName, campusAddress));
    }
}