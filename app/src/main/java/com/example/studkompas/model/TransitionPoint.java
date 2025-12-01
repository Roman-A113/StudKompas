package com.example.studkompas.model;

public class TransitionPoint {
    public final GraphNode fromNode;
    public final String targetFloor;
    public final GraphNode targetNodeId;

    public TransitionPoint(GraphNode fromNode, String targetFloor, GraphNode targetNode) {
        this.fromNode = fromNode;
        this.targetFloor = targetFloor;
        this.targetNodeId = targetNode;
    }
}