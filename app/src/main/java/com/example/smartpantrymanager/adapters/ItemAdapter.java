package com.example.smartpantrymanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Item;

import java.util.List;

/** Custom RecyclerView adapter that displays pantry items. */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(Item item);
        void onDelete(Item item);
    }

    private final List<Item> items;
    private final boolean showAlerts;
    private final Listener listener;

    public ItemAdapter(List<Item> items, boolean showAlerts, Listener listener) {
        this.items = items;
        this.showAlerts = showAlerts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Item item = items.get(position);
        h.tvName.setText(item.getName());
        h.tvQty.setText(Item.fmt(item.getQuantity()) + " " + item.getUnit());

        String exp = item.getExpiryDate();
        if (exp == null || exp.isEmpty()) {
            h.tvExpiry.setText("No expiry date");
            h.tvExpiry.setTextColor(Color.GRAY);
        } else {
            h.tvExpiry.setText("Expires: " + exp);
            boolean warn = showAlerts && item.isExpiringSoon(3);
            h.tvExpiry.setTextColor(warn ? Color.RED : Color.GRAY);
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQty, tvExpiry;
        final ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvQty = v.findViewById(R.id.tvQty);
            tvExpiry = v.findViewById(R.id.tvExpiry);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
