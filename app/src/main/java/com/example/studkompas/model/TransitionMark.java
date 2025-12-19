package com.example.studkompas.model;

import android.graphics.RectF;

public class TransitionMark {
    public final GraphNode fromNode;
    public final String targetFloor;
    public RectF displayBounds = new RectF();

    public TransitionMark(GraphNode fromNode, String targetFloor) {
        this.fromNode = fromNode;
        this.targetFloor = targetFloor;
    }
}