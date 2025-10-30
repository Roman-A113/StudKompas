package com.example.studkompas.model;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class Floor {
    public int Number;
    public List<GraphNode> Rooms;

    public PhotoView Photo;

    public Floor(int number, List<GraphNode> rooms) {
        Number = number;
        Rooms = rooms;
    }
}
