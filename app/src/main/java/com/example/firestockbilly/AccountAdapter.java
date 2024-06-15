package com.example.firestockbilly;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountsList;
    private Context context;

    public AccountAdapter(Context context, List<Account> accountsList) {
        this.context = context;
        this.accountsList = accountsList;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_button, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountsList.get(position);
        holder.bind(account, context);
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

        public void bind(Account account, Context context) {
            accountButton.setText(account.getName());
            accountButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, AccountDetail.class);
                intent.putExtra("accountId", account.getId());
                intent.putExtra("accountName", account.getName());
                context.startActivity(intent);
            });
        }
    }
}
