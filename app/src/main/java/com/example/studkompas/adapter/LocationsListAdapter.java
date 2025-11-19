package com.example.studkompas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.utils.GraphManager;

import java.util.List;

public class LocationsListAdapter extends RecyclerView.Adapter<LocationsListAdapter.ViewHolder> {
    private final OnLocationsSelectedListener listener;
    private List<GraphNode> nodes;

    public LocationsListAdapter(OnLocationsSelectedListener listener) {
        this.listener = listener;
    }

    public void updateList(List<GraphNode> newNodes) {
        this.nodes = newNodes;
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
        GraphNode node = nodes.get(position);
        holder.textView.setText(node.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(node);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public interface OnLocationsSelectedListener {
        void onLocationSelected(GraphNode node);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textLocationName);
        }
    }
}