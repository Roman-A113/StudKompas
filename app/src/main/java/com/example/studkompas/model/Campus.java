package com.example.studkompas.model;

import java.io.Serializable;
import java.util.Map;

public class Campus implements Serializable {
    public String Name;
    public String Id;
    public String Address;
    public Map<Integer, Integer> FloorNumberToDrawable;

    public Campus(String name, String id, String address, Map<Integer, Integer> floorDrawableResIds) {
        Name = name;
        Id = id;
        Address = address;
        FloorNumberToDrawable = floorDrawableResIds;
    }
}
