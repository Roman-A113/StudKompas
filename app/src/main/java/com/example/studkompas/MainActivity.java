package com.example.studkompas;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Building> buildings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildings = new ArrayList<>();
        buildings.add(new Building("ГУК", "ул. Мира, 19"));
        buildings.add(new Building("Матмех", "ул. Тургенева, 4"));
        buildings.add(new Building("Биологический", "ул. Куйбышева, 48"));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBuildings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BuildingAdapter adapter = new BuildingAdapter(buildings);
        recyclerView.setAdapter(adapter);

        // Кнопки внизу
        findViewById(R.id.buttonHelp).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
        });

        findViewById(R.id.buttonSettings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    static class Building {
        String name;
        String address;

        Building(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.ViewHolder> {
        private List<Building> buildings;

        BuildingAdapter(List<Building> buildings) {
            this.buildings = buildings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_building, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Building building = buildings.get(position);
            holder.nameTextView.setText(building.name);
            holder.addressTextView.setText(building.address);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BuildingMapActivity.class);
                intent.putExtra("building_name", building.name);
                intent.putExtra("building_address", building.address);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return buildings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView addressTextView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.textViewBuildingName);
                addressTextView = itemView.findViewById(R.id.textViewBuildingAddress);
            }
        }
    }
}