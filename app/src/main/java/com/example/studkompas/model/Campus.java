package com.example.studkompas.model;

import java.util.Map;

public class Campus {
    public String Name;
    public String Address;
    public Map<Integer, Integer> FloorsDrawableIds;

    public Campus(String name, String address, Map<Integer, Integer> floorDrawableResIds) {
        this.Name = name;
        this.Address = address;
        this.FloorsDrawableIds = floorDrawableResIds;
    }
}
