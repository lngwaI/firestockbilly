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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDetail extends AppCompatActivity {

    private static final String TAG = "BillyDebug - AccountDetail";
    private final List<CheckBox> userCheckBoxes = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private TextView accountNameTextView;
    private TextView displayNameTextView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String accountId;
    private FirebaseUser currentUser;
    private Button addCategoryButton, confirmButton;
    private LinearLayout paymentForLinearLayout;
    private EditText amountEditText, categoryDetailEditText;
    private RadioGroup categoryRadioGroup;
    private List<String> userIds = new ArrayList<>();
    private String adminUserId;
    private String defaultUserId;

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
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        accountId = getIntent().getStringExtra("accountId");

        if (currentUser != null) {
            defaultUserId = currentUser.getUid();
            Log.d(TAG, "Current User ID: " + defaultUserId);
        } else {
            Log.e(TAG, "Kein Benutzer ist angemeldet");
            Toast.makeText(this, "Kein Benutzer ist angemeldet", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
                        Log.d(TAG, "User IDs: " + userIds);
                        // Überprüfe und füge fehlende Nutzer hinzu
                        checkAndAddMissingUsers();
                        // Zeige die Nutzernamen an
                        displayUserNames(userIds);
                    } else {
                        // Wenn keine weiteren Mitglieder gefunden wurden, füge den eingeloggten Nutzer hinzu
                        List<String> defaultUserList = new ArrayList<>();
                        defaultUserList.add(defaultUserId);
                        displayUserNames(defaultUserList);
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
        loadCategoriesFromFirestore();
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());
        setDefaultUserSelection(defaultUserId);
    }

    //TODO: Alle user ausgeben die in diesem raum sind! Ggf methoden "logik" verändern
    private void displayUserNames(List<String> userIds) {
        paymentForLinearLayout.removeAllViews();
        userCheckBoxes.clear();

        // "Alle" Checkbox hinzufügen
        CheckBox allCheckBox = new CheckBox(this);
        allCheckBox.setText("Alle");
        allCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : userCheckBoxes) {
                checkBox.setChecked(isChecked);
            }
        });
        paymentForLinearLayout.addView(allCheckBox);

        // Nutzer Checkboxen hinzufügen
        for (String userId : userIds) {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument.exists()) {
                        String displayName = userDocument.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            CheckBox userCheckBox = new CheckBox(this);
                            userCheckBox.setText(displayName);
                            if (userId.equals(defaultUserId)) {
                                userCheckBox.setChecked(true); // Der eingeloggte Nutzer ist standardmäßig ausgewählt
                            }
                            paymentForLinearLayout.addView(userCheckBox);
                            userCheckBoxes.add(userCheckBox);
                            Log.d(TAG, "Benutzer hinzugefügt: " + displayName);
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

    private void checkAndAddMissingUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> existingUserIds = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    existingUserIds.add(document.getId());
                }
                for (String userId : userIds) {
                    if (!existingUserIds.contains(userId)) {
                        // Füge den fehlenden Nutzer zur "users" Sammlung hinzu
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("displayName", "Benutzer " + userId);
                        db.collection("users").document(userId).set(newUser)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nutzer hinzugefügt: " + userId))
                                .addOnFailureListener(e -> Log.e(TAG, "Fehler beim Hinzufügen des Nutzers: " + userId, e));
                    }
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Nutzer", task.getException());
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kategorie hinzufügen");
        builder.setMessage("Geben Sie eine neue Kategorie ein:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String categoryToAdd = input.getText().toString().trim();
            if (!categoryToAdd.isEmpty()) {
                checkAndAddCategory(categoryToAdd);
            }
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void checkAndAddCategory(String categoryToAdd) {
        if (categories.contains(categoryToAdd)) {
            Toast.makeText(this, "Diese Kategorie existiert bereits.", Toast.LENGTH_SHORT).show();
        } else {
            // Überprüfen, ob die Kategorie bereits für das Konto existiert
            db.collection("accounts").document(accountId).collection("categories").whereEqualTo("name", categoryToAdd).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean categoryExists = !task.getResult().isEmpty();
                    if (!categoryExists) {
                        addCategoryToFirestore(categoryToAdd);
                    } else {
                        Toast.makeText(this, "Diese Kategorie existiert bereits für dieses Konto.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error checking category existence", task.getException());
                    Toast.makeText(this, "Fehler beim Überprüfen der Kategorie.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addCategoryToFirestore(String categoryToAdd) {
        // Kategorie zur Firestore-Datenbank hinzufügen
        String creatorUserId = defaultUserId;  // Benutzer-ID des Erstellers
        Category category = new Category(categoryToAdd, creatorUserId);
        db.collection("accounts").document(accountId).collection("categories").add(category).addOnSuccessListener(documentReference -> {
            categories.add(categoryToAdd);
            updateCategoryRadioGroup();
            Toast.makeText(AccountDetail.this, "Kategorie hinzugefügt", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding category", e);
            Toast.makeText(AccountDetail.this, "Fehler beim Hinzufügen der Kategorie", Toast.LENGTH_SHORT).show();
        });
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
        new AlertDialog.Builder(this).setTitle("Bestätigung").setMessage("Möchten Sie den Eintrag speichern?").setPositiveButton("Ja", (dialog, which) -> saveEntry()).setNegativeButton("Nein", null).show();
    }

    private void saveEntry() {
        String amount = amountEditText.getText().toString().trim();
        if (amount.isEmpty()) {
            Toast.makeText(this, "Bitte einen Betrag eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryDetail = categoryDetailEditText.getText().toString().trim();
        if (categoryDetail.isEmpty()) {
            categoryDetail = "ohne Spez.";
        }

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
        if (category.length() == 0) {
            Toast.makeText(this, "Bitte eine Kategorie auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedUserIds = new ArrayList<>();
        for (CheckBox checkBox : userCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedUserIds.add(checkBox.getText().toString());
            }
        }

        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "Bitte mindestens einen Benutzer auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        Entry entry = new Entry(amount, category.toString(), categoryDetail, currentUser.getDisplayName(), selectedUserIds);
        db.collection("accounts").document(accountId).collection("entries").add(entry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AccountDetail.this, "Eintrag gespeichert", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fehler beim Hinzufügen des Eintrags", e);
                    Toast.makeText(AccountDetail.this, "Fehler beim Hinzufügen des Eintrags", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCategoriesFromFirestore() {
        db.collection("accounts").document(accountId).collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categories.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String category = document.getString("name");
                    if (category != null) {
                        categories.add(category);
                    }
                }
                updateCategoryRadioGroup();
            } else {
                Log.e(TAG, "Error loading categories", task.getException());
            }
        });
    }

    private void setDefaultUserSelection(String defaultUserId) {
        Log.d(TAG, "Setting default user selection for User ID: " + defaultUserId);
        for (CheckBox checkBox : userCheckBoxes) {
            if (checkBox.getText().toString().equals(defaultUserId)) {
                checkBox.setChecked(true);
                Log.d(TAG, "Default user checkbox set for: " + defaultUserId);
                break;
            }
        }
    }
}
