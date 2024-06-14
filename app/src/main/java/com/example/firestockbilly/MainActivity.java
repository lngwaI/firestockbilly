package com.example.firestockbilly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AccountAdapter.OnAccountItemClickListener {

    private static final int REQUEST_CREATE_ACCOUNT = 1;
    private RecyclerView accountsRecyclerView;
    private AccountAdapter accountAdapter;
    private List<String> accountsList;

    // ActivityResultLauncher for starting CreateAccountActivity
    private final ActivityResultLauncher<Intent> createAccountLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("accountName")) {
                        String accountName = data.getStringExtra("accountName");
                        accountsList.add(accountName);
                        accountAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Neues Konto erstellt: " + accountName, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Edge-to-Edge library setup
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accountsList = new ArrayList<>();
        accountsList.add("Konto 1");
        accountsList.add("Konto 2");
        accountsList.add("Konto 3");
        // Add more accounts as needed

        accountsRecyclerView = findViewById(R.id.accountsRecyclerView);
        accountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        accountAdapter = new AccountAdapter(accountsList, this);
        accountsRecyclerView.setAdapter(accountAdapter);

        // Find and set click listener for "Neues Konto hinzufügen" button
        findViewById(R.id.buttonAddAccount).setOnClickListener(v -> onAddAccountClicked());
    }

    // Method to handle click on "Neues Konto hinzufügen" button
    private void onAddAccountClicked() {
        Intent intent = new Intent(this, CreateAccount.class);
        createAccountLauncher.launch(intent);
    }



    // Handle click on account item
    @Override
    public void onItemClick(String accountName) {
        Intent intent = new Intent(this, AccountDetail.class);
        intent.putExtra("accountName", accountName);
        startActivity(intent);
    }
}
