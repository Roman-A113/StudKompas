package com.example.studkompas.model;

import java.io.Serializable;
import java.util.Map;

public class Campus implements Serializable {
    public final String Name;
    public final String Id;
    public final String Address;
    public final Map<Integer, Integer> FloorNumberToDrawable;

    public Campus(String name, String id, String address, Map<Integer, Integer> floorDrawableResIds) {
        Name = name;
        Id = id;
        Address = address;
        FloorNumberToDrawable = floorDrawableResIds;
    }
}
