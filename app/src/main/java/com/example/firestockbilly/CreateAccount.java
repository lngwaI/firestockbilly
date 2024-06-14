package com.example.firestockbilly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    private EditText editTextRoomName;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        editTextRoomName = findViewById(R.id.editTextAccountName);
        buttonSave = findViewById(R.id.buttonSaveAccount);

        buttonSave.setOnClickListener(view -> saveRoom());
    }

    private void saveRoom() {
        String roomName = editTextRoomName.getText().toString().trim();
        if (!roomName.isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra("roomName", roomName);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
