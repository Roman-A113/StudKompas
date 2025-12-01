package com.example.studkompas.utils;

import android.content.Context;
import android.util.Log;

import com.example.studkompas.model.Campus;
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.model.PathWithTransition;
import com.example.studkompas.model.TransitionPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphManager {
    private static final String TAG = "GraphManager";
    private static final String GRAPH_FILENAME = "graph.json";
    public static Map<String, Map<String, Map<String, GraphNode>>> Graphs = new HashMap<>();

    public static void loadGraphFromAssets(Context context) {
        try (InputStream is = context.getAssets().open("graph.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Type type = new TypeToken<Map<String, Map<String, Map<String, GraphNode>>>>() {
            }.getType();
            Graphs = new Gson().fromJson(sb.toString(), type);

            if (Graphs == null) {
                Graphs = new HashMap<>();
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке graph.json из assets", e);
            Graphs = new HashMap<>();
        }
    }

    public static void saveGraphToTempFile(Context context) {
        File file = new File(context.getFilesDir(), GRAPH_FILENAME);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(Graphs));
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка сохранения graph.json", e);
        }
    }

    public static void copyAssetGraphToTempFile(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("graph.json");
            File outFile = new File(context.getFilesDir(), "graph.json");
            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось скопировать graph.json из assets", e);
        }
    }

    public static GraphNode findNodeAt(float relX, float relY, String campusKey, String floor) {
        Map<String, GraphNode> campusGraph = Graphs.get(campusKey).get(floor);
        if (campusGraph == null) return null;

        final float TOL = 0.005f;

        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;
            float dx = node.location[0] - relX;
            float dy = node.location[1] - relY;
            if (Math.abs(dx) <= TOL && Math.abs(dy) <= TOL) {
                return node;
            }
        }
        return null;
    }

    public static void addNode(Context context, String campusKey, String floor, float x, float y, String name) {
        Graphs.putIfAbsent(campusKey, new HashMap<>());
        Graphs.get(campusKey).putIfAbsent(floor, new HashMap<>());

        int totalNodeCount = 0;
        for (Map<String, GraphNode> floorMap : Graphs.get(campusKey).values()) {
            totalNodeCount += floorMap.size();
        }
        String newId = String.valueOf(totalNodeCount + 1);

        GraphNode newNode = new GraphNode(newId, name, floor, new float[]{x, y});
        Graphs.get(campusKey).get(floor).put(newId, newNode);

        saveGraphToTempFile(context);
    }

    public static void addEdge(Context context, GraphNode node1, GraphNode node2) {
        if (node1.floor.equals(node2.floor)) {
            node1.edges.add(node2.id);
            node2.edges.add(node1.id);
        } else {
            node1.interFloorEdges.put(node2.id, node2.floor);
            node2.interFloorEdges.put(node1.id, node1.floor);
        }
        saveGraphToTempFile(context);
    }

    public static void updateNodePosition(Context context, GraphNode node, float x, float y) {
        if (node == null)
            return;

        if (node.location == null || node.location.length < 2) {
            node.location = new float[2];
        }
        node.location[0] = x;
        node.location[1] = y;

        saveGraphToTempFile(context);
    }

    public static void renameNode(Context context, GraphNode node, String newName) {
        if (node != null) {
            node.name = newName;
            saveGraphToTempFile(context);
        }
    }

    public static List<GraphNode> getNodesInCampus(String campusKey) {
        List<GraphNode> result = new ArrayList<>();
        Map<String, Map<String, GraphNode>> campusGraphs = Graphs.get(campusKey);

        for (Map<String, GraphNode> floorGraph : campusGraphs.values()) {
            if (floorGraph == null)
                continue;

            for (GraphNode node : floorGraph.values()) {
                String name = node.name.trim();
                if (name.isEmpty())
                    continue;
                result.add(node);
            }
        }

        return result;
    }

    public static PathWithTransition getPath(Campus campus, GraphNode startNode, GraphNode endNode) {
        String campusId = campus.Id;
        Map<String, Map<String, GraphNode>> campusGraphs = Graphs.get(campusId);
        if (campusGraphs == null) {
            throw new RuntimeException("Кампус не загружен: " + campusId);
        }

        Map<String, GraphNode> allNodes = new HashMap<>();
        for (Map<String, GraphNode> floorGraph : campusGraphs.values()) {
            allNodes.putAll(floorGraph);
        }

        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.offer(startNode.id);
        visited.add(startNode.id);
        parent.put(startNode.id, null);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            if (currentId.equals(endNode.id)) {
                List<GraphNode> fullPath = new ArrayList<>();
                String id = currentId;
                while (id != null) {
                    fullPath.add(allNodes.get(id));
                    id = parent.get(id);
                }
                Collections.reverse(fullPath);

                Map<String, List<List<GraphNode>>> segmentedPath = new HashMap<>();
                Set<TransitionPoint> transitionNodes = new HashSet<>();

                if (fullPath.isEmpty())
                    return new PathWithTransition(segmentedPath, new ArrayList<>(transitionNodes));

                List<GraphNode> currentSegment = new ArrayList<>();
                String currentFloor = fullPath.get(0).floor;
                currentSegment.add(fullPath.get(0));

                for (int i = 1; i < fullPath.size(); i++) {
                    GraphNode node = fullPath.get(i);
                    if (node.floor.equals(currentFloor)) {
                        currentSegment.add(node);
                    } else {
                        GraphNode previousNode = fullPath.get(i - 1);
                        segmentedPath.computeIfAbsent(currentFloor, k -> new ArrayList<>()).add(new ArrayList<>(currentSegment));

                        transitionNodes.add(new TransitionPoint(previousNode, node.floor, node));

                        currentSegment = new ArrayList<>();
                        currentFloor = node.floor;
                        currentSegment.add(node);
                    }
                }
                segmentedPath.computeIfAbsent(currentFloor, k -> new ArrayList<>()).add(new ArrayList<>(currentSegment));

                return new PathWithTransition(segmentedPath, new ArrayList<>(transitionNodes));
            }

            GraphNode currentNode = allNodes.get(currentId);
            for (String neighborId : currentNode.edges) {
                if (!visited.contains(neighborId) && allNodes.containsKey(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, currentId);
                    queue.offer(neighborId);
                }
            }

            for (String neighborId : currentNode.interFloorEdges.keySet()) {
                if (!visited.contains(neighborId) && allNodes.containsKey(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, currentId);
                    queue.offer(neighborId);
                }
            }
        }

        throw new RuntimeException(
                String.format("Путь между вершинами %s и %s не найден", startNode.name, endNode.name)
        );
    }
}
