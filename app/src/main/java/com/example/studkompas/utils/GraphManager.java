package com.example.studkompas.utils;

import android.content.Context;
import android.util.Log;

import com.example.studkompas.model.Campus;
import com.example.studkompas.model.GraphNode;
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

    public static String findNodeAt(float relX, float relY, String campusKey, String floor) {
        Map<String, GraphNode> campusGraph = Graphs.get(campusKey).get(floor);
        if (campusGraph == null) return null;

        final float TOL = 0.005f;

        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;
            float dx = node.location[0] - relX;
            float dy = node.location[1] - relY;
            if (Math.abs(dx) <= TOL && Math.abs(dy) <= TOL) {
                return node.id;
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

        GraphNode newNode = new GraphNode(newId, name, new float[]{x, y});
        Graphs.get(campusKey).get(floor).put(newId, newNode);

        saveGraphToTempFile(context);
    }

    public static void addEdge(Context context, String campusKey, String floor, String nodeId1, String nodeId2) {
        Map<String, GraphNode> campusMap = Graphs.get(campusKey).get(floor);

        GraphNode node1 = campusMap.get(nodeId1);
        GraphNode node2 = campusMap.get(nodeId2);

        node1.edges.add(nodeId2);
        node2.edges.add(nodeId1);
        saveGraphToTempFile(context);
    }

    public static void updateNodePosition(Context context, String campusKey, String floor, String nodeId, float x, float y) {
        Map<String, Map<String, GraphNode>> campusGraphs = Graphs.get(campusKey);
        if (campusGraphs == null) return;

        Map<String, GraphNode> floorGraph = campusGraphs.get(floor);
        if (floorGraph == null) return;

        GraphNode node = floorGraph.get(nodeId);
        if (node == null) return;

        if (node.location == null || node.location.length < 2) {
            node.location = new float[2];
        }
        node.location[0] = x;
        node.location[1] = y;

        saveGraphToTempFile(context);
    }

    public static String getNodeName(String campusId, String floor, String nodeId) {
        Map<String, GraphNode> floorGraph = Graphs.get(campusId).get(floor);
        if (floorGraph == null) return null;
        GraphNode node = floorGraph.get(nodeId);
        return node != null ? node.name : null;
    }

    public static void renameNode(Context context, String campusId, String floor, String nodeId, String newName) {
        Map<String, GraphNode> floorGraph = Graphs.get(campusId).get(floor);
        if (floorGraph == null) return;
        GraphNode node = floorGraph.get(nodeId);
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

    public static List<GraphNode> getPath(Campus campus, String floor, GraphNode startNode, GraphNode endNode) {
        Map<String, GraphNode> floorGraph = Graphs.get(campus.Id).get(floor);

        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.offer(startNode.id);
        visited.add(startNode.id);
        parent.put(startNode.id, null);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (currentId.equals(endNode.id)) {
                List<GraphNode> path = new ArrayList<>();
                String id = currentId;
                while (id != null) {
                    path.add(floorGraph.get(id));
                    id = parent.get(id);
                }
                Collections.reverse(path);
                return path;
            }

            GraphNode currentNode = floorGraph.get(currentId);

            for (String neighborId : currentNode.edges) {
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, currentId);
                    queue.offer(neighborId);
                }
            }
        }

        String message = String.format("путь между вершинами %s и %s не найден", startNode.name, endNode.name);
        throw new RuntimeException(message);
    }
}
