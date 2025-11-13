package com.example.studkompas.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studkompas.R;
import com.example.studkompas.model.Campus;
import com.example.studkompas.model.CustomPhotoView;
import com.example.studkompas.utils.GraphManager;

import java.util.Set;
import java.util.TreeSet;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;
    private Campus selectedCampus;
    private String currentFloorStr;
    private boolean edgeSelectionMode = false;
    private String firstSelectedNodeId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");

        configurePhotoView();
        createFloorButtons();
        configureDrawGraphModeButtons();
    }

    private void configurePhotoView() {
        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);
        photoView.setOnPhotoTapListener((view, x, y) -> handlePhotoTap(x, y));
        switchToFloor(1);
    }

    private void handlePhotoTap(float normalizedX, float normalizedY) {
        Drawable drawable = photoView.getDrawable();

        float pixelX = normalizedX * drawable.getIntrinsicWidth();
        float pixelY = normalizedY * drawable.getIntrinsicHeight();

        if (edgeSelectionMode) {
            handleEdgeModeTap(pixelX, pixelY);
        } else {
            handleAddNodeModeTap(pixelX, pixelY);
        }
    }

    private void handleEdgeModeTap(float pixelX, float pixelY) {
        String tappedNodeId = GraphManager.findNodeAt(pixelX, pixelY, selectedCampus.Id, currentFloorStr);
        Button edgeModeButton = findViewById(R.id.btn_edge_mode);

        if (tappedNodeId == null) {
            Toast.makeText(this, "Узел не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firstSelectedNodeId == null) {
            firstSelectedNodeId = tappedNodeId;
            edgeModeButton.setText("Выбрана вершина: " + tappedNodeId);
        } else if (!firstSelectedNodeId.equals(tappedNodeId)) {
            GraphManager.addEdge(this, selectedCampus.Id, currentFloorStr, firstSelectedNodeId, tappedNodeId);
            photoView.invalidate();
            firstSelectedNodeId = null;
            edgeModeButton.setText("Режим добавления ребер");
        }
    }

    private void handleAddNodeModeTap(float pixelX, float pixelY) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CampusMapActivity.this);
        builder.setTitle("Введите имя вершины");

        final EditText input = new EditText(CampusMapActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Например: Кабинет 101");
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String nodeName = input.getText().toString().trim();
            GraphManager.addNode(CampusMapActivity.this, selectedCampus.Id, currentFloorStr, pixelX, pixelY, nodeName);
            photoView.invalidate();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void configureDrawGraphModeButtons() {
        Button btnEdgeMode = findViewById(R.id.btn_edge_mode);
        btnEdgeMode.setOnClickListener(v -> {
            edgeSelectionMode = !edgeSelectionMode;
            if (edgeSelectionMode) {
                firstSelectedNodeId = null;
                btnEdgeMode.setText("Режим добавления ребер");
            } else {
                btnEdgeMode.setText("Режим добавления вершин");
            }
        });
    }

    private void switchToFloor(int floorNumber) {
        currentFloorStr = String.valueOf(floorNumber);
        Integer drawableRes = selectedCampus.FloorNumberToDrawable.get(floorNumber);
        if (drawableRes == null) {
            throw new RuntimeException("drawableRes is null");
        }
        photoView.setImageResource(drawableRes);
        photoView.loadGraphForCampus(selectedCampus.Id, currentFloorStr);
        photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);
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

