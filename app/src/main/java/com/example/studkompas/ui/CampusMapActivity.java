package com.example.studkompas.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.adapter.LocationsListAdapter;
import com.example.studkompas.model.Campus;
import com.example.studkompas.model.CustomPhotoView;
import com.example.studkompas.utils.GraphEditorController;
import com.example.studkompas.utils.GraphManager;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;
    private Campus selectedCampus;
    private GraphEditorController editorController;

    private RecyclerView locationsList;
    private LocationsListAdapter locationAdapter;
    private boolean isLocationsListDisplays = false;
    private EditText currentFocusedInputField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);

        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");
        editorController = new GraphEditorController(this, photoView, selectedCampus.Id);

        createFloorButtons();
        switchToFloor(1);
        setupLocationsListAdapter();
        setupFocusChangeListenerToInputFields();
    }

    private void setupFocusChangeListenerToInputFields() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                currentFocusedInputField = (EditText) v;
                showLocationsList();
            }
        };

        findViewById(R.id.editTextStart).setOnFocusChangeListener(focusListener);
        findViewById(R.id.editTextEnd).setOnFocusChangeListener(focusListener);
    }

    private void setupLocationsListAdapter() {
        locationsList = findViewById(R.id.LocationsList);
        locationAdapter = new LocationsListAdapter(locationName -> {
            if (currentFocusedInputField != null) {
                currentFocusedInputField.setText(locationName);
            }
            hideLocationsList();
        });

        locationsList.setAdapter(locationAdapter);
        locationsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showLocationsList() {
        if (isLocationsListDisplays)
            return;
        isLocationsListDisplays = true;

        findViewById(R.id.imageViewCampusMap).animate().alpha(0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
        findViewById(R.id.editor_controls).animate().alpha(0f).setDuration(300).start();
        findViewById(R.id.floorPanel).animate().alpha(0f).setDuration(300).start();

        locationsList.setVisibility(View.VISIBLE);

        List<String> locationNames = GraphManager.getNodeNamesInCampus(selectedCampus.Id);
        locationAdapter.updateList(locationNames);
    }

    public void hideLocationsList() {
        if (!isLocationsListDisplays)
            return;
        isLocationsListDisplays = false;

        findViewById(R.id.imageViewCampusMap).animate().alpha(1f).setDuration(300).start();
        findViewById(R.id.editor_controls).animate().alpha(1f).setDuration(300).start();
        findViewById(R.id.floorPanel).animate().alpha(1f).setDuration(300).start();

        locationsList.setVisibility(View.GONE);
        currentFocusedInputField.clearFocus();
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
}


