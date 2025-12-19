package com.example.studkompas.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.adapter.LocationsListAdapter;
import com.example.studkompas.model.Campus;
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.model.PathWithTransition;
import com.example.studkompas.model.ShowUiTransitionListener;
import com.example.studkompas.utils.AnalyticsHelper;
import com.example.studkompas.utils.GraphEditorControllerUI;
import com.example.studkompas.utils.GraphManager;
import com.example.studkompas.utils.WindowInsetsHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CampusMapActivity extends AppCompatActivity {
    private final boolean isDeveloperMode = true;
    private FloorMapView floorMapView;
    private View editorControls;
    private View floorPanel;
    private TextInputLayout inputLayoutStart;
    private TextInputLayout inputLayoutEnd;
    private EditText editTextStart;
    private EditText editTextEnd;
    private AutoCompleteTextView floorAutoComplete;
    private ConstraintLayout rootLayout;
    private Button makePathButton;
    private Button backButton;
    private LinearLayout routeSummaryLayout;
    private TextView startTextView;
    private TextView endTextView;
    private Button finishPathButton;
    private CheckBox flagElevator;
    private GraphEditorControllerUI editorController;
    private RecyclerView locationsList;
    private LocationsListAdapter locationAdapter;
    private boolean isLocationsListDisplays = false;

    private TextInputLayout currentFocusedInputLayout = null;
    private EditText currentFocusedInputField = null;

    private Campus selectedCampus;
    private String selectedFloor;
    private GraphNode selectedStartNode;
    private GraphNode selectedEndNode;

    private PathWithTransition pathWithTransition;

    private GraphManager graphManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);
        WindowInsetsHelper.ApplySystemWindowInsets(this, R.id.mainConstraintLayout);

        rootLayout = findViewById(R.id.mainConstraintLayout);


        editorControls = findViewById(R.id.editor_controls);
        floorPanel = findViewById(R.id.floorDropdownContainer);

        inputLayoutStart = findViewById(R.id.inputLayoutStart);
        inputLayoutEnd = findViewById(R.id.inputLayoutEnd);
        editTextStart = findViewById(R.id.editTextStart);
        editTextEnd = findViewById(R.id.editTextEnd);

        routeSummaryLayout = findViewById(R.id.routeSummaryLayout);
        startTextView = findViewById(R.id.startTextView);
        endTextView = findViewById(R.id.endTextView);
        finishPathButton = findViewById(R.id.finishPathButton);

        floorAutoComplete = findViewById(R.id.floorAutoComplete);
        makePathButton = findViewById(R.id.MakePathButton);

        backButton = findViewById(R.id.backButton);
        flagElevator = findViewById(R.id.flagElevator);

        floorMapView = findViewById(R.id.floor_map_view);
        floorMapView.setMaximumScale(10.0f);
        floorMapView.setOnTransitionMarkClickListener(targetFloor -> {
            switchToFloor(Integer.parseInt(targetFloor));
        });


        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");
        if (selectedCampus == null) {
            throw new RuntimeException("selectedCampus cannot be null");
        }

        graphManager = new GraphManager(this, selectedCampus);
        graphManager.loadCampusGraphFromAssets();
        graphManager.saveCampusGraphToTempFile();

        if (isDeveloperMode) {
            editorControls.setVisibility(View.VISIBLE);
            editorController = new GraphEditorControllerUI(this, floorMapView, graphManager);
        } else {
            floorMapView.setGraphVisible(false);
        }

        setupFloorDropdown();
        setupLocationsListAdapter();
        setupFocusChangeListenerToInputFields();
        setupMakePathButton();
        setupBackButton();
        setupFinishPathButton();
    }

    private void setupFinishPathButton() {
        finishPathButton.setOnClickListener(v -> {

            boolean isCounted = AnalyticsHelper.logRouteButtonClick(this);

            clearPath();
            finishPathButton.setVisibility(View.GONE);
            routeSummaryLayout.setVisibility(View.GONE);
            showBothInputFields();
            flagElevator.setChecked(false);
            flagElevator.setVisibility(View.VISIBLE);
            makePathButton.setVisibility(View.VISIBLE);
            floorMapView.clearPath();
            floorMapView.clearTransitionNodes();
            switchToFloor(Integer.parseInt(selectedFloor));
        });
    }

    private void clearPath() {
        pathWithTransition = null;
        editTextStart.setText("");
        editTextEnd.setText("");
        selectedStartNode = selectedEndNode = null;
    }

    private void setupMakePathButton() {
        makePathButton.setOnClickListener(v -> {
            if (selectedStartNode == null && selectedEndNode == null) {
                Toast.makeText(this, "Выберите стартовую и конечную точку", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartNode == null) {
                Toast.makeText(this, "Выберите стартовую точку", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedEndNode == null) {
                Toast.makeText(this, "Выберите конечную точку", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartNode == selectedEndNode) {
                Toast.makeText(this, "Вы уже здесь!", Toast.LENGTH_SHORT).show();
                return;
            }


            if (selectedEndNode.name.equals("Туалет (М)")
                    || selectedEndNode.name.equals("Туалет (Ж)")
                    || selectedEndNode.name.equals("Автомат с кофе")
                    || selectedEndNode.name.equals("Автомат с едой")) {
                pathWithTransition = graphManager.getPathByTargetName(selectedStartNode, selectedEndNode.name, !flagElevator.isChecked());
            } else {
                pathWithTransition = graphManager.getPathBetweenTwoNodes(selectedStartNode, selectedEndNode, !flagElevator.isChecked());
            }

            switchToFloor(Integer.parseInt(selectedStartNode.floor));
            floorMapView.updatePath(pathWithTransition.segmentedPath.get(selectedFloor));
            floorMapView.setFloor(selectedFloor);
            floorMapView.setTransitionNodes(pathWithTransition.transitionNodes);

            hideBothInputFields();
            flagElevator.setVisibility(View.GONE);
            makePathButton.setVisibility(View.GONE);

            startTextView.setText(selectedStartNode.name);
            endTextView.setText(selectedEndNode.name);
            routeSummaryLayout.setVisibility(View.VISIBLE);
            finishPathButton.setVisibility(View.VISIBLE);

            AnalyticsHelper.logRouteStart(
                    this,
                    selectedCampus,
                    selectedStartNode,
                    selectedEndNode
            );
        });
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            hideLocationsList();
        });
    }

    private void setupFocusChangeListenerToInputFields() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                currentFocusedInputField = (EditText) v;
                if (v.getId() == R.id.editTextStart) {
                    currentFocusedInputLayout = inputLayoutStart;
                } else if (v.getId() == R.id.editTextEnd) {
                    currentFocusedInputLayout = inputLayoutEnd;
                }
                showLocationsList();
            }
        };

        editTextStart.setOnFocusChangeListener(focusListener);
        editTextEnd.setOnFocusChangeListener(focusListener);
    }

    private void setupFloorDropdown() {
        Set<Integer> sortedFloors = new TreeSet<>(selectedCampus.FloorNumberToDrawable.keySet());
        List<Integer> floorList = new ArrayList<>(sortedFloors);

        String[] floorStrings = floorList.stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_floor_dropdown,
                floorStrings
        );

        floorAutoComplete.setAdapter(adapter);
        floorAutoComplete.setText("1", false);
        floorAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            int floorNumber = Integer.parseInt(selected);
            switchToFloor(floorNumber);
        });
        switchToFloor(1);
    }

    private void switchToFloor(int floorNumber) {
        selectedFloor = String.valueOf(floorNumber);
        Integer drawableRes = selectedCampus.FloorNumberToDrawable.get(floorNumber);
        if (drawableRes == null) {
            throw new RuntimeException("drawableRes cannot be null");
        }
        floorMapView.setImageResource(drawableRes);
        Map<String, GraphNode> floorGraph = graphManager.CampusGraph.get(selectedFloor);
        if (floorGraph == null) {
            throw new RuntimeException(String.format("Граф этажа %s у корпуса %s не может быть null", selectedFloor, selectedCampus.Id));
        }

        floorMapView.loadFloorGraphForCampus(floorGraph);
        floorMapView.setFloor(selectedFloor);
        floorAutoComplete.setText(String.valueOf(floorNumber), false);
        if (pathWithTransition != null) {
            floorMapView.updatePath(pathWithTransition.segmentedPath.get(selectedFloor));
        }
        floorMapView.postDelayed(() -> floorMapView.setScale(2.0f, true), 200);

        if (editorController != null)
            editorController.setCurrentFloor(selectedFloor);
    }

    private void setupLocationsListAdapter() {
        locationsList = findViewById(R.id.LocationsList);
        locationAdapter = new LocationsListAdapter(node -> {
            currentFocusedInputField.setText(node.name);
            if (currentFocusedInputField.getId() == R.id.editTextStart) {
                selectedStartNode = node;
            } else if (currentFocusedInputField.getId() == R.id.editTextEnd) {
                selectedEndNode = node;
            }
            hideLocationsList();
        });

        locationsList.setAdapter(locationAdapter);
        locationsList.setLayoutManager(new LinearLayoutManager(this));

        TextWatcher filterWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (currentFocusedInputLayout == inputLayoutStart) {
                    selectedStartNode = null;
                } else if (currentFocusedInputLayout == inputLayoutEnd) {
                    selectedEndNode = null;
                }
                locationAdapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };

        EditText editTextStart = findViewById(R.id.editTextStart);
        EditText editTextEnd = findViewById(R.id.editTextEnd);

        editTextStart.addTextChangedListener(filterWatcher);
        editTextEnd.addTextChangedListener(filterWatcher);
    }

    private void showLocationsList() {
        if (isLocationsListDisplays)
            return;
        isLocationsListDisplays = true;

        hideMainUI();
        hideOppositeInputField();
        backButton.setVisibility(View.VISIBLE);

        LinearLayout layout = findViewById(R.id.inputFieldsWithBackButtonLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout.setLayoutParams(params);

        if (!currentFocusedInputField.getText().toString().trim().isEmpty())
            currentFocusedInputLayout.setEndIconVisible(true);

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
        transition.addListener(new ShowUiTransitionListener(() -> locationsList.setVisibility(View.VISIBLE)));

        TransitionManager.beginDelayedTransition(rootLayout, transition);
        constraints.applyTo(rootLayout);


        List<GraphNode> locationNodes;
        if (currentFocusedInputField.getId() == R.id.editTextStart) {
            Set<String> excludedNames = Set.of("Туалет (М)", "Туалет (Ж)", "Автомат с кофе", "Автомат с едой", "Лифт");
            locationNodes = graphManager.getNodesWithNamesInCampus(excludedNames);
        } else {
            Set<String> excludedNames = Set.of("Лифт");
            locationNodes = graphManager.getNodesWithNamesInCampus(excludedNames);
        }

        locationAdapter.updateList(locationNodes);
    }

    public void hideLocationsList() {
        if (!isLocationsListDisplays)
            return;
        isLocationsListDisplays = false;

        currentFocusedInputLayout.setEndIconVisible(false);
        currentFocusedInputLayout = null;

        locationsList.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        hideKeyboard();
        currentFocusedInputField.clearFocus();
        currentFocusedInputField = null;

        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(rootLayout);

        LinearLayout layout = findViewById(R.id.inputFieldsWithBackButtonLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        float dp = 305f;
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
        params.width = px;
        layout.setLayoutParams(params);

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
        transition.addListener(new ShowUiTransitionListener(() -> {
            showMainUI();
            showBothInputFields();
        }));

        TransitionManager.beginDelayedTransition(rootLayout, transition);
        constraints.applyTo(rootLayout);
    }

    private void hideMainUI() {
        floorMapView.setVisibility(View.GONE);
        floorPanel.setVisibility(View.GONE);
        makePathButton.setVisibility(View.GONE);
        flagElevator.setVisibility(View.GONE);
        if (isDeveloperMode)
            editorControls.setVisibility(View.GONE);
    }

    private void showMainUI() {
        floorMapView.setVisibility(View.VISIBLE);
        floorPanel.setVisibility(View.VISIBLE);
        makePathButton.setVisibility(View.VISIBLE);
        flagElevator.setVisibility(View.VISIBLE);
        if (isDeveloperMode)
            editorControls.setVisibility(View.VISIBLE);
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

    private void hideBothInputFields() {
        inputLayoutStart.setVisibility(View.GONE);
        inputLayoutEnd.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocusedInputField.getWindowToken(), 0);
        }
    }
}
