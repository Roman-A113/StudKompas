package com.example.studkompas.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studkompas.ui.CampusMapActivity;
import com.example.studkompas.R;
import com.example.studkompas.model.CampusItem;

import java.util.List;

public class CampusListAdapter extends RecyclerView.Adapter<CampusListAdapter.ViewHolder> {
    private final List<CampusItem> campusItems;
    private final Context context;

    public CampusListAdapter(Context context, List<CampusItem> campusItems) {
        this.campusItems = campusItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.campus_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampusItem campusItem = campusItems.get(position);
        holder.nameTextView.setText(campusItem.name);
        holder.addressTextView.setText(campusItem.address);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CampusMapActivity.class);
            intent.putExtra("campus_name", campusItem.name);
            intent.putExtra("campus_address", campusItem.address);
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