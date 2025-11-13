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

import kotlin.NotImplementedError;

public class GraphEditorController {
    private final Activity activity;
    private final CustomPhotoView photoView;
    private final String campusId;
    private String currentFloor = "1";
    private String firstSelectedNodeId = null;

    private enum EditMode {
        ADD_NODE,
        ADD_EDGE,
        MOVE_NODE,
        HIDE_GRAPH
    }

    private EditMode currentMode = null;

    Button buttonAddNode;
    Button buttonAddEdge;
    Button buttonMoveNode;
    Button buttonToggleGraph;

    public GraphEditorController(Activity activity, CustomPhotoView photoView, String campusId) {
        this.activity = activity;
        this.photoView = photoView;
        this.campusId = campusId;
        setup();
    }

    public void setup() {
        buttonAddNode =  activity.findViewById(R.id.btn_add_node);
        buttonAddEdge =  activity.findViewById(R.id.btn_add_edge);
        buttonMoveNode =  activity.findViewById(R.id.btn_move_node);
        buttonToggleGraph =  activity.findViewById(R.id.btn_toggle_graph);

        resetColors();
        buttonAddNode.setOnClickListener(v -> {
            if (currentMode == EditMode.HIDE_GRAPH){
                Toast.makeText(activity, "Граф скрыт, рисовать нельзя", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(activity, "Режим добавления вершин", Toast.LENGTH_SHORT).show();
            resetColors();
            currentMode = EditMode.ADD_NODE;
            buttonAddNode.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
        });

        buttonAddEdge.setOnClickListener(v -> {
            if (currentMode == EditMode.HIDE_GRAPH){
                Toast.makeText(activity, "Граф скрыт, рисовать нельзя", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(activity, "Режим добавления ребер", Toast.LENGTH_SHORT).show();
            resetColors();
            currentMode = EditMode.ADD_EDGE;
            buttonAddEdge.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
        });

        buttonMoveNode.setOnClickListener(v -> {
            if (currentMode == EditMode.HIDE_GRAPH){
                Toast.makeText(activity, "Граф скрыт, рисовать нельзя", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(activity, "Режим перемещения вершин", Toast.LENGTH_SHORT).show();
            resetColors();
            currentMode = EditMode.MOVE_NODE;
            buttonMoveNode.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
        });

        buttonToggleGraph.setOnClickListener(v -> {
            resetColors();
            if (currentMode != EditMode.HIDE_GRAPH) {
                currentMode = EditMode.HIDE_GRAPH;
                buttonToggleGraph.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
                buttonToggleGraph.setText("показать граф");
            } else {
                currentMode = null;
                buttonToggleGraph.setText("скрыть граф");
            }
        });

        photoView.setOnPhotoTapListener((view, x, y) -> HandlePhotoTap(x, y));
    }

    private void resetColors(){
        int greyColor = ContextCompat.getColor(activity, R.color.grey);
        buttonAddNode.setBackgroundColor(greyColor);
        buttonAddEdge.setBackgroundColor(greyColor);
        buttonMoveNode.setBackgroundColor(greyColor);
        buttonToggleGraph.setBackgroundColor(greyColor);
    }

    private void HandlePhotoTap(float x, float y) {
        Drawable drawable = photoView.getDrawable();
        if (drawable == null)
            return;

        float pixelX = x * drawable.getIntrinsicWidth();
        float pixelY = y * drawable.getIntrinsicHeight();

        if(currentMode == null)
            return;

        switch(currentMode){
            case ADD_NODE:
                handleAddNodeModeTap(pixelX, pixelY);
                break;
            case ADD_EDGE:
                handleEdgeModeTap(pixelX, pixelY);
                break;
            case MOVE_NODE:
                handleMoveNodeTap();
                break;
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

        if (firstSelectedNodeId == null) {
            firstSelectedNodeId = tappedNodeId;
            buttonAddEdge.setText("вершина: " + tappedNodeId);
        } else if (!firstSelectedNodeId.equals(tappedNodeId)) {
            GraphManager.addEdge(activity, campusId, currentFloor, firstSelectedNodeId, tappedNodeId);
            photoView.invalidate();
            firstSelectedNodeId = null;
            buttonAddEdge.setText("ребра");
        }
    }

    private void handleMoveNodeTap(){
        throw new NotImplementedError();
    }

    public void setCurrentFloor(String floor) {
        this.currentFloor = floor;
    }
}