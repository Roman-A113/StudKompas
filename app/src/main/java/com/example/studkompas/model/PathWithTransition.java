package com.example.studkompas.model;

import java.util.List;
import java.util.Map;

public class PathWithTransition {
    public final Map<String, List<List<GraphNode>>> segmentedPath;
    public final List<TransitionPoint> transitionNodes;

    public PathWithTransition(
            Map<String, List<List<GraphNode>>> segmentedPath,
            List<TransitionPoint> transitionNodes
    ) {
        this.segmentedPath = segmentedPath;
        this.transitionNodes = transitionNodes;
    }
}