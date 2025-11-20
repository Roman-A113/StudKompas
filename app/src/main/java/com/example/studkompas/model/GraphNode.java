package com.example.studkompas.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphNode {
    public final String id;
    public String name;
    public String floor;
    public float[] location;
    public List<String> edges = new ArrayList<>();
    public Map<String, String> interFloorEdges = new HashMap<>();

    public GraphNode(String id, String name, String floor, float[] location) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.location = location;
    }
}

