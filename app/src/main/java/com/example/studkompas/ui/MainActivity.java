package com.example.studkompas.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.adapter.CampusListAdapter;
import com.example.studkompas.model.Campus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static List<Campus> Campuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Campuses = createCampusesList();

        copyAssetGraphToFile();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBuildings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CampusListAdapter adapter = new CampusListAdapter(this, Campuses);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.buttonHelp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
        findViewById(R.id.buttonSettings).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void copyAssetGraphToFile() {
        File graphFile = new File(getFilesDir(), "graph.json");
        if (!graphFile.exists()) {
            try {
                InputStream inputStream = getAssets().open("graph.json");
                File outFile = new File(getFilesDir(), "graph.json");
                OutputStream outputStream = new FileOutputStream(outFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ArrayList<Campus> createCampusesList() {
        ArrayList<Campus> campuses = new ArrayList<>();
        campuses.add(new Campus("ГУК", "ул. Мира, 19", Map.of(
                1, R.drawable.guk_1,
                2, R.drawable.guk_2,
                3, R.drawable.guk_3,
                4, R.drawable.guk_4,
                5, R.drawable.guk_5
        )));
        campuses.add(new Campus("Матмех", "ул. Тургенева, 4", Map.of(
                1, R.drawable.turgeneva_1,
                2, R.drawable.turgeneva_2,
                3, R.drawable.turgeneva_3,
                4, R.drawable.turgeneva_4,
                5, R.drawable.turgeneva_5,
                6, R.drawable.turgeneva_6
        )));

        campuses.add(new Campus("Биологический", "ул. Куйбышева, 48", Map.of(
                1, R.drawable.bio_1,
                2, R.drawable.bio_2,
                3, R.drawable.bio_3
        )));
        return campuses;
    }
}