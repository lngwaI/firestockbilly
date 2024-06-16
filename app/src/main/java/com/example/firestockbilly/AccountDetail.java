package com.example.firestockbilly;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AccountDetail extends AppCompatActivity {

    private static final String TAG = "AccountDetail";
    private TextView accountNameTextView;
    private TextView displayNameTextView;
    private FirebaseFirestore db;
    private Button addUserButton, addCategoryButton, confirmButton;
    private LinearLayout paymentForLinearLayout;
    private EditText amountEditText, categoryDetailEditText;
    private RadioGroup categoryRadioGroup;
    private List<String> userIds = new ArrayList<>();
    private List<CheckBox> userCheckBoxes = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private String adminUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        accountNameTextView = findViewById(R.id.accountNameTextView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        amountEditText = findViewById(R.id.amountEditText);
        categoryDetailEditText = findViewById(R.id.categoryDetailEditText);
        categoryRadioGroup = findViewById(R.id.categoryRadioGroup);
        paymentForLinearLayout = findViewById(R.id.paymentForLinearLayout);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        confirmButton = findViewById(R.id.confirmButton);

        db = FirebaseFirestore.getInstance();

        String accountId = getIntent().getStringExtra("accountId");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        db.collection("accounts").document(accountId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String accountName = document.getString("name");
                    accountNameTextView.setText(accountName);

                    adminUserId = document.getString("userId");

                    db.collection("users").document(adminUserId).get().addOnCompleteListener(adminTask -> {
                        if (adminTask.isSuccessful()) {
                            DocumentSnapshot adminDocument = adminTask.getResult();
                            if (adminDocument.exists()) {
                                String adminDisplayName = adminDocument.getString("displayName");
                                if (adminDisplayName != null && !adminDisplayName.isEmpty()) {
                                    displayNameTextView.setText(adminDisplayName + " [Admin]");
                                } else {
                                    displayNameTextView.setText("Admin Name nicht gefunden [Admin]");
                                }
                            } else {
                                displayNameTextView.setText("Admin Name nicht gefunden [Admin]");
                            }
                        } else {
                            displayNameTextView.setText("Fehler beim Abrufen des Admin-Namens");
                        }
                    });

                    userIds = (List<String>) document.get("userIds");
                    if (userIds != null && !userIds.isEmpty()) {
                        displayUserNames(userIds);
                    } else {
                        Log.d(TAG, "Keine weiteren Mitglieder gefunden für Konto-ID: " + accountId);
                    }

                    confirmButton.setOnClickListener(v -> showConfirmationDialog());

                } else {
                    Log.d(TAG, "Dokument nicht gefunden für Konto-ID: " + accountId);
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen des Kontos: ", task.getException());
            }
        });

        // Initialize with some default categories
        categories.add("Einkaufen");
        categories.add("Taxi");
        categories.add("Haushalt");
        updateCategoryRadioGroup();
    }

    private void displayUserNames(List<String> userIds) {
        paymentForLinearLayout.removeAllViews();

        CheckBox allCheckBox = new CheckBox(this);
        //allCheckBox.setId(R.id.paymentForAllRadioButton);
        allCheckBox.setText("Alle");
        allCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : userCheckBoxes) {
                checkBox.setChecked(isChecked);
            }
        });
        paymentForLinearLayout.addView(allCheckBox);

        for (String userId : userIds) {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument.exists()) {
                        String displayName = userDocument.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            CheckBox userCheckBox = new CheckBox(this);
                            userCheckBox.setText(displayName);
                            paymentForLinearLayout.addView(userCheckBox);
                            userCheckBoxes.add(userCheckBox);
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

    private void showAddCategoryDialog() {
        if (categories.size() >= 10) {
            new AlertDialog.Builder(this)
                    .setTitle("Kategorie hinzufügen")
                    .setMessage("Maximale Anzahl von Kategorien erreicht")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kategorie hinzufügen");
        builder.setMessage("Geben Sie eine neue Kategorie ein:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String categoryToAdd = input.getText().toString().trim();
            if (!categoryToAdd.isEmpty()) {
                categories.add(categoryToAdd);
                updateCategoryRadioGroup();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateCategoryRadioGroup() {
        categoryRadioGroup.removeAllViews();
        for (String category : categories) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(category);
            categoryRadioGroup.addView(radioButton);
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Bestätigung")
                .setMessage("Möchten Sie den Eintrag speichern?")
                .setPositiveButton("Ja", (dialog, which) -> saveEntry())
                .setNegativeButton("Nein", null)
                .show();
    }

    private void saveEntry() {
        String amount = amountEditText.getText().toString().trim();
        String categoryDetail = categoryDetailEditText.getText().toString().trim();
        StringBuilder category = new StringBuilder();
        for (int i = 0; i < categoryRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) categoryRadioGroup.getChildAt(i);
            if (radioButton.isChecked()) {
                if (category.length() > 0) {
                    category.append(", ");
                }
                category.append(radioButton.getText().toString());
            }
        }

        List<String> paidForUserIds = new ArrayList<>();
        if (paymentForLinearLayout.getChildAt(0) instanceof CheckBox) {
            CheckBox allCheckBox = (CheckBox) paymentForLinearLayout.getChildAt(0);
            if (allCheckBox.isChecked()) {
                paidForUserIds.addAll(userIds);
            } else {
                for (CheckBox checkBox : userCheckBoxes) {
                    if (checkBox.isChecked() && !checkBox.getText().toString().equals("Alle")) {
                        paidForUserIds.add(userIds.get(userCheckBoxes.indexOf(checkBox)));
                    }
                }
            }
        }

        String accountId = getIntent().getStringExtra("accountId");
        if (accountId != null) {
            Entry entry = new Entry(amount, category.toString(), categoryDetail, paidForUserIds);
            db.collection("accounts").document(accountId).collection("entries").add(entry).addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Eintrag erfolgreich hinzugefügt: " + documentReference.getId());
                Toast.makeText(this, "Eintrag erfolgreich hinzugefügt", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Fehler beim Hinzufügen des Eintrags: ", e);
                Toast.makeText(this, "Fehler beim Hinzufügen des Eintrags", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Konto-ID nicht übergeben.");
        }
    }
}
