package com.example.apka_inzynierka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLDataException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTargetActivity extends AppCompatActivity {

    EditText endTargetDateEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        ImageView calendarImageView = findViewById(R.id.calendarImageView);
        endTargetDateEditText = findViewById(R.id.date_edit_text);
        calendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            endTargetDateEditText.setText(selectedDate);
            Toast.makeText(AddTargetActivity.this, "Wybrano datę: " + selectedDate, Toast.LENGTH_SHORT).show();
        }
    };

    public void addNewTarget(View view) {
        TextView usernameTextView = findViewById(R.id.username_text_view);
        EditText targetNameEditView = findViewById(R.id.target_name_edit_text);
        EditText targetAmountEditText = findViewById(R.id.target_amount_edit_text);
        EditText dateEditText = findViewById(R.id.date_edit_text);
        if(targetNameEditView.getText().length() == 0 || targetAmountEditText.getText().length() == 0 ||  dateEditText.getText().length() == 0){
            Toast.makeText(getApplicationContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
        }else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            DatabaseManager databaseManager = new DatabaseManager(this);
            try {
                Date endTargetDate = dateFormat.parse(String.valueOf(endTargetDateEditText.getText()));
                databaseManager.open();
                databaseManager.insertTarget(String.valueOf(targetNameEditView.getText()), String.valueOf(usernameTextView.getText()), Double.valueOf(String.valueOf(targetAmountEditText.getText())), endTargetDate);
                databaseManager.close();
            } catch (ParseException e) {
                e.printStackTrace();
            }catch (SQLDataException e) {
                throw new RuntimeException(e);
            }

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }

    }
}