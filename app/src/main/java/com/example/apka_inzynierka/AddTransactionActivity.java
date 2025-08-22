package com.example.apka_inzynierka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.sql.SQLDataException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    TextView selectedCategoryTextView;
    EditText dateEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        selectedCategoryTextView = findViewById(R.id.choose_category_text_view);
        Chip outcomeChip = findViewById(R.id.outcome_chip);
        LinearLayout layoutMultibox = findViewById(R.id.container_multibox);
        ScrollView scrollviewMultibox = findViewById(R.id.scroll_view_multibox);
        Button addTransactionButton = findViewById(R.id.add_transaction_button);
        dateEditText = findViewById(R.id.date_edit_text);
        List<String> categories = Arrays.asList("Edukacja", "Elektronika", "Prezenty", "Rachunki", "Rozrywka", "Samochód", "Transport", "Trening", "Wypoczynek", "Zakupy", "Zdrowie");


        ImageView calendarImageView = findViewById(R.id.calendarImageView);
        calendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });


        ArrayList<TextView> textviewsList = new ArrayList<>();
        for(int i = 0; i < categories.size(); i++){
            TextView textView =  new TextView(this);
            textView.setTextSize(18);
            textView.setText(categories.get(i));

            layoutMultibox.addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedCategoryTextView.setText(textView.getText());
                    selectedCategoryTextView.setBackgroundResource(R.drawable.spinner_bg);
                    scrollviewMultibox.setVisibility(View.INVISIBLE);
                }
            });
            textviewsList.add(textView);
        }

        outcomeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!outcomeChip.isChecked()) outcomeChip.setChecked(true);
                selectedCategoryTextView.setText("");
            }
        });

        Chip incomeChip = findViewById(R.id.income_chip);
        incomeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!incomeChip.isChecked()) incomeChip.setChecked(true);
                selectedCategoryTextView.setText("Wpłata");
                selectedCategoryTextView.setBackgroundResource(R.drawable.spinner_bg);
                scrollviewMultibox.setVisibility(View.INVISIBLE);
            }
        });

        selectedCategoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(outcomeChip.isChecked()){
                    if(scrollviewMultibox.getVisibility() == View.VISIBLE){
                        selectedCategoryTextView.setBackgroundResource(R.drawable.spinner_bg);
                        scrollviewMultibox.setVisibility(View.INVISIBLE);
                    }else{
                        scrollviewMultibox.setVisibility(View.VISIBLE);
                        selectedCategoryTextView.setBackgroundResource(R.drawable.spinner_bg_up);

                    }
                }
            }
        });

        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText transactionDescriptionEditText= findViewById(R.id.transaction_decription_edit_text);
                EditText dateEditText = findViewById(R.id.date_edit_text);
                EditText transactionAmountEditText= findViewById(R.id.transaction_amount_edit_text);
                EditText transactionNameEditText= findViewById(R.id.transaction_name_edit_text);
                TextView usernameTextView = findViewById(R.id.username_text_view);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                if(transactionNameEditText.getText().length() == 0 || transactionAmountEditText.getText().length() == 0 ||  dateEditText.getText().length() == 0 || selectedCategoryTextView.getText().length() == 0) {
                       Toast.makeText(getApplicationContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                }else {
                    DatabaseManager databaseManager = new DatabaseManager(AddTransactionActivity.this);
                    try {
                        Date endTargetDate = dateFormat.parse(String.valueOf(dateEditText.getText()));
                        databaseManager.open();
                        if(databaseManager.getBalance(String.valueOf(usernameTextView.getText())) >= Double.valueOf(String.valueOf(transactionAmountEditText.getText())) || incomeChip.isChecked()){
                            databaseManager.insertTransaction(String.valueOf(transactionNameEditText.getText()), Double.valueOf(String.valueOf(transactionAmountEditText.getText())), String.valueOf(usernameTextView.getText()), String.valueOf(selectedCategoryTextView.getText()), String.valueOf(transactionDescriptionEditText.getText()), -1, endTargetDate);
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(), "Kwota transakcji jest większa od salda", Toast.LENGTH_SHORT).show();
                        }
                        databaseManager.close();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (SQLDataException e) {
                        throw new RuntimeException(e);
                    }


                }
            }


        });
    }
    private void showDatePicker() {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String selectedDateTime = simpleDateFormat.format(calendar.getTime());

                    dateEditText.setText(selectedDateTime);
                    Toast.makeText(AddTransactionActivity.this, "Wybrano datę i godzinę: " + selectedDateTime, Toast.LENGTH_SHORT).show();
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}