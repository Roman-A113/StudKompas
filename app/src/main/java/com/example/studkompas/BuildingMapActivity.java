package com.example.studkompas;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BuildingMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_map);

        String buildingName = getIntent().getStringExtra("building_name");
        String buildingAddress = getIntent().getStringExtra("building_address");

        TextView title = findViewById(R.id.textViewMapTitle);
        title.setText(buildingName + "\n" + buildingAddress);
    }
}