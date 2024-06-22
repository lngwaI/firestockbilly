package com.example.firestockbilly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Overview extends AppCompatActivity {

    private static final String TAG = "BillyDebug - Overview";
    private RecyclerView recyclerView;
    private EntryAdapter entryAdapter;
    private List<Entry> entryList;
    private FirebaseFirestore db;
    private String accountId;
    private String accountName;
    private TextView accountNameTextView;
    private Button addEntryButton, manageUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        Log.d(TAG, "Overview.java gestartet!");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryList = new ArrayList<>();
        entryAdapter = new EntryAdapter(entryList, this::onEntryClick);
        recyclerView.setAdapter(entryAdapter);

        db = FirebaseFirestore.getInstance();
        accountId = getIntent().getStringExtra("accountId");
        accountName = getIntent().getStringExtra("accountName");

        accountNameTextView = findViewById(R.id.accountNameTextView);
        addEntryButton = findViewById(R.id.addEntryButton);
        manageUsers = findViewById(R.id.manageUserBTN);

        accountNameTextView.setText(accountName);
        addEntryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Overview.this, AccountDetail.class);
            intent.putExtra("accountId", accountId);
            startActivity(intent);
        });

        manageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(Overview.this, UserListActivity.class);
            intent.putExtra("accountId", accountId);
            startActivity(intent);
        });

        Log.d(TAG, "Current Account ID: " + accountId);

        loadEntriesFromFirestore();
    }

    private void loadEntriesFromFirestore() {
        db.collection("accounts").document(accountId).collection("entries").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                entryList.clear(); // Clear existing entries
                for (DocumentSnapshot document : task.getResult()) {
                    Entry entry = document.toObject(Entry.class);
                    if (entry != null) {
                        entryList.add(entry); // Add entry to list
                    }
                }
                entryAdapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Log.e(TAG, "Error loading entries", task.getException());
            }
        });
    }

    private void onEntryClick(int position) {
        Entry clickedEntry = entryList.get(position);
        Intent intent = new Intent(this, EntryDetailActivity.class);
        intent.putExtra("amount", clickedEntry.getAmount());
        intent.putExtra("category", clickedEntry.getCategory());
        intent.putExtra("categoryDetail", clickedEntry.getCategoryDetail());
        intent.putStringArrayListExtra("paidForUserIds", (ArrayList<String>) clickedEntry.getPaidForUserIds());
        startActivity(intent);
    }
}
