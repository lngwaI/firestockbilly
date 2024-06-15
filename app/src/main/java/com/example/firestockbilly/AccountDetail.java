package com.example.firestockbilly;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AccountDetail extends AppCompatActivity {

    private static final String TAG = "AccountDetail";
    private TextView accountNameTextView;
    private TextView displayNameTextView;
    private FirebaseFirestore db;
    private Button addUserButton;
    private RecyclerView usersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        accountNameTextView = findViewById(R.id.accountNameTextView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        addUserButton = findViewById(R.id.addUserButton);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        db = FirebaseFirestore.getInstance();

        String accountId = getIntent().getStringExtra("accountId");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);

        db.collection("accounts").document(accountId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String accountName = document.getString("name");
                    accountNameTextView.setText(accountName);

                    String adminUserId = document.getString("userId");

                    db.collection("users").document(adminUserId).get().addOnCompleteListener(adminTask -> {
                        if (adminTask.isSuccessful()) {
                            DocumentSnapshot adminDocument = adminTask.getResult();
                            if (adminDocument.exists()) {
                                Log.d(TAG, "Admin Document Data: " + adminDocument.getData());

                                String adminDisplayName = adminDocument.getString("displayname");
                                if (adminDisplayName != null && !adminDisplayName.isEmpty()) {
                                    displayNameTextView.setText(adminDisplayName + " [Admin]");
                                } else {
                                    Log.e(TAG, "Admin DisplayName ist leer oder null");
                                    displayNameTextView.setText("Admin Name nicht gefunden [Admin]");
                                }
                            } else {
                                Log.d(TAG, "Admin-Nutzer-Dokument nicht gefunden für ID: " + adminUserId);
                                displayNameTextView.setText("Admin Name nicht gefunden [Admin]");
                            }
                        } else {
                            Log.e(TAG, "Fehler beim Abrufen des Admin-Nutzer-Dokuments: ", adminTask.getException());
                            displayNameTextView.setText("Fehler beim Abrufen des Admin-Namens");
                        }
                    });

                    List<String> userIds = (List<String>) document.get("userIds");
                    if (userIds != null && !userIds.isEmpty()) {
                        displayUserNames(userIds);
                    } else {
                        Log.d(TAG, "Keine weiteren Mitglieder gefunden für Konto-ID: " + accountId);
                    }

                    addUserButton.setOnClickListener(v -> {
                        showAddUserDialog();
                    });

                } else {
                    Log.d(TAG, "Dokument nicht gefunden für Konto-ID: " + accountId);
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen des Kontos: ", task.getException());
            }
        });
    }

    private void displayUserNames(List<String> userIds) {
        for (String userId : userIds) {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument.exists()) {
                        String displayName = userDocument.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            displayNameTextView.append("\n" + displayName);
                        } else {
                            Log.e(TAG, "DisplayName ist leer oder null für userId: " + userId);
                        }
                    } else {
                        Log.d(TAG, "User Document not found for userId: " + userId);
                    }
                } else {
                    Log.e(TAG, "Error getting user document for userId: " + userId, task.getException());
                }
            });
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add User");
        builder.setMessage("Enter user ID:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userIdToAdd = input.getText().toString().trim();
            addUserIdToAccount(userIdToAdd);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addUserIdToAccount(String userIdToAdd) {
        String accountId = getIntent().getStringExtra("accountId");
        if (accountId != null) {
            db.collection("accounts").document(accountId).update("userIds", FieldValue.arrayUnion(userIdToAdd)).addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Benutzer-ID erfolgreich zum Konto hinzugefügt: " + userIdToAdd);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Fehler beim Hinzufügen der Benutzer-ID zum Konto: " + userIdToAdd, e);
            });
        } else {
            Log.e(TAG, "Konto-ID nicht übergeben.");
        }
    }
}
