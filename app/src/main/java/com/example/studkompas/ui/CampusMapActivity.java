package com.example.studkompas.ui;

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
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.utils.GraphManager;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CampusMapActivity extends AppCompatActivity {
    private CustomPhotoView photoView;
    private Campus selectedCampus;
    private String currentFloor = "1";

    private boolean edgeSelectionMode = false;
    private String firstSelectedNodeId = null;

    private LinearLayout floorButtonsContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        selectedCampus = (Campus) getIntent().getSerializableExtra("campus");

        floorButtonsContainer = findViewById(R.id.floorPanel);
        Set<Integer> sortedFloors = new TreeSet<>(selectedCampus.FloorNumberToDrawable.keySet());
        setupFloorButtons(sortedFloors);


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

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);
        photoView.setImageResource(selectedCampus.FloorNumberToDrawable.get(1));
        photoView.loadGraphForCampus(selectedCampus.Id, currentFloor);

        photoView.setOnPhotoTapListener((view, x, y) -> {
            Drawable drawable = photoView.getDrawable();

            float pixelX = x * drawable.getIntrinsicWidth();
            float pixelY = y * drawable.getIntrinsicHeight();

            if (edgeSelectionMode) {
                String tappedNodeId = findNodeAt(pixelX, pixelY, selectedCampus.Id);
                if (tappedNodeId != null) {
                    if (firstSelectedNodeId == null) {
                        firstSelectedNodeId = tappedNodeId;
                        btnEdgeMode.setText("Выбрана вершина: " + tappedNodeId);
                    } else if (!firstSelectedNodeId.equals(tappedNodeId)) {
                        GraphManager.addEdge(this, selectedCampus.Id, currentFloor, firstSelectedNodeId, tappedNodeId);
                        photoView.invalidate();
                        firstSelectedNodeId = null;
                        btnEdgeMode.setText("Режим добавления ребер");
                    }
                } else {
                    Toast.makeText(this, "Узел не найден", Toast.LENGTH_SHORT).show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CampusMapActivity.this);
                builder.setTitle("Введите имя вершины");

                final EditText input = new EditText(CampusMapActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Например: Кабинет 101");
                builder.setView(input);

                builder.setPositiveButton("Добавить", (dialog, which) -> {
                    String nodeName = input.getText().toString().trim();
                    GraphManager.addNode(CampusMapActivity.this, selectedCampus.Id, currentFloor, pixelX, pixelY, nodeName);
                    photoView.invalidate();
                });

                builder.setNegativeButton("Отмена", null);
                builder.show();
            }
        });
        photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);
    }

    private String findNodeAt(float x, float y, String campusKey) {
        Map<String, GraphNode> campusGraph = GraphManager.Graphs.get(campusKey).get(currentFloor);
        if (campusGraph == null) return null;

        final float TOLERANCE = 60f;

        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;
            float dx = node.location[0] - x;
            float dy = node.location[1] - y;
            if (dx * dx + dy * dy <= TOLERANCE * TOLERANCE) {
                return node.id;
            }
        }
        return null;
    }

    private void setupFloorButtons(Set<Integer> floorNumbers) {
        floorButtonsContainer.removeAllViews();

        for (Integer floorNumber : floorNumbers) {
            Button floorButton = new Button(this);
            floorButton.setText(String.valueOf(floorNumber));

            float density = getResources().getDisplayMetrics().density;
            int widthPx = (int) (48 * density + 0.5f);
            int heightPx = (int) (52 * density + 0.5f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);

            int marginDp = 5;
            int marginPx = (int) (marginDp * density + 0.5f);
            params.setMargins(marginPx, marginPx, marginPx, marginPx);

            floorButton.setLayoutParams(params);

            int paddingDp = 0;
            int paddingPx = (int) (paddingDp * density + 0.5f);
            floorButton.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            float cornerRadiusDp = 8f;
            float cornerRadiusPx = cornerRadiusDp * density;
            GradientDrawable roundedBackground = new GradientDrawable();
            roundedBackground.setColor(ContextCompat.getColor(this, R.color.green));
            roundedBackground.setCornerRadius(cornerRadiusPx);
            floorButton.setBackground(roundedBackground);

            floorButton.setTextColor(Color.BLACK);

            floorButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

            final int floor = floorNumber;
            floorButton.setOnClickListener(v -> {
                Integer floorDrawable = selectedCampus.FloorNumberToDrawable.get(floor);
                photoView.setImageResource(floorDrawable);
                currentFloor = String.valueOf(floor);
                photoView.loadGraphForCampus(selectedCampus.Id, currentFloor);
                photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);
            });
            floorButtonsContainer.addView(floorButton);
        }
    }
}
