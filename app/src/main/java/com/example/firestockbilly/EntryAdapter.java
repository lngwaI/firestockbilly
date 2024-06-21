package com.example.firestockbilly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private final List<Entry> entryList;
    private final OnItemClickListener listener;

    public EntryAdapter(List<Entry> entryList, OnItemClickListener listener) {
        this.entryList = entryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        float c = Float.parseFloat(entry.getAmount());
        holder.amountTextView.setText(String.format("%.2f €", c));
        holder.categoryTextView.setText(entry.getCategory());
        holder.paidByTextView.setText(entry.getPaidBy());
        holder.paidForTextView.setText(String.join(", ", entry.getPaidForUserIds()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {

        public TextView amountTextView, categoryTextView, paidForTextView, paidByTextView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            paidByTextView = itemView.findViewById(R.id.paidByTextView);
            paidForTextView = itemView.findViewById(R.id.paidForTextView);
        }
    }
}
