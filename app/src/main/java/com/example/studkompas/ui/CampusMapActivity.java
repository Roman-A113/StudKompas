package com.example.studkompas.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studkompas.R;
import com.example.studkompas.model.CustomPhotoView;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);

        Button btnFloor1 = findViewById(R.id.btnFloor1);
        Button btnFloor2 = findViewById(R.id.btnFloor2);
        Button btnFloor3 = findViewById(R.id.btnFloor3);
        Button btnFloor4 = findViewById(R.id.btnFloor4);
        Button btnFloor5 = findViewById(R.id.btnFloor5);

        setupFloorButton(btnFloor1, R.drawable.guk_1);
        setupFloorButton(btnFloor2, R.drawable.guk_2);
        setupFloorButton(btnFloor3, R.drawable.guk_3);
        setupFloorButton(btnFloor4, R.drawable.guk_4);
        setupFloorButton(btnFloor5, R.drawable.guk_5);


        Button btnToggleLine = findViewById(R.id.btnToggleLine);
        btnToggleLine.setOnClickListener(v -> {
           boolean currentlyShowing = photoView.getShowTestLine();
            photoView.setShowTestLine(!currentlyShowing);
            btnToggleLine.setText(currentlyShowing ? "Показать линии" : "Скрыть линии");
        });
    }

    private void setupFloorButton(Button button, int drawableResId) {
        button.setOnClickListener(v -> {
            photoView.setImageResource(drawableResId);
            //photoView.postDelayed(() -> photoView.setScale(2.0f, true), 100);
        });
    }
}