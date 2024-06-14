package com.example.firestockbilly;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<String> accountsList;
    private OnAccountItemClickListener clickListener;

    public AccountAdapter(List<String> accountsList, OnAccountItemClickListener clickListener) {
        this.accountsList = accountsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_button, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        String accountName = accountsList.get(position);
        holder.bind(accountName, clickListener);
    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {

        private Button accountButton;

        public AccountViewHolder(View itemView) {
            super(itemView);
            accountButton = itemView.findViewById(R.id.accountButton);
        }

        public void bind(String accountName, OnAccountItemClickListener clickListener) {
            accountButton.setText(accountName);
            accountButton.setOnClickListener(v -> clickListener.onItemClick(accountName));
        }
    }

    public interface OnAccountItemClickListener {
        void onItemClick(String accountName);
    }
}
