package com.example.studkompas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;

import java.util.ArrayList;
import java.util.List;

public class FloorButtonsListAdapter extends RecyclerView.Adapter<FloorButtonsListAdapter.FloorButtonViewHolder> {
    private final List<Integer> floorNumbers;
    private final OnFloorSelectedListener listener;

    public FloorButtonsListAdapter(List<Integer> floorNumbers, OnFloorSelectedListener listener) {
        this.floorNumbers = new ArrayList<>(floorNumbers);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FloorButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor_button, parent, false);
        return new FloorButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorButtonViewHolder holder, int position) {
        int floor = floorNumbers.get(position);
        holder.bind(floor);
    }

    @Override
    public int getItemCount() {
        return floorNumbers.size();
    }

    public interface OnFloorSelectedListener {
        void onFloorSelected(int floorNumber);
    }

    class FloorButtonViewHolder extends RecyclerView.ViewHolder {
        private final Button floorButton;

        public FloorButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            floorButton = itemView.findViewById(R.id.floorButton);
        }

        public void bind(int floorNumber) {
            floorButton.setText(String.valueOf(floorNumber));
            floorButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFloorSelected(floorNumber);
                }
            });
        }
    }
}