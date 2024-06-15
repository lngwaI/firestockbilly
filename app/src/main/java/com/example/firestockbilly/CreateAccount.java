package com.example.firestockbilly;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
                    addAccountToFirestore(currentUser.getUid(), accountName);
                } else {
                    Toast.makeText(CreateAccount.this, "Account name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateAccount.this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addAccountToFirestore(String userId, String accountName) {
        Map<String, Object> account = new HashMap<>();
        account.put("userId", userId);
        account.put("name", accountName);

        db.collection("accounts")
                .add(account)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateAccount.this, "Account added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Finish the activity and go back to the main screen
                })
                .addOnFailureListener(e -> Toast.makeText(CreateAccount.this, "Error adding account: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
