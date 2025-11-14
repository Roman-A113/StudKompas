package com.example.studkompas.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studkompas.R;
import com.example.studkompas.adapter.LocationAdapter;
import com.example.studkompas.model.Campus;
import com.example.studkompas.model.CustomPhotoView;
import com.example.studkompas.utils.GraphEditorController;
import com.example.studkompas.utils.GraphManager;

import java.util.Set;
import java.util.TreeSet;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;
    private Campus selectedCampus;
    private GraphEditorController editorController;


    private RecyclerView recyclerViewLocations;
    private LocationAdapter locationAdapter;
    private View mapView;
    private boolean isRouteMode = false;
    private EditText currentFocusedEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        recyclerViewLocations = findViewById(R.id.recyclerViewLocations);
        setupLocationList();

        EditText editTextStart = findViewById(R.id.editTextStart);
        EditText editTextEnd = findViewById(R.id.editTextEnd);

        mapView = findViewById(R.id.imageViewCampusMap);

        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                currentFocusedEditText = (EditText) v;
                enterRouteInputMode();
            }
        };

        editTextStart.setOnFocusChangeListener(focusListener);
        editTextEnd.setOnFocusChangeListener(focusListener);

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);

        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");
        editorController = new GraphEditorController(this, photoView, selectedCampus.Id);

        createFloorButtons();
        switchToFloor(1);
    }



    private void switchToFloor(int floorNumber) {
        String currentFloorStr = String.valueOf(floorNumber);
        Integer drawableRes = selectedCampus.FloorNumberToDrawable.get(floorNumber);
        if (drawableRes == null) {
            throw new RuntimeException("drawableRes is null");
        }
        photoView.setImageResource(drawableRes);
        photoView.loadGraphForCampus(selectedCampus.Id, currentFloorStr);
        photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);

        editorController.setCurrentFloor(currentFloorStr);
    }

    private void createFloorButtons() {
        LinearLayout floorButtonsContainer = findViewById(R.id.floorPanel);
        floorButtonsContainer.removeAllViews();
        Set<Integer> sortedFloors = new TreeSet<>(selectedCampus.FloorNumberToDrawable.keySet());

        for (Integer floorNumber : sortedFloors) {
            Button floorButton = createFloorButton(floorNumber);
            floorButtonsContainer.addView(floorButton);
        }
    }

    private Button createFloorButton(int floorNumber) {
        Button button = new Button(this);
        button.setText(String.valueOf(floorNumber));

        Resources resources = getResources();
        float density = resources.getDisplayMetrics().density;

        int sizeDp = 48;
        int heightDp = 52;
        int marginDp = 5;
        int paddingDp = 0;
        float cornerRadiusDp = 8f;

        int widthPx = (int) (sizeDp * density + 0.5f);
        int heightPx = (int) (heightDp * density + 0.5f);
        int marginPx = (int) (marginDp * density + 0.5f);
        int paddingPx = (int) (paddingDp * density + 0.5f);
        float cornerRadiusPx = cornerRadiusDp * density;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
        params.setMargins(marginPx, marginPx, marginPx, marginPx);
        button.setLayoutParams(params);
        button.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ContextCompat.getColor(this, R.color.green));
        bg.setCornerRadius(cornerRadiusPx);
        button.setBackground(bg);
        button.setTextColor(Color.BLACK);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        button.setOnClickListener(v -> switchToFloor(floorNumber));
        return button;
    }

    private void setupLocationList() {
        locationAdapter = new LocationAdapter(locationName -> {
            if (currentFocusedEditText != null) {
                currentFocusedEditText.setText(locationName);
            }
            exitRouteInputMode();
        });

        recyclerViewLocations.setAdapter(locationAdapter);
        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(this));
    }
    private void enterRouteInputMode() {
        if (isRouteMode)
            return;
        isRouteMode = true;

        mapView.animate().alpha(0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
        findViewById(R.id.editor_controls).animate().alpha(0f).setDuration(300).start();
        findViewById(R.id.floorPanel).animate().alpha(0f).setDuration(300).start();

        recyclerViewLocations.setVisibility(View.VISIBLE);
        loadLocationsIntoList();
    }

    public void exitRouteInputMode() {
        if (!isRouteMode)
            return;
        isRouteMode = false;

        mapView.animate().alpha(1f).setDuration(200).start();
        findViewById(R.id.editor_controls).animate().alpha(1f).setDuration(200).start();
        findViewById(R.id.floorPanel).animate().alpha(1f).setDuration(200).start();

        recyclerViewLocations.setVisibility(View.GONE);
        currentFocusedEditText.clearFocus();
    }

    private void loadLocationsIntoList() {
        List<String> locationNames = GraphManager.getUniqueNonEmptyNodeNames(selectedCampus.Id);
        locationAdapter.updateList(locationNames);
    }
}


