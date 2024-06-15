package com.example.firestockbilly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, displayNameEditText;
    private Button registerButton, goToLoginButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        emailEditText = findViewById(R.id.registration_et_email);
        passwordEditText = findViewById(R.id.registration_et_passwort);
        displayNameEditText = findViewById(R.id.registration_et_display);
        registerButton = findViewById(R.id.registration_btn_register);
        goToLoginButton = findViewById(R.id.registration_tv_goto_login);
        progressBar = findViewById(R.id.registration_pb_progressBar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                final String displayName = displayNameEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required.");
                    return;
                }

                if (password.length() < 6) {
                    passwordEditText.setError("Password must be >= 6 characters.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Setze den Display-Namen des Benutzers
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName)
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> profileTask) {
                                                    if (profileTask.isSuccessful()) {
                                                        // Speichere zusätzliche Benutzerdaten in Firestore
                                                        saveUserData(user.getUid(), displayName);

                                                        Toast.makeText(Registration.this, "User Registered.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                        finish(); // Optional: Schließe die Registration Activity
                                                    } else {
                                                        Toast.makeText(Registration.this, "Failed to set display name.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Registration.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }

    private void saveUserData(String userId, String displayName) {
        // Beispiel für das Speichern von Benutzerdaten in Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("displayName", displayName);

        db.collection("users")
                .document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Erfolgreich gespeichert
                    Toast.makeText(Registration.this, "User data saved in Firestore.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Fehler beim Speichern
                    Toast.makeText(Registration.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
