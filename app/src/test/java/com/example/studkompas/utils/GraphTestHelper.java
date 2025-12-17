package com.example.studkompas.utils;

import com.example.studkompas.model.GraphNode;
import com.example.studkompas.model.PathWithTransition;
import com.example.studkompas.model.TransitionPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphTestHelper {
    /**
     * Получить узел по ID
     */
    public static GraphNode getNodeById(Map<String, Map<String, GraphNode>> graph, String nodeId) {
        for (Map<String, GraphNode> floor : graph.values()) {
            if (floor.containsKey(nodeId)) {
                return floor.get(nodeId);
            }
        }
        return null;
    }

    /**
     * Получить все узлы графа
     */
    public static List<GraphNode> getAllNodes(Map<String, Map<String, GraphNode>> graph) {
        List<GraphNode> nodes = new ArrayList<>();
        for (Map<String, GraphNode> floor : graph.values()) {
            nodes.addAll(floor.values());
        }
        return nodes;
    }

    /**
     * Подсчитать общее количество узлов
     */
    public static int countNodes(Map<String, Map<String, GraphNode>> graph) {
        int count = 0;
        for (Map<String, GraphNode> floor : graph.values()) {
            count += floor.size();
        }
        return count;
    }

    /**
     * Проверить, содержит ли путь указанный узел
     */
    public static boolean pathContainsNode(PathWithTransition path, String nodeName) {
        if (path == null || path.segmentedPath == null) return false;

        for (Map.Entry<String, List<List<GraphNode>>> entry : path.segmentedPath.entrySet()) {
            for (List<GraphNode> segment : entry.getValue()) {
                for (GraphNode node : segment) {
                    if (node.name.equals(nodeName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверить, содержит ли путь переход через указанный тип узла
     */
    public static boolean pathHasTransitionType(PathWithTransition path, String nodeType) {
        if (path == null || path.transitionNodes == null) return false;

        for (TransitionPoint tp : path.transitionNodes) {
            if (tp.fromNode.name.contains(nodeType) || tp.targetNodeId.name.contains(nodeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Создать изолированный узел (без связей)
     */
    public static GraphNode createIsolatedNode(String id, String name, String floor, float x, float y) {
        return new GraphNode(id, name, floor, new float[]{x, y});
    }
}