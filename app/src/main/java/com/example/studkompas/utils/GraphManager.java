package com.example.studkompas.utils;

import android.content.Context;

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
    private final Campus campus;
    private final String fileName;
    private final Context context;
    public Map<String, Map<String, GraphNode>> CampusGraph;

    public GraphManager(Context context, Campus campus) {
        this.context = context;
        this.campus = campus;
        this.fileName = campus.Id + ".json";
    }

    public void loadCampusGraphFromAssets() {
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Type type = new TypeToken<Map<String, Map<String, GraphNode>>>() {
            }.getType();
            CampusGraph = new Gson().fromJson(sb.toString(), type);

        } catch (Exception e) {
            throw new RuntimeException(String.format("Ошибка при загрузке файла %s из assets", fileName), e);
        }
    }

    public void saveCampusGraphToTempFile() {
        File file = new File(context.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(CampusGraph));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Ошибка при сохранении графа у корпуса %s в temp-файл", campus.Id), e);
        }
    }

    public GraphNode findNodeAt(float relX, float relY, String floor) {
        Map<String, GraphNode> floorGraph = CampusGraph.get(floor);

        final float TOL = 0.005f;

        if (floorGraph == null) {
            throw new RuntimeException(String.format("Граф этажа %s у корпуса %s не может быть null", floor, campus.Id));
        }

        for (GraphNode node : floorGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;
            float dx = node.location[0] - relX;
            float dy = node.location[1] - relY;
            if (Math.abs(dx) <= TOL && Math.abs(dy) <= TOL) {
                return node;
            }
        }
        return null;
    }

    public void addNode(String floor, float x, float y, String name) {
        int maxId = 0;
        for (Map<String, GraphNode> floorMap : CampusGraph.values()) {
            for (String id : floorMap.keySet()) {
                int numericId = Integer.parseInt(id);
                if (numericId > maxId) {
                    maxId = numericId;
                }
            }
        }

        String newId = String.valueOf(maxId + 1);

        GraphNode newNode = new GraphNode(newId, name, floor, new float[]{x, y});

        Map<String, GraphNode> floorGraph = CampusGraph.get(floor);
        if (floorGraph == null) {
            throw new RuntimeException(String.format("Граф этажа %s у корпуса %s не может быть null", floor, campus.Id));
        }
        floorGraph.put(newId, newNode);

        saveCampusGraphToTempFile();
    }

    public void deleteNode(String floor, GraphNode node) {
        Map<String, GraphNode> floorGraph = CampusGraph.get(floor);
        if (floorGraph == null)
            return;

        floorGraph.remove(node.id);

        for (Map<String, GraphNode> otherFloor : CampusGraph.values()) {
            for (GraphNode to : otherFloor.values()) {
                to.edges.remove(node.id);
                to.interFloorEdges.remove(node.id);
            }
        }

        saveCampusGraphToTempFile();
    }

    public void addEdge(GraphNode node1, GraphNode node2) {
        if (node1.floor.equals(node2.floor)) {
            node1.edges.add(node2.id);
            node2.edges.add(node1.id);
        } else {
            node1.interFloorEdges.put(node2.id, node2.floor);
            node2.interFloorEdges.put(node1.id, node1.floor);
        }
        saveCampusGraphToTempFile();
    }

    public void updateNodePosition(GraphNode node, float x, float y) {
        if (node == null)
            return;

        if (node.location == null || node.location.length < 2) {
            node.location = new float[2];
        }
        node.location[0] = x;
        node.location[1] = y;

        saveCampusGraphToTempFile();
    }

    public void renameNode(GraphNode node, String newName) {
        if (node != null) {
            node.name = newName;
            saveCampusGraphToTempFile();
        }
    }

    public List<GraphNode> getNodesInCampus() {
        List<GraphNode> result = new ArrayList<>();

        for (Map<String, GraphNode> floorGraph : CampusGraph.values()) {
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

    public PathWithTransition getPath(GraphNode startNode, GraphNode endNode) {
        Map<String, GraphNode> allNodes = new HashMap<>();
        for (Map<String, GraphNode> floorGraph : CampusGraph.values()) {
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
            if (currentId == null) {
                throw new RuntimeException(String.format("id у вершины не может быть null в корпусе %s", campus.Id));
            }

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
            if (currentNode == null) {
                throw new RuntimeException(String.format("вершина не может быть null у корпуса %s", campus.Id));
            }

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
