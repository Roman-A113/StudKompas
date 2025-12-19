package com.example.studkompas.model;

public class TransitionMark {
    public final GraphNode fromNode;
    public final String targetFloor;

    public TransitionMark(GraphNode fromNode, String targetFloor) {
        this.fromNode = fromNode;
        this.targetFloor = targetFloor;
    }
}