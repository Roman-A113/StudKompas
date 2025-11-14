// com/example/studkompas/adapter/LocationAdapter.java

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

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private List<String> locations = new ArrayList<>();
    private LocationItemListener listener;

    public LocationAdapter(LocationItemListener listener) {
        this.listener = listener;
    }

    public void updateList(List<String> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textLocationName);
        }
    }
}