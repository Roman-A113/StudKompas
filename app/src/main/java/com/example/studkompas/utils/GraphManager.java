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
import java.util.function.Predicate;

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

        final float TOL = 0.0015f;

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

    public List<GraphNode> getNodesWithNamesInCampus(Set<String> excludedNames) {
        if (excludedNames == null) {
            excludedNames = Collections.emptySet();
        }

        Map<String, GraphNode> uniqueByName = new HashMap<>();

        for (Map<String, GraphNode> floorGraph : CampusGraph.values()) {
            if (floorGraph == null) continue;

            for (GraphNode node : floorGraph.values()) {
                String name = node.name.trim();
                if (name.isEmpty() || excludedNames.contains(name)) {
                    continue;
                }
                uniqueByName.putIfAbsent(name, node);
            }
        }

        return new ArrayList<>(uniqueByName.values());
    }

    private Map<String, GraphNode> getAllIdsToGraphNodes() {
        Map<String, GraphNode> result = new HashMap<>();
        for (Map<String, GraphNode> floorGraph : CampusGraph.values()) {
            result.putAll(floorGraph);
        }
        return result;
    }

    public PathWithTransition getPathBetweenTwoNodes(GraphNode startNode, GraphNode endNode) {
        Map<String, GraphNode> allNodes = getAllIdsToGraphNodes();
        return bfsWithPredicate(startNode, allNodes, node -> node.id.equals(endNode.id));
    }

    public PathWithTransition getPathByTargetName(GraphNode startNode, String targetName) {
        Map<String, GraphNode> allNodes = getAllIdsToGraphNodes();
        return bfsWithPredicate(startNode, allNodes, node -> node.name.equals(targetName));
    }

    private PathWithTransition bfsWithPredicate(GraphNode startNode, Map<String, GraphNode> allNodes, Predicate<GraphNode> isTargetNode) {
        Queue<GraphNode> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.offer(startNode);
        visited.add(startNode.id);
        parent.put(startNode.id, null);

        while (!queue.isEmpty()) {
            GraphNode currentNode = queue.poll();
            assert currentNode != null;

            if (isTargetNode.test(currentNode)) {
                return buildPathWithTransitions(currentNode, parent, allNodes);
            }

            for (String neighborId : currentNode.edges) {
                if (!visited.contains(neighborId) && allNodes.containsKey(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, currentNode.id);

                    GraphNode neighborNode = allNodes.get(neighborId);
                    queue.offer(neighborNode);
                }
            }

            for (String neighborId : currentNode.interFloorEdges.keySet()) {
                if (!visited.contains(neighborId) && allNodes.containsKey(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, currentNode.id);

                    GraphNode neighborNode = allNodes.get(neighborId);
                    queue.offer(neighborNode);
                }
            }
        }
        throw new RuntimeException(
                String.format("Путь от вершины %s до targetNode не найден", startNode.name)
        );
    }

    private PathWithTransition buildPathWithTransitions(GraphNode endNode, Map<String, String> parent, Map<String, GraphNode> allNodes) {
        List<GraphNode> fullPath = new ArrayList<>();
        String id = endNode.id;
        while (id != null) {
            GraphNode node = allNodes.get(id);
            if (node == null) {
                throw new RuntimeException("Найден несуществующий узел с id: " + id);
            }
            fullPath.add(node);
            id = parent.get(id);
        }
        Collections.reverse(fullPath);

        Map<String, List<List<GraphNode>>> segmentedPath = new HashMap<>();
        Set<TransitionPoint> transitionNodes = new HashSet<>();

        if (fullPath.isEmpty()) {
            return new PathWithTransition(segmentedPath, new ArrayList<>(transitionNodes));
        }

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
}
