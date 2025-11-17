package com.example.studkompas.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.R;
import com.example.studkompas.model.Campus;
import com.example.studkompas.ui.CampusMapActivity;

import java.util.List;

public class CampusesListAdapter extends RecyclerView.Adapter<CampusesListAdapter.ViewHolder> {
    private final List<Campus> campusItems;
    private final Context context;

    public CampusesListAdapter(Context context, List<Campus> campusItems) {
        this.campusItems = campusItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campus_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campus campus = campusItems.get(position);
        holder.nameTextView.setText(campus.Name);
        holder.addressTextView.setText(campus.Address);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CampusMapActivity.class);
            intent.putExtra("campus", campus);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return campusItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewCampusName);
            addressTextView = itemView.findViewById(R.id.textViewCampusAddress);
        }
    }
}
