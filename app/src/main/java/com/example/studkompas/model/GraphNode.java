package com.example.studkompas.model;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    public final String id;
    public final String name;
    public float[] location;
    public List<String> edges = new ArrayList<>();

    public GraphNode(String id, String name, float[] location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}

