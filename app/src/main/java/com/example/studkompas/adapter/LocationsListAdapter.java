package com.example.studkompas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.model.GraphNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocationsListAdapter extends RecyclerView.Adapter<LocationsListAdapter.ViewHolder> {
    private final OnLocationsSelectedListener listener;
    private List<GraphNode> nodes;
    private List<GraphNode> filteredNodes;

    public LocationsListAdapter(OnLocationsSelectedListener listener) {
        this.listener = listener;
    }

    public void updateList(List<GraphNode> newNodes) {
        this.nodes = newNodes;
        this.nodes.sort(Comparator.comparing(node -> node.name));
        this.filteredNodes = new ArrayList<>(newNodes);
    }

    public void filter(String query) {
        filteredNodes.clear();
        if (query.isEmpty()) {
            filteredNodes.addAll(nodes);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (GraphNode node : nodes) {
                if (node.name != null && node.name.toLowerCase().contains(lowerQuery)) {
                    filteredNodes.add(node);
                }
            }
        }
        notifyDataSetChanged();
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
        GraphNode node = filteredNodes.get(position);
        holder.textView.setText(node.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(node);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredNodes.size();
    }

    public interface OnLocationsSelectedListener {
        void onLocationSelected(GraphNode node);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textLocationName);
        }
    }
}