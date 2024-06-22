package com.example.firestockbilly;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "BillyDebug - MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView disName;
    private RecyclerView accountsRecyclerView;
    private AccountAdapter accountAdapter;
    private List<Account> accountsList;
    private Button buttonAddAccount, btnGetCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        disName = findViewById(R.id.tv_1);
        btnGetCode = findViewById(R.id.getCode);

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

        FirebaseUser currentUser = mAuth.getCurrentUser();

        disName.setText(currentUser.getDisplayName());

        btnGetCode.setOnClickListener(v -> {
            copyUserIdToClipboard(currentUser.getUid());
            Toast.makeText(MainActivity.this, "ID: " + currentUser.getUid() + " wurde erfolgreich in die Zwischenablage kopiert!", Toast.LENGTH_SHORT).show();
        });
    }

    private void copyUserIdToClipboard(String userId) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("UserId", userId);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //loadUserAccounts(currentUser.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }

    private void loadUserAccounts(String userId) {
        // Liste zur Speicherung aller Accounts
        List<Account> allAccounts = new ArrayList<>();
        // Set zur Speicherung einzigartiger Account-IDs
        Set<String> uniqueAccountIds = new HashSet<>();

        Log.d(TAG, "loadUserAccounts");

        // Abfrage für Accounts, bei denen der Benutzer der Ersteller ist
        db.collection("accounts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String accountId = document.getId();
                        String accountName = document.getString("name");
                        Account account = new Account(accountId, accountName);

                        // Log für hinzugefügten Admin-Account
                        Log.d(TAG, "Admin Account hinzugefügt: " + account.getId());

                        // Hinzufügen des Accounts zur Liste aller Accounts, wenn die Account-ID noch nicht existiert
                        if (!uniqueAccountIds.contains(accountId)) {
                            allAccounts.add(account);
                            uniqueAccountIds.add(accountId);
                        }
                    }

                    // Zusätzliche Abfrage für Accounts, bei denen der Benutzer Mitglied ist, aber kein Admin
                    db.collection("accounts")
                            .get()
                            .addOnSuccessListener(memberQuerySnapshot -> {
                                for (QueryDocumentSnapshot accountDoc : memberQuerySnapshot) {
                                    String accountId = accountDoc.getId();
                                    String accountName = accountDoc.getString("name");
                                    Account account = new Account(accountId, accountName);

                                    // Überprüfen, ob die Account-ID bereits als Admin hinzugefügt wurde
                                    if (uniqueAccountIds.contains(accountId)) {
                                        continue; // Überspringen, wenn die Account-ID bereits als Admin hinzugefügt wurde
                                    }

                                    // Überprüfen, ob der Benutzer in der Subcollection "users" dieses Accounts enthalten ist
                                    db.collection("accounts")
                                            .document(accountId)
                                            .collection("users")
                                            .document(userId)
                                            .get()
                                            .addOnCompleteListener(userTask -> {
                                                if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                                    // Account hinzufügen, wenn der Benutzer Mitglied ist und noch nicht als Admin hinzugefügt wurde
                                                    if (!uniqueAccountIds.contains(accountId)) {
                                                        allAccounts.add(account);
                                                        uniqueAccountIds.add(accountId);

                                                        // Log für hinzugefügten Mitglieds-Account
                                                        Log.d(TAG, "Member Account hinzugefügt: " + account.getId());

                                                        // Liste aktualisieren und RecyclerView aktualisieren
                                                        updateRecyclerView(allAccounts);
                                                    }
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error getting member accounts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    // Liste aktualisieren und RecyclerView aktualisieren
                    updateRecyclerView(allAccounts);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error getting admin accounts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecyclerView(List<Account> accounts) {
        // Sortieren der Accounts nach dem Namen
        Collections.sort(accounts, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));

        // Liste aktualisieren und RecyclerView aktualisieren
        accountsList.clear();
        accountsList.addAll(accounts);
        accountAdapter.notifyDataSetChanged();
    }










    private void initializeAccount(String accountId, String accountName, String creatorUserId, String creatorUserName) {
        // Account-Daten
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("name", accountName);
        accountData.put("userId", creatorUserId);

        // Erstellen des Accounts in Firestore
        db.collection("accounts").document(accountId).set(accountData).addOnSuccessListener(aVoid -> {
            Log.d("MainActivity", "Account successfully written!");

            // User-Daten für den Ersteller
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", creatorUserName);
            userData.put("isAdmin", true); // Annahme: Der Ersteller ist immer Admin

            // Hinzufügen des Erstellers zur "users"-Unterkollektion
            db.collection("accounts").document(accountId).collection("users").document(creatorUserId).set(userData).addOnSuccessListener(aVoid1 -> Log.d("MainActivity", "User successfully written!")).addOnFailureListener(e -> Log.w("MainActivity", "Error writing user", e));

        }).addOnFailureListener(e -> Log.w("MainActivity", "Error writing account", e));
    }
}
