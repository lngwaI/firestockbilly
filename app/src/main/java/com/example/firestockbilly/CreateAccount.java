package com.example.firestockbilly;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText accountNameEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        accountNameEditText = findViewById(R.id.editTextAccountName);
        saveButton = findViewById(R.id.buttonSaveAccount);

        saveButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String accountName = accountNameEditText.getText().toString().trim();
                if (!accountName.isEmpty()) {
                    addAccountToFirestore(currentUser.getUid(), accountName, currentUser.getDisplayName());
                } else {
                    Toast.makeText(CreateAccount.this, "Account name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateAccount.this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addAccountToFirestore(String userId, String accountName, String creatorUserName) {
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("userId", userId);
        accountData.put("name", accountName);

        // Add account to Firestore
        db.collection("accounts")
                .add(accountData)
                .addOnSuccessListener(documentReference -> {
                    String accountId = documentReference.getId();

                    // Initialize account users collection with the creator as admin
                    initializeAccountUsers(accountId, userId, creatorUserName);

                    Toast.makeText(CreateAccount.this, "Account added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Finish the activity and go back to the main screen
                })
                .addOnFailureListener(e -> Toast.makeText(CreateAccount.this, "Error adding account: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void initializeAccountUsers(String accountId, String userId, String creatorUserName) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("userName", creatorUserName);
        userData.put("isAdmin", true); // Assuming creator is admin

        // Add user to account's users collection
        db.collection("accounts").document(accountId).collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateAccount.this, "User added to account users", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateAccount.this, "Error adding user to account users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
