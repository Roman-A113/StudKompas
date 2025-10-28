package com.example.studkompas.model;

import android.graphics.Point;

import com.github.chrisbanes.photoview.PhotoView;

public class Room {
    private int number;
    private String name;
    private Point absoluteLocation;

    private PhotoView floorImage;
    private int floorIndex;

    public Room(int number, String name, Point location, int floorIndex) {
        this.number = number;
        this.name = name;
        absoluteLocation = location;
        this.floorIndex = floorIndex;
    }

    public Point GetRelativeLocation(Point absoluteLocation, PhotoView floorImage) {
        double width = floorImage.getWidth();
        double height = floorImage.getHeight();
        return new Point((int) (absoluteLocation.x / width), (int) (absoluteLocation.y / height));
    }
}
