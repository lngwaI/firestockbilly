package com.example.firestockbilly;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EntryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        // Extrahiere die Daten aus dem Intent
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String category = getIntent().getStringExtra("category");
        String categoryDetail = getIntent().getStringExtra("categoryDetail");

        // Finde die TextViews im Layout
        TextView amountTextView = findViewById(R.id.amountTextView);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        TextView categoryDetailTextView = findViewById(R.id.categoryDetailTextView);

        // Setze die Werte in die TextViews
        amountTextView.setText(String.valueOf(amount));
        categoryTextView.setText(category);
        categoryDetailTextView.setText(categoryDetail);
    }
}
