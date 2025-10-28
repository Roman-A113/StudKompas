package com.example.studkompas.model;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class Floor {
    public int Number;
    public List<Room> Rooms;

    public PhotoView Photo;

    public Floor(int number, List<Room> rooms) {
        Number = number;
        Rooms = rooms;
    }
}
