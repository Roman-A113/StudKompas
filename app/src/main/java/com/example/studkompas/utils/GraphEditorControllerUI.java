package com.example.studkompas.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.studkompas.R;
import com.example.studkompas.model.CustomPhotoView;

public class GraphEditorControllerUI {
    private final Activity activity;
    private final CustomPhotoView photoView;
    private final String campusId;
    Button buttonAddNode;
    Button buttonAddEdge;
    Button buttonMoveNode;
    Button buttonRenameNode;
    Button buttonToggleGraph;
    private String currentFloor = "1";
    private String selectedNodeId = null;
    private EditMode currentMode = null;

    public GraphEditorControllerUI(Activity activity, CustomPhotoView photoView, String campusId) {
        this.activity = activity;
        this.photoView = photoView;
        this.campusId = campusId;
        setup();
    }

    public void setCurrentFloor(String floor) {
        this.currentFloor = floor;
    }

    public void setup() {
        buttonAddNode = activity.findViewById(R.id.btn_add_node);
        buttonAddEdge = activity.findViewById(R.id.btn_add_edge);
        buttonMoveNode = activity.findViewById(R.id.btn_move_node);
        buttonRenameNode = activity.findViewById(R.id.btn_rename_node);
        buttonToggleGraph = activity.findViewById(R.id.btn_toggle_graph);

        resetColors();
        buttonAddNode.setOnClickListener(v -> setMode(EditMode.ADD_NODE, "Режим добавления вершин", buttonAddNode));
        buttonAddEdge.setOnClickListener(v -> setMode(EditMode.ADD_EDGE, "Режим добавления ребер", buttonAddEdge));
        buttonMoveNode.setOnClickListener(v -> setMode(EditMode.MOVE_NODE, "Режим перемещения вершин", buttonMoveNode));
        buttonRenameNode.setOnClickListener(v -> setMode(EditMode.RENAME_NODE, "Режим переименования вершин", buttonRenameNode));

        buttonToggleGraph.setOnClickListener(v -> {
            resetColors();
            if (currentMode != EditMode.HIDE_GRAPH) {
                currentMode = EditMode.HIDE_GRAPH;
                buttonToggleGraph.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
                buttonToggleGraph.setText("показать граф");
                photoView.setGraphVisible(false);
            } else {
                currentMode = null;
                buttonToggleGraph.setText("скрыть граф");
                photoView.setGraphVisible(true);
            }
        });

        photoView.setOnPhotoTapListener((view, x, y) -> HandlePhotoTap(x, y));
    }

    private void setMode(EditMode mode, String toastMessage, Button activeButton) {
        if (currentMode == EditMode.HIDE_GRAPH) {
            Toast.makeText(activity, "Граф скрыт, рисовать нельзя", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
        resetColors();
        currentMode = mode;
        activeButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
    }

    private void resetColors() {
        int greyColor = ContextCompat.getColor(activity, R.color.grey);
        buttonAddNode.setBackgroundColor(greyColor);
        buttonAddEdge.setBackgroundColor(greyColor);
        buttonMoveNode.setBackgroundColor(greyColor);
        buttonToggleGraph.setBackgroundColor(greyColor);
        buttonRenameNode.setBackgroundColor(greyColor);
    }

    private void HandlePhotoTap(float x, float y) {
        Drawable drawable = photoView.getDrawable();
        if (drawable == null)
            return;

        if (currentMode == null)
            return;

        switch (currentMode) {
            case ADD_NODE:
                handleAddNodeModeTap(x, y);
                break;
            case ADD_EDGE:
                handleEdgeModeTap(x, y);
                break;
            case MOVE_NODE:
                handleMoveNodeTap(x, y);
                break;
            case RENAME_NODE:
                handleRenameNodeTap(x, y);
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

    private void handleEdgeModeTap(float pixelX, float pixelY) {
        String tappedNodeId = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (tappedNodeId == null) {
            Toast.makeText(activity, "Узел не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedNodeId == null) {
            selectedNodeId = tappedNodeId;
            buttonAddEdge.setText("вершина: " + tappedNodeId);
        } else if (!selectedNodeId.equals(tappedNodeId)) {
            GraphManager.addEdge(activity, campusId, currentFloor, selectedNodeId, tappedNodeId);
            photoView.invalidate();
            selectedNodeId = null;
            buttonAddEdge.setText("ребра");
        }
    }

    private void handleMoveNodeTap(float pixelX, float pixelY) {
        String nodeId = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (selectedNodeId == null) {
            if (nodeId == null) {
                Toast.makeText(activity, "Вершина не найдена", Toast.LENGTH_SHORT).show();
                return;
            }
            buttonMoveNode.setText("вершина: " + nodeId);
            selectedNodeId = nodeId;
            return;
        }

        GraphManager.updateNodePosition(activity, campusId, currentFloor, selectedNodeId, pixelX, pixelY);
        photoView.invalidate();
        Toast.makeText(activity, "Вершина " + selectedNodeId + " перемещена", Toast.LENGTH_SHORT).show();
        buttonMoveNode.setText("переместить");
        selectedNodeId = null;
    }

    private void handleRenameNodeTap(float pixelX, float pixelY) {
        String nodeId = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (nodeId == null) {
            Toast.makeText(activity, "Вершина не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentName = GraphManager.getNodeName(campusId, currentFloor, nodeId);
        if (currentName == null) currentName = "";

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Переименовать вершину");

        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName);
        input.setSelection(input.getText().length()); // курсор в конец
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            GraphManager.renameNode(activity, campusId, currentFloor, nodeId, newName);
            photoView.invalidate();
            Toast.makeText(activity, "Вершина переименована", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private enum EditMode {
        ADD_NODE,
        ADD_EDGE,
        MOVE_NODE,
        RENAME_NODE,
        HIDE_GRAPH
    }
}