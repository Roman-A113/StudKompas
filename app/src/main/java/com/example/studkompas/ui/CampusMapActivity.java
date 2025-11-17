package com.example.studkompas.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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
    private View editorControls;
    private View floorPanel;
    private View inputLayoutStart;
    private View inputLayoutEnd;
    private ConstraintLayout rootLayout;
    private Button makePathButton;

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

        editorControls = findViewById(R.id.editor_controls);
        floorPanel = findViewById(R.id.floorPanel);
        inputLayoutStart = findViewById(R.id.inputLayoutStart);
        inputLayoutEnd = findViewById(R.id.inputLayoutEnd);
        rootLayout = findViewById(R.id.mainConstraintLayout);
        makePathButton = findViewById(R.id.MakePathButton);

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);

        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");
        if (selectedCampus == null) {
            throw new RuntimeException("selectedCampus cannot be null");
        }
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

        hideMainUI();
        hideOppositeInputField();

        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(rootLayout);

        constraints.clear(R.id.routeInputPanel, ConstraintSet.TOP);
        constraints.clear(R.id.routeInputPanel, ConstraintSet.BOTTOM);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraints.clear(R.id.LocationsList, ConstraintSet.TOP);
        constraints.clear(R.id.LocationsList, ConstraintSet.BOTTOM);
        constraints.connect(R.id.LocationsList, ConstraintSet.TOP, R.id.routeInputPanel, ConstraintSet.BOTTOM, 8);
        constraints.connect(R.id.LocationsList, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraints.connect(R.id.LocationsList, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraints.connect(R.id.LocationsList, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        Transition transition = new ChangeBounds().setDuration(150).setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(rootLayout, transition);
        constraints.applyTo(rootLayout);

        locationsList.setVisibility(View.VISIBLE);
        List<String> locationNames = GraphManager.getNodeNamesInCampus(selectedCampus.Id);
        locationAdapter.updateList(locationNames);
    }

    public void hideLocationsList() {
        if (!isLocationsListDisplays) return;
        isLocationsListDisplays = false;

        locationsList.setVisibility(View.GONE);
        hideKeyboard();

        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(rootLayout);

        constraints.clear(R.id.routeInputPanel, ConstraintSet.TOP);
        constraints.clear(R.id.routeInputPanel, ConstraintSet.BOTTOM);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 10);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraints.connect(R.id.routeInputPanel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraints.clear(R.id.LocationsList, ConstraintSet.TOP);
        constraints.clear(R.id.LocationsList, ConstraintSet.BOTTOM);
        constraints.connect(R.id.LocationsList, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraints.connect(R.id.LocationsList, ConstraintSet.BOTTOM, R.id.routeInputPanel, ConstraintSet.TOP, 8);
        constraints.connect(R.id.LocationsList, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraints.connect(R.id.LocationsList, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraints.setVisibility(R.id.LocationsList, ConstraintSet.GONE);

        Transition transition = new ChangeBounds().setDuration(150).setInterpolator(new DecelerateInterpolator());
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                showMainUI();
                showBothInputFields();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                onTransitionEnd(transition);
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(rootLayout, transition);
        constraints.applyTo(rootLayout);

        currentFocusedInputField.clearFocus();
        currentFocusedInputField = null;
    }

    private void hideMainUI() {
        photoView.setVisibility(View.GONE);
        editorControls.setVisibility(View.GONE);
        floorPanel.setVisibility(View.GONE);
        makePathButton.setVisibility(View.GONE);
    }

    private void showMainUI() {
        photoView.setVisibility(View.VISIBLE);
        editorControls.setVisibility(View.VISIBLE);
        floorPanel.setVisibility(View.VISIBLE);
        makePathButton.setVisibility(View.VISIBLE);
    }

    private void hideOppositeInputField() {
        if (currentFocusedInputField == findViewById(R.id.editTextStart)) {
            inputLayoutEnd.setVisibility(View.GONE);
        } else {
            inputLayoutStart.setVisibility(View.GONE);
        }
    }

    private void showBothInputFields() {
        inputLayoutStart.setVisibility(View.VISIBLE);
        inputLayoutEnd.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocusedInputField.getWindowToken(), 0);
        }
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
        bg.setColor(ContextCompat.getColor(this, R.color.purple));
        bg.setCornerRadius(cornerRadiusPx);
        button.setBackground(bg);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        button.setOnClickListener(v -> switchToFloor(floorNumber));
        return button;
    }
}


