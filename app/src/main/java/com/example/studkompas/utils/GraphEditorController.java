package com.example.studkompas.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.studkompas.R;
import com.example.studkompas.model.CustomPhotoView;

public class GraphEditorController {
    private final Activity activity;
    private final CustomPhotoView photoView;
    private final String campusId;
    private String currentFloor = "1";

    private boolean edgeSelectionMode = false;
    private String firstSelectedNodeId = null;

    public GraphEditorController(Activity activity, CustomPhotoView photoView, String campusId) {
        this.activity = activity;
        this.photoView = photoView;
        this.campusId = campusId;
        setup();
    }

    public void setup() {
        Button btnEdgeMode = activity.findViewById(R.id.btn_edge_mode);
        btnEdgeMode.setOnClickListener(v -> {
            edgeSelectionMode = !edgeSelectionMode;
            firstSelectedNodeId = null;
            if (edgeSelectionMode) {
                btnEdgeMode.setText("Режим добавления ребер");
            } else {
                btnEdgeMode.setText("Режим добавления вершин");
            }
        });

        photoView.setOnPhotoTapListener((view, x, y) -> HandlePhotoTap(x, y, btnEdgeMode));
    }

    private void HandlePhotoTap(float x, float y, Button btnEdgeMode) {
        Drawable drawable = photoView.getDrawable();
        if (drawable == null)
            return;

        float pixelX = x * drawable.getIntrinsicWidth();
        float pixelY = y * drawable.getIntrinsicHeight();

        if (edgeSelectionMode) {
            handleEdgeModeTap(pixelX, pixelY, btnEdgeMode);
        } else {
            handleAddNodeModeTap(pixelX, pixelY);
        }
    }

    private void handleEdgeModeTap(float pixelX, float pixelY, Button btnEdgeMode) {
        String tappedNodeId = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (tappedNodeId == null) {
            Toast.makeText(activity, "Узел не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firstSelectedNodeId == null) {
            firstSelectedNodeId = tappedNodeId;
            btnEdgeMode.setText("Выбрана вершина: " + tappedNodeId);
        } else if (!firstSelectedNodeId.equals(tappedNodeId)) {
            GraphManager.addEdge(activity, campusId, currentFloor, firstSelectedNodeId, tappedNodeId);
            photoView.invalidate();
            firstSelectedNodeId = null;
            btnEdgeMode.setText("Режим добавления ребер");
        }
    }

    private void handleAddNodeModeTap(float pixelX, float pixelY) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Введите имя вершины");

        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Например: Кабинет 101");
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String nodeName = input.getText().toString().trim();
            GraphManager.addNode(activity, campusId, currentFloor, pixelX, pixelY, nodeName);
            photoView.invalidate();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    public void setCurrentFloor(String floor) {
        this.currentFloor = floor;
    }
}