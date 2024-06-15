package com.example.firestockbilly;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private Button addUserButton;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        accountNameTextView = findViewById(R.id.accountNameTextView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        addUserButton = findViewById(R.id.addUserButton);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        String accountId = getIntent().getStringExtra("accountId");

        // Setzen Sie den LayoutManager für den RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);

        // Firestore Dokument abrufen
        db.collection("accounts").document(accountId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String accountName = document.getString("name");
                    accountNameTextView.setText(accountName);

                    String adminUserId = document.getString("userId");

                    // Überprüfen, ob der aktuelle Benutzer der Administrator ist und den Display-Namen anzeigen
                    if (currentUser != null && currentUser.getUid().equals(adminUserId)) {
                        String displayName = currentUser.getDisplayName();
                        displayNameTextView.setText(displayName + " [Admin]");
                    } else {
                        Log.e(TAG, "Ungültige Nutzer-ID für Administrator: " + adminUserId);
                    }

                    // Weitere Mitglieder (userIds) aus dem Firestore-Dokument abrufen und anzeigen
                    List<String> userIds = (List<String>) document.get("userIds");
                    if (userIds != null && !userIds.isEmpty()) {
                        displayUserNames(userIds);
                    } else {
                        Log.d(TAG, "Keine weiteren Mitglieder gefunden für Konto-ID: " + accountId);
                    }

                    addUserButton.setOnClickListener(v -> {
                        // Implementieren Sie den Dialog zur Hinzufügung eines Nutzers hier
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
        // Erstellen Sie den Adapter und setzen Sie ihn für den RecyclerView
        userAdapter = new UserAdapter(userIds);
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void showAddUserDialog() {
        // Implementieren Sie hier die Logik für den Dialog zur Hinzufügung eines Nutzers
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
                // Optional: Aktualisieren Sie die Ansicht oder führen Sie andere Aktionen aus, nachdem der Benutzer hinzugefügt wurde
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Fehler beim Hinzufügen der Benutzer-ID zum Konto: " + userIdToAdd, e);
                // Führen Sie Fehlerbehandlung aus, wenn das Hinzufügen fehlschlägt
            });
        } else {
            Log.e(TAG, "Konto-ID nicht übergeben.");
        }
    }
}
