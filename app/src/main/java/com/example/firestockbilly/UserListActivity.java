package com.example.firestockbilly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserAdapter.RemoveUserClickListener {

    private static final String TAG = "BillyDebug - UserListActivity";
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String accountId;
    private boolean isAdmin = false; // Default Wert
    private Button addUserButton;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, isAdmin, this);
        userRecyclerView.setAdapter(userAdapter);

        accountId = getIntent().getStringExtra("accountId");

        addUserButton = findViewById(R.id.addUserButton);

        loadUsersFromFirestore();
    }

    private void loadUsersFromFirestore() {
        db.collection("accounts").document(accountId).collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            boolean userIsAdmin = false; // Default-Wert

                            // Überprüfen, ob das Feld "isAdmin" im Dokument vorhanden ist
                            if (document.contains("isAdmin") && document.getBoolean("isAdmin") != null) {
                                userIsAdmin = document.getBoolean("isAdmin");
                            }

                            // Direkte Abfrage nach displayName in der "users" Collection
                            boolean finalUserIsAdmin = userIsAdmin;
                            db.collection("users").document(userId).get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = userTask.getResult();
                                    if (userDocument.exists()) {
                                        String userName = userDocument.getString("displayName");

                                        if (userName == null || userName.isEmpty()) {
                                            userName = document.getString("userName");
                                        }

                                        User user = new User(userId, userName, finalUserIsAdmin);
                                        userList.add(user);

                                        // Prüfen, ob der aktuelle Benutzer ein Admin ist
                                        if (userId.equals(currentUserId)) {
                                            isAdmin = finalUserIsAdmin;
                                        }

                                        userAdapter.setAdmin(isAdmin);
                                        userAdapter.notifyDataSetChanged();
                                        updateUIForAdmin();
                                    } else {
                                        Log.e(TAG, "User document not found for userId: " + userId);
                                    }
                                } else {
                                    Log.e(TAG, "Error fetching user document for userId: " + userId, userTask.getException());
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Error loading users", task.getException());
                    }
                });
    }





    private void updateUIForAdmin() {
        if (isAdmin) {
            addUserButton.setVisibility(View.VISIBLE);
            addUserButton.setOnClickListener(v -> showAddUserDialog());
        } else {
            addUserButton.setVisibility(View.GONE);
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neuen Benutzer hinzufügen");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null, false);
        final EditText input = viewInflated.findViewById(R.id.input);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String userId = input.getText().toString();
            checkAndAddUser(userId);
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void checkAndAddUser(String userId) {
        db.collection("accounts").document(accountId).collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().exists()) {
                        addUserToAccount(userId);
                    } else {
                        Toast.makeText(UserListActivity.this, "Der Benutzer ist bereits Mitglied oder existiert nicht.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToAccount(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String userName = task.getResult().getString("displayName");
                if (userName != null) {
                    User newUser = new User(userId, userName, false);
                    db.collection("accounts").document(accountId).collection("users").document(userId).set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UserListActivity.this, "Benutzer hinzugefügt", Toast.LENGTH_SHORT).show();
                                loadUsersFromFirestore();
                            })
                            .addOnFailureListener(e -> Toast.makeText(UserListActivity.this, "Fehler beim Hinzufügen des Benutzers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(UserListActivity.this, "Benutzername konnte nicht abgerufen werden.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UserListActivity.this, "Der Benutzer existiert nicht.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRemoveUserClick(String userId) {
        showRemoveUserConfirmationDialog(userId);
    }

    private void showRemoveUserConfirmationDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Möchten Sie diesen Benutzer entfernen?")
                .setPositiveButton("Entfernen", (dialog, which) -> {
                    removeUserFromAccount(userId);
                })
                .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeUserFromAccount(String userId) {
        db.collection("accounts").document(accountId).collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserListActivity.this, "Nutzer entfernt", Toast.LENGTH_SHORT).show();
                    loadUsersFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserListActivity.this, "Fehler beim Entfernen des Nutzers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
