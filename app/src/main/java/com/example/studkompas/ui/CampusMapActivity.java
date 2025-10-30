package com.example.studkompas.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studkompas.R;
import com.example.studkompas.model.Campus;
import com.example.studkompas.model.CustomPhotoView;
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.utils.GraphManager;

import java.util.ArrayList;
import java.util.Map;

public class CampusMapActivity extends AppCompatActivity {
    public static final String EXTRA_CAMPUS_NAME = "campus_name";
    private CustomPhotoView photoView;
    private Campus selectedCampus;
    private String campusKey;
    private ArrayList<Button> floorButtons;

    private boolean edgeSelectionMode = false;
    private String firstSelectedNodeId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        initializeFloorButtons();

        String campusName = getIntent().getStringExtra(EXTRA_CAMPUS_NAME);
        selectedCampus = findCampusByName(campusName);
        campusKey = getCampusKeyByName(campusName);

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

        Integer firstFloorResId = selectedCampus.FloorsDrawableIds.get(1);

        photoView = findViewById(R.id.imageViewCampusMap);
        photoView.setMaximumScale(10.0f);
        photoView.setImageResource(firstFloorResId);
        photoView.setOnPhotoTapListener((view, x, y) -> {
            Drawable drawable = photoView.getDrawable();

            float pixelX = x * drawable.getIntrinsicWidth();
            float pixelY = y * drawable.getIntrinsicHeight();

            if (edgeSelectionMode) {
                String tappedNodeId = findNodeAt(pixelX, pixelY, campusKey);
                if (tappedNodeId != null) {
                    if (firstSelectedNodeId == null) {
                        firstSelectedNodeId = tappedNodeId;
                        btnEdgeMode.setText("Выбрана вершина: " + tappedNodeId);
                    } else if (!firstSelectedNodeId.equals(tappedNodeId)) {
                        GraphManager.addEdge(this, campusKey, firstSelectedNodeId, tappedNodeId);
                        photoView.loadGraphForCampus(campusKey);
                        firstSelectedNodeId = null;
                        btnEdgeMode.setText("Режим добавления ребер");
                    }
                } else {
                    Toast.makeText(this, "Узел не найден", Toast.LENGTH_SHORT).show();
                }
            } else {
                GraphManager.addNode(CampusMapActivity.this, campusKey, pixelX, pixelY);
                photoView.loadGraphForCampus(campusKey);
            }
        });

        photoView.loadGraphForCampus(campusKey);

        photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);
        setupFloorButtons();
    }

    private String findNodeAt(float x, float y, String campusKey) {
        Map<String, GraphNode> campusGraph = GraphManager.Graphs.get(campusKey);
        if (campusGraph == null) return null;

        final float TOLERANCE = 60f; // пикселей (в координатах изображения)

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

    private void initializeFloorButtons() {
        floorButtons = new ArrayList<>();
        floorButtons.add(findViewById(R.id.btnFloor1));
        floorButtons.add(findViewById(R.id.btnFloor2));
        floorButtons.add(findViewById(R.id.btnFloor3));
        floorButtons.add(findViewById(R.id.btnFloor4));
        floorButtons.add(findViewById(R.id.btnFloor5));
        floorButtons.add(findViewById(R.id.btnFloor6));
        floorButtons.add(findViewById(R.id.btnFloor7));
        floorButtons.add(findViewById(R.id.btnFloor8));
        floorButtons.add(findViewById(R.id.btnFloor9));
        floorButtons.add(findViewById(R.id.btnFloor10));
    }

    private void setupFloorButtons() {
        for (Button button : floorButtons) {
            button.setVisibility(View.GONE);
        }

        for (int floorNumber : selectedCampus.FloorsDrawableIds.keySet()) {
            int buttonIndex = floorNumber - 1;
            Button button = floorButtons.get(buttonIndex);
            button.setVisibility(View.VISIBLE);
            button.setText(String.valueOf(floorNumber));
            final int floor = floorNumber;
            button.setOnClickListener(v -> {
                Integer resId = selectedCampus.FloorsDrawableIds.get(floor);
                photoView.setImageResource(resId);

                photoView.loadGraphForCampus("guk");
                photoView.postDelayed(() -> photoView.setScale(2.0f, true), 200);
            });
        }
    }

    private Campus findCampusByName(String name) {
        for (Campus campus : MainActivity.Campuses) {
            if (campus.Name.equals(name)) {
                return campus;
            }
        }
        return null;
    }

    private String getCampusKeyByName(String name) {
        if ("ГУК".equals(name)) return "guk";
        if ("Матмех".equals(name)) return "turgeneva";
        if ("Биологический".equals(name)) return "bio";
        return "guk"; // по умолчанию
    }
}
