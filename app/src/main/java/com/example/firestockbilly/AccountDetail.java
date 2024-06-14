package com.example.firestockbilly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AccountDetail extends AppCompatActivity {

    private static final String PREFS_NAME = "UserAccountsPrefs";
    private static final String KEY_ACCOUNTS = "userAccounts";
    private static final int REQUEST_CREATE_ACCOUNT = 1;

    private TextView textViewAccountName;
    private LinearLayout containerRooms;
    private Button buttonAddNewRoom;

    private List<String> roomsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        textViewAccountName = findViewById(R.id.textViewAccountName);
        containerRooms = findViewById(R.id.containerRooms);
        buttonAddNewRoom = findViewById(R.id.buttonAddNewRoom);

        String accountName = getIntent().getStringExtra("accountName");
        textViewAccountName.setText(accountName);

        // Load user's rooms from SharedPreferences
        roomsList = loadUserRooms();
        populateRooms(roomsList);

        // Setup button to add new room
        buttonAddNewRoom.setOnClickListener(view -> {
            startActivityForResult(new Intent(AccountDetail.this, CreateAccount.class), REQUEST_CREATE_ACCOUNT);
        });

        // Show or hide add room button based on number of rooms
        updateAddRoomButtonVisibility();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_ACCOUNT && resultCode == RESULT_OK && data != null) {
            String roomName = data.getStringExtra("roomName");
            if (roomName != null) {
                roomsList.add(roomName);
                saveUserRooms(roomsList); // Save updated rooms list
                populateRooms(roomsList); // Update UI with new room

                // Update add room button visibility
                updateAddRoomButtonVisibility();
            }
        }
    }

    private List<String> loadUserRooms() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String serializedRooms = preferences.getString(KEY_ACCOUNTS, null);
        if (serializedRooms != null) {
            // Deserialize rooms list from SharedPreferences
            return deserializeRooms(serializedRooms);
        } else {
            return new ArrayList<>(); // Return empty list if no rooms found
        }
    }

    private void saveUserRooms(List<String> rooms) {
        // Serialize rooms list and save to SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ACCOUNTS, serializeRooms(rooms));
        editor.apply();
    }

    private List<String> deserializeRooms(String serializedRooms) {
        // Example method to deserialize rooms list from a string
        // Replace with actual deserialization logic based on your data model
        // Example implementation:
        // return Arrays.asList(serializedRooms.split(","));
        // This assumes rooms are serialized as comma-separated values
        // Replace with your actual deserialization logic
        return new ArrayList<>(); // Placeholder
    }

    private String serializeRooms(List<String> rooms) {
        // Example method to serialize rooms list to a string
        // Replace with actual serialization logic based on your data model
        // Example implementation:
        // StringBuilder sb = new StringBuilder();
        // for (String room : rooms) {
        //     sb.append(room).append(",");
        // }
        // return sb.toString();
        // This assumes rooms are serialized as comma-separated values
        // Replace with your actual serialization logic
        return ""; // Placeholder
    }

    private void populateRooms(List<String> rooms) {
        containerRooms.removeAllViews();

        for (String room : rooms) {
            Button button = new Button(this);
            button.setText(room);
            button.setOnClickListener(view -> {
                // Handle click on room button (if needed)
            });
            containerRooms.addView(button);
        }
    }

    private void updateAddRoomButtonVisibility() {
        if (roomsList.size() >= 20) {
            buttonAddNewRoom.setVisibility(View.GONE);
        } else {
            buttonAddNewRoom.setVisibility(View.VISIBLE);
        }
    }
}
