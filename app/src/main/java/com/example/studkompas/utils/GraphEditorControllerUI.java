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
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.ui.FloorMapView;

import java.util.ArrayList;
import java.util.List;

public class GraphEditorControllerUI {
    private final Activity activity;
    private final FloorMapView photoView;
    private final String campusId;
    private final List<GraphNode> selectedNodesForFullGraph = new ArrayList<>();
    Button buttonAddNode;
    Button buttonAddEdge;
    Button buttonMoveNode;
    Button buttonDeleteNode;
    Button buttonRenameNode;
    Button buttonToggleGraph;
    Button buttonFullGraph;

    private String currentFloor = "1";
    private EditMode currentMode;
    private GraphNode selectedNode;


    public GraphEditorControllerUI(Activity activity, FloorMapView photoView, String campusId) {
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
        buttonDeleteNode = activity.findViewById(R.id.btn_remove_node);
        buttonToggleGraph = activity.findViewById(R.id.btn_toggle_graph);
        buttonFullGraph = activity.findViewById(R.id.btn_full_graph);

        resetColors();
        buttonAddNode.setOnClickListener(v -> setMode(EditMode.ADD_NODE, "Режим добавления вершин", buttonAddNode));
        buttonAddEdge.setOnClickListener(v -> setMode(EditMode.ADD_EDGE, "Режим добавления ребер", buttonAddEdge));
        buttonMoveNode.setOnClickListener(v -> setMode(EditMode.MOVE_NODE, "Режим перемещения вершин", buttonMoveNode));
        buttonDeleteNode.setOnClickListener(v -> setMode(EditMode.DELETE_NODE, "Режим удаления вершин", buttonDeleteNode));
        buttonRenameNode.setOnClickListener(v -> setMode(EditMode.RENAME_NODE, "Режим переименования вершин", buttonRenameNode));
        buttonFullGraph.setOnClickListener(v -> {
            if (currentMode == EditMode.HIDE_GRAPH) {
                Toast.makeText(activity, "Граф скрыт, рисовать нельзя", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentMode != EditMode.FULL_GRAPH) {
                resetColors();
                currentMode = EditMode.FULL_GRAPH;
                selectedNodesForFullGraph.clear();
                buttonFullGraph.setText("Построить");
                buttonFullGraph.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
                Toast.makeText(activity, "Выберите вершины для полного графа", Toast.LENGTH_SHORT).show();
            } else {
                if (selectedNodesForFullGraph.size() < 2) {
                    Toast.makeText(activity, "Выберите хотя бы 2 вершины", Toast.LENGTH_SHORT).show();
                    return;
                }

                int edgeCount = 0;
                for (int i = 0; i < selectedNodesForFullGraph.size(); i++) {
                    for (int j = i + 1; j < selectedNodesForFullGraph.size(); j++) {
                        GraphNode a = selectedNodesForFullGraph.get(i);
                        GraphNode b = selectedNodesForFullGraph.get(j);
                        if (!a.edges.contains(b.id) && !b.edges.contains(a.id)) {
                            GraphManager.addEdge(activity, a, b);
                            edgeCount++;
                        }
                    }
                }

                photoView.invalidate();
                Toast.makeText(activity, "Добавлено " + edgeCount + " рёбер", Toast.LENGTH_SHORT).show();

                selectedNodesForFullGraph.clear();
                currentMode = null;
                buttonFullGraph.setText("полный граф");
                resetColors();
            }
        });


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
        buttonDeleteNode.setBackgroundColor(greyColor);
        buttonToggleGraph.setBackgroundColor(greyColor);
        buttonRenameNode.setBackgroundColor(greyColor);
        buttonFullGraph.setBackgroundColor(greyColor);
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
            case DELETE_NODE:
                handleDeleteNodeTap(x, y);
                break;
            case RENAME_NODE:
                handleRenameNodeTap(x, y);
                break;
            case FULL_GRAPH:
                handleMakeFullGraphTap(x, y);
        }
    }

    private void handleDeleteNodeTap(float x, float y) {
        GraphNode node = GraphManager.findNodeAt(x, y, campusId, currentFloor);
        if (node == null) {
            Toast.makeText(activity, "Вершина не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        GraphManager.deleteNode(activity, campusId, currentFloor, node);
        photoView.invalidate();
    }

    private void handleMakeFullGraphTap(float x, float y) {
        GraphNode tappedNode = GraphManager.findNodeAt(x, y, campusId, currentFloor);
        if (tappedNode == null) {
            Toast.makeText(activity, "Выберите вершину на карте", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean alreadySelected = selectedNodesForFullGraph.stream()
                .anyMatch(node -> node.id.equals(tappedNode.id));

        if (alreadySelected) {
            Toast.makeText(activity, "Вершина уже выбрана", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedNodesForFullGraph.add(tappedNode);
        Toast.makeText(activity, "Выбрано: " + tappedNode.name + " (" + selectedNodesForFullGraph.size() + ")", Toast.LENGTH_SHORT).show();
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
        GraphNode tappedNode = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (tappedNode == null) {
            Toast.makeText(activity, "Узел не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedNode == null) {
            selectedNode = tappedNode;
            buttonAddEdge.setText("вершина: " + tappedNode.id);
        } else {
            if (selectedNode.id.equals(tappedNode.id)) {
                Toast.makeText(activity, "Выберите другой узел", Toast.LENGTH_SHORT).show();
                return;
            }
            GraphManager.addEdge(activity, selectedNode, tappedNode);
            photoView.invalidate();
            selectedNode = null;
            buttonAddEdge.setText("ребра");
        }
    }

    private void handleMoveNodeTap(float pixelX, float pixelY) {
        GraphNode node = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (selectedNode == null) {
            if (node == null) {
                Toast.makeText(activity, "Вершина не найдена", Toast.LENGTH_SHORT).show();
                return;
            }
            buttonMoveNode.setText("вершина: " + node.id);
            selectedNode = node;
            return;
        }

        GraphManager.updateNodePosition(activity, selectedNode, pixelX, pixelY);
        photoView.invalidate();
        Toast.makeText(activity, "Вершина " + selectedNode.id + " перемещена", Toast.LENGTH_SHORT).show();
        buttonMoveNode.setText("переместить");
        selectedNode = null;
    }

    private void handleRenameNodeTap(float pixelX, float pixelY) {
        GraphNode node = GraphManager.findNodeAt(pixelX, pixelY, campusId, currentFloor);
        if (node == null) {
            Toast.makeText(activity, "Вершина не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentName = node.name;
        if (currentName == null)
            currentName = "";

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Переименовать вершину");

        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName);
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            GraphManager.renameNode(activity, node, newName);
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
        DELETE_NODE,
        RENAME_NODE,
        HIDE_GRAPH,
        FULL_GRAPH
    }
}