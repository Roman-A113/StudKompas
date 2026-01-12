package com.example.studkompas.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.adapter.CampusesListAdapter;
import com.example.studkompas.model.Campus;
import com.example.studkompas.utils.WindowInsetsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static List<Campus> Campuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowInsetsHelper.ApplySystemWindowInsets(this, R.id.rootLayout);

        Campuses = createCampusesList();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBuildings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CampusesListAdapter adapter = new CampusesListAdapter(this, Campuses);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.buttonHelp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
    }

    private ArrayList<Campus> createCampusesList() {
        ArrayList<Campus> campuses = new ArrayList<>();
        campuses.add(new Campus("ГУК", "guk", "ул. Мира, 19", Map.of(
                1, R.drawable.guk_1,
                2, R.drawable.guk_2,
                3, R.drawable.guk_3,
                4, R.drawable.guk_4,
                5, R.drawable.guk_5
        )));
        campuses.add(new Campus("Тургенева", "turgeneva", "ул. Тургенева, 4", Map.of(
                1, R.drawable.turgeneva_1,
                2, R.drawable.turgeneva_2,
                3, R.drawable.turgeneva_3,
                4, R.drawable.turgeneva_4,
                5, R.drawable.turgeneva_5,
                6, R.drawable.turgeneva_6
        )));
        campuses.add(new Campus("Куйбышева", "bio", "ул. Куйбышева, 48", Map.of(
                1, R.drawable.bio_1,
                2, R.drawable.bio_2,
                3, R.drawable.bio_3,
                4, R.drawable.bio_4,
                5, R.drawable.bio_5,
                6, R.drawable.bio_6,
                7, R.drawable.bio_7,
                8, R.drawable.bio_8
        )));
        campuses.add(new Campus("УГИ", "ugi", "ул. Ленина, 51", Map.of(
                0, R.drawable.lenina_0,
                1, R.drawable.lenina_1,
                2, R.drawable.lenina_2,
                3, R.drawable.lenina_3,
                4, R.drawable.lenina_4
        )));
        return campuses;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}