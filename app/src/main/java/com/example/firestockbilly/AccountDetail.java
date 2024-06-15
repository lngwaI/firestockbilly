package com.example.firestockbilly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AccountDetail extends AppCompatActivity {

    private static final String TAG = "AccountDetail";
    private TextView accountNameTextView;
    private TextView displayNameTextView;
    private Button addUserButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        accountNameTextView = findViewById(R.id.accountNameTextView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        addUserButton = findViewById(R.id.addUserButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Beispiel für das Abrufen der Konto-ID (z.B. durch Klick auf ein Element in der Liste)
        accountId = getIntent().getStringExtra("accountId");

        // Button Klick Listener
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserToAccount();
            }
        });

        // Firestore Dokument abrufen
        db.collection("accounts").document(accountId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String accountName = document.getString("name");
                    accountNameTextView.setText(accountName);

                    // Nutzer-ID aus dem Firestore-Dokument abrufen
                    String userId = document.getString("userId");

                    // Display-Namen des Nutzers aus Firebase Authentication abrufen
                    if (currentUser != null && currentUser.getUid().equals(userId)) {
                        String displayName = currentUser.getDisplayName();
                        displayNameTextView.setText(displayName);
                    } else {
                        Log.e(TAG, "Ungültige Nutzer-ID: " + userId);
                    }
                } else {
                    Log.d(TAG, "Dokument nicht gefunden für Konto-ID: " + accountId);
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen des Kontos: ", task.getException());
            }
        });
    }

    // Methode zum Hinzufügen eines Benutzers zu einem Konto
    private void addUserToAccount() {
        if (currentUser != null) {
            // Erstelle einen AlertDialog für die Nutzer-ID-Eingabe
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Benutzer hinzufügen");

            // Setze ein EditText-Feld im Dialog für die Nutzer-ID
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Setze die Buttons im Dialog
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userId = input.getText().toString().trim();
                    addUserIdToAccount(userId);
                }
            });

            builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Zeige den AlertDialog an
            builder.show();
        } else {
            Toast.makeText(this, "Nutzer nicht angemeldet.", Toast.LENGTH_SHORT).show();
        }
    }


    // Beispiel-Methode zum Hinzufügen einer Nutzer-ID zu einem Konto in Firestore
    private void addUserIdToAccount(String userId) {
        db.collection("accounts").document(accountId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> userIds = (List<String>) document.get("userIds");
                    if (userIds == null) {
                        userIds = new ArrayList<>();
                    }
                    if (!userIds.contains(userId)) {
                        userIds.add(userId);
                        db.collection("accounts").document(accountId).update("userIds", userIds)
                                .addOnSuccessListener(aVoid -> Toast.makeText(AccountDetail.this, "Benutzer hinzugefügt.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Log.e(TAG, "Fehler beim Hinzufügen des Benutzers", e));
                    } else {
                        Toast.makeText(AccountDetail.this, "Benutzer bereits hinzugefügt.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Dokument nicht gefunden für Konto-ID: " + accountId);
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen des Kontos: ", task.getException());
            }
        });
    }


}
