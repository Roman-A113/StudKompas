package com.example.studkompas.model;

import android.graphics.Point;

import com.github.chrisbanes.photoview.PhotoView;

public class Room {
    private final int number;
    private final String name;
    private final Point absoluteLocation;
    private final int floorIndex;
    private PhotoView floorImage;

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
