package com.example.studkompas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;

import java.util.ArrayList;
import java.util.List;

public class LocationsListAdapter extends RecyclerView.Adapter<LocationsListAdapter.ViewHolder> {

    private final OnLocationsSelectedListener listener;
    private List<String> locations = new ArrayList<>();

    public LocationsListAdapter(OnLocationsSelectedListener listener) {
        this.listener = listener;
    }

    public void updateList(List<String> newLocations) {
        this.locations = newLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_location_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = locations.get(position);
        holder.textView.setText(name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public interface OnLocationsSelectedListener {
        void onLocationSelected(String locationName);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textLocationName);
        }
    }
}