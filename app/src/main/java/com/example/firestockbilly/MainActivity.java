package com.example.firestockbilly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView accountsRecyclerView;
    private AccountAdapter accountAdapter;
    private List<Account> accountsList;
    private Button buttonAddAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        accountsRecyclerView = findViewById(R.id.accountsRecyclerView);
        accountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        accountsList = new ArrayList<>();
        accountAdapter = new AccountAdapter(this, accountsList);
        accountsRecyclerView.setAdapter(accountAdapter);

        buttonAddAccount = findViewById(R.id.buttonAddAccount);
        buttonAddAccount.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });

        if (mAuth.getCurrentUser() != null) {
            loadUserAccounts(mAuth.getCurrentUser().getUid());
        } else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserAccounts(currentUser.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }

    private void loadUserAccounts(String userId) {
        db.collection("accounts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<Account> uniqueAccounts = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String accountId = document.getId();
                            String accountName = document.getString("name");
                            uniqueAccounts.add(new Account(accountId, accountName));
                        }

                        // Zweite Abfrage: Konten, bei denen der Benutzer Mitglied ist
                        db.collection("accounts")
                                .whereArrayContains("userIds", userId)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task2.getResult()) {
                                            String accountId = document.getId();
                                            String accountName = document.getString("name");
                                            uniqueAccounts.add(new Account(accountId, accountName));
                                        }

                                        accountsList.clear();
                                        accountsList.addAll(uniqueAccounts);
                                        accountAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error getting member accounts: " + task2.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Error getting admin accounts: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
