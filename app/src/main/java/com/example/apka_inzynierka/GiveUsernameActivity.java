package com.example.apka_inzynierka;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLDataException;

public class GiveUsernameActivity extends AppCompatActivity {

    DatabaseManager databaseManager;
    TextView error_text_view;
    EditText username_edit_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_username);

        databaseManager = new DatabaseManager(this);

        error_text_view = (TextView) findViewById(R.id.error_text_view);
        username_edit_text = (EditText) findViewById(R.id.username_edit_text);
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);

        if (sessionToken != null) {
            System.out.println("WYWOŁANO TO BEZ SENSU " + sessionToken);
            Intent intent = new Intent(this, GivePinActivity.class);
            intent.putExtra("usernameKey", sessionToken);
            startActivity(intent);
        }
    }


    public void clickConfirmButton(View v) throws SQLDataException {
        String username = String.valueOf(username_edit_text.getText());
        username = username.substring(0,1).toUpperCase() + username.substring(1).toLowerCase();
        if(username.length()==0){
            error_text_view.setVisibility(View.VISIBLE);
            error_text_view.setText("Nie podano nazwy użytkownika");
        }else if(username.contains(" ")){
            error_text_view.setVisibility(View.VISIBLE);
            error_text_view.setText("Nie używaj spacji.");
        }else {
            databaseManager.open();
            System.out.println(databaseManager.selectUserUsername(username));
            if (databaseManager.selectUserUsername(username) == 0) {
                Intent intent = new Intent(this, SetPinActivity.class);
                intent.putExtra("usernameKey", username);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, GivePinActivity.class);
                intent.putExtra("usernameKey", username);
                startActivity(intent);
                finish();
            }
            databaseManager.close();


        }
    }


}