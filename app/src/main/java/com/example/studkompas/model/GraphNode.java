package com.example.studkompas.model;

public class GraphNode {
    public final String id;
    public final String name;
    public final float[] location;
    public String[] edges = new String[0];

    public GraphNode(String id, String name, float[] location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}

