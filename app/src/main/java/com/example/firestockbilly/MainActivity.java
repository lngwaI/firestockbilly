package com.example.firestockbilly;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisiere Firestore
        db = FirebaseFirestore.getInstance();

        // Beispiel-Methode aufrufen, um Daten hinzuzufügen
        addExampleData();
    }

    private void addExampleData() {
        // Erstelle einen Benutzer
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Max Mustermann");
        user.put("email", "max.mustermann@example.com");
        Toast.makeText(MainActivity.this, "D", Toast.LENGTH_SHORT).show();
        // Erstelle ein Konto
        Map<String, Object> account = new HashMap<>();
        account.put("name", "Hauptkonto");
        account.put("admin", "userID_here"); // Hier sollte die tatsächliche Benutzer-ID sein
        account.put("code", "abc123");
        List<String> members = new ArrayList<>();
        members.add("userID_here"); // Füge die tatsächliche Benutzer-ID hinzu
        account.put("members", members);
        List<String> entries = new ArrayList<>();
        entries.add("entryID_here"); // Füge die tatsächliche Eintrags-ID hinzu
        account.put("entries", entries);

        // Füge den Benutzer und das Konto zur Datenbank hinzu
        addUserData(user, account);
    }

    private void addUserData(Map<String, Object> user, Map<String, Object> account) {
        // Füge den Benutzer hinzu
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Benutzer erfolgreich hinzugefügt mit ID: " + documentReference.getId());

                        // Wenn der Benutzer erfolgreich hinzugefügt wurde, füge das Konto hinzu
                        addAccountData(documentReference.getId(), account);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Fehler beim Hinzufügen des Benutzers", e);
                    }
                });
    }

    private void addAccountData(String userId, Map<String, Object> account) {
        // Füge das Konto hinzu
        db.collection("accounts")
                .add(account)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Konto erfolgreich hinzugefügt mit ID: " + documentReference.getId());

                        // Hier kannst du weitere Aktionen ausführen, wenn das Konto erfolgreich hinzugefügt wurde
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Fehler beim Hinzufügen des Kontos", e);
                    }
                });
    }
}
