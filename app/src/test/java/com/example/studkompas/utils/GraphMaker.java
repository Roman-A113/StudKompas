package com.example.studkompas.utils;

import com.example.studkompas.model.GraphNode;

import java.util.HashMap;
import java.util.Map;

public class GraphMaker {
    /**
     * Базовый граф: A -- B -- C (3 узла на одном этаже)
     */
    public static Map<String, Map<String, GraphNode>> createSimpleGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();
        Map<String, GraphNode> floor = new HashMap<>();

        GraphNode A = new GraphNode("1", "A", "1", new float[]{0.1f, 0.1f});
        GraphNode B = new GraphNode("2", "B", "1", new float[]{0.5f, 0.5f});
        GraphNode C = new GraphNode("3", "C", "1", new float[]{0.9f, 0.9f});

        A.edges.add("2");
        B.edges.add("1");
        B.edges.add("3");
        C.edges.add("2");

        floor.put("1", A);
        floor.put("2", B);
        floor.put("3", C);
        graph.put("1", floor);

        return graph;
    }

    /**
     * Граф с двумя путями: A->B->D (длинный) и A->C->D (короткий)
     */
    public static Map<String, Map<String, GraphNode>> createShortestPathGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();
        Map<String, GraphNode> floor = new HashMap<>();

        // Координаты так подобраны, что A->C->D короче, чем A->B->D
        GraphNode A = new GraphNode("1", "A", "1", new float[]{0.0f, 0.0f});
        GraphNode B = new GraphNode("2", "B", "1", new float[]{10.0f, 0.0f});  // далеко от A
        GraphNode C = new GraphNode("3", "C", "1", new float[]{2.0f, 0.0f});   // близко к A
        GraphNode D = new GraphNode("4", "D", "1", new float[]{4.0f, 0.0f});   // близко к C

        A.edges.add("2");
        A.edges.add("3");
        B.edges.add("4");
        C.edges.add("4");
        B.edges.add("1");
        C.edges.add("1");
        D.edges.add("2");
        D.edges.add("3");

        floor.put("1", A);
        floor.put("2", B);
        floor.put("3", C);
        floor.put("4", D);
        graph.put("1", floor);

        return graph;
    }

    /**
     * Граф с межэтажными переходами (лифт)
     */
    public static Map<String, Map<String, GraphNode>> createMultiFloorGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();

        // Этаж 1
        Map<String, GraphNode> floor1 = new HashMap<>();
        GraphNode lift1 = new GraphNode("1", "Лифт", "1", new float[]{0.5f, 0.5f});
        lift1.interFloorEdges.put("2", "2");  // связь с лифтом на 2 этаже

        // Этаж 2
        Map<String, GraphNode> floor2 = new HashMap<>();
        GraphNode lift2 = new GraphNode("2", "Лифт", "2", new float[]{0.5f, 0.5f});
        lift2.interFloorEdges.put("1", "1");
        GraphNode room = new GraphNode("3", "Кабинет 201", "2", new float[]{0.7f, 0.7f});
        lift2.edges.add("3");
        room.edges.add("2");

        floor1.put("1", lift1);
        floor2.put("2", lift2);
        floor2.put("3", room);

        graph.put("1", floor1);
        graph.put("2", floor2);

        return graph;
    }

    /**
     * Граф с единственным лифтом между этажами
     */
    public static Map<String, Map<String, GraphNode>> createElevatorOnlyGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();

        // Этаж 1
        Map<String, GraphNode> floor1 = new HashMap<>();
        GraphNode start = new GraphNode("1", "Старт", "1", new float[]{0.1f, 0.1f});
        GraphNode lift1 = new GraphNode("2", "Лифт", "1", new float[]{0.2f, 0.2f});
        start.edges.add("2");
        lift1.edges.add("1");
        lift1.interFloorEdges.put("4", "2");  // лифт на 2 этаже

        // Этаж 2
        Map<String, GraphNode> floor2 = new HashMap<>();
        GraphNode lift2 = new GraphNode("4", "Лифт", "2", new float[]{0.2f, 0.2f});
        GraphNode end = new GraphNode("5", "Конец", "2", new float[]{0.3f, 0.3f});
        lift2.interFloorEdges.put("2", "1");
        lift2.edges.add("5");
        end.edges.add("4");

        floor1.put("1", start);
        floor1.put("2", lift1);
        floor2.put("4", lift2);
        floor2.put("5", end);

        graph.put("1", floor1);
        graph.put("2", floor2);

        return graph;
    }

    /**
     * Циклический граф (A->B->C->A)
     */
    public static Map<String, Map<String, GraphNode>> createCircularGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();
        Map<String, GraphNode> floor = new HashMap<>();

        GraphNode A = new GraphNode("1", "A", "1", new float[]{0.1f, 0.1f});
        GraphNode B = new GraphNode("2", "B", "1", new float[]{0.4f, 0.4f});
        GraphNode C = new GraphNode("3", "C", "1", new float[]{0.7f, 0.7f});
        GraphNode D = new GraphNode("4", "D", "1", new float[]{0.9f, 0.9f});

        A.edges.add("2");
        B.edges.add("1");
        B.edges.add("3");
        C.edges.add("2");
        C.edges.add("4");
        D.edges.add("3");

        floor.put("1", A);
        floor.put("2", B);
        floor.put("3", C);
        floor.put("4", D);
        graph.put("1", floor);

        return graph;
    }

    /**
     * Граф с одним узлом (для теста из точки в ту же точку)
     */
    public static Map<String, Map<String, GraphNode>> createSingleNodeGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();
        Map<String, GraphNode> floor = new HashMap<>();

        GraphNode node = new GraphNode("1", "Кабинет 101", "1", new float[]{0.5f, 0.5f});
        floor.put("1", node);
        graph.put("1", floor);

        return graph;
    }

    /**
     * Граф с координатами на границах (очень маленькие/большие значения)
     */
    public static Map<String, Map<String, GraphNode>> createBoundaryCoordinatesGraph() {
        Map<String, Map<String, GraphNode>> graph = new HashMap<>();
        Map<String, GraphNode> floor = new HashMap<>();

        GraphNode corner1 = new GraphNode("1", "Угол1", "1", new float[]{0.0001f, 0.0001f});
        GraphNode corner2 = new GraphNode("2", "Угол2", "1", new float[]{0.9999f, 0.9999f});
        GraphNode center = new GraphNode("3", "Центр", "1", new float[]{0.5f, 0.5f});

        corner1.edges.add("3");
        center.edges.add("1");
        center.edges.add("2");
        corner2.edges.add("3");

        floor.put("1", corner1);
        floor.put("2", corner2);
        floor.put("3", center);
        graph.put("1", floor);

        return graph;
    }
}
