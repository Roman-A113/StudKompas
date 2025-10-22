package com.example.studkompas;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createCampusList();

        findViewById(R.id.buttonHelp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
        findViewById(R.id.buttonSettings).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void createCampusList() {
        List<CampusItem> items = new ArrayList<>();
        items.add(new CampusItem("ГУК", "ул. Мира, 19"));
        items.add(new CampusItem("Матмех", "ул. Тургенева, 4"));
        items.add(new CampusItem("Биологический", "ул. Куйбышева, 48"));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBuildings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CampusListAdapter adapter = new CampusListAdapter(this, items);
        recyclerView.setAdapter(adapter);
    }
}