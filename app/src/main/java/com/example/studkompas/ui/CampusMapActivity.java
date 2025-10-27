package com.example.studkompas.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studkompas.R;
import com.example.studkompas.model.CustomPhotoView;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;
    private Button btnToggleLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        photoView = findViewById(R.id.imageViewCampusMap);
        btnToggleLine = findViewById(R.id.btnToggleLine);

        photoView.setMaximumScale(10.0f);
        photoView.postDelayed(() -> photoView.setScale(2.0f, true), 100);


        btnToggleLine.setOnClickListener(v -> {
            boolean currentlyShowing = photoView.getShowTestLine();
            photoView.setShowTestLine(!currentlyShowing);
            btnToggleLine.setText(currentlyShowing ? "Показать линии" : "Скрыть линии");
        });
    }
}