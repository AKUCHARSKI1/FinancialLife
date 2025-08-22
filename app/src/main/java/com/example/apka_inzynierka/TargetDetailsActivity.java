package com.example.apka_inzynierka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLDataException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TargetDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_details);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        Intent intent = getIntent();
        Target target = (Target) intent.getSerializableExtra("target_details");
        prepareTargetDetails(target);
        Button archiveButton = findViewById(R.id.archive_button);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                archiveTarget(target.getTargetId());
            }
        });
        ImageButton deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTarget(target.getTargetId());
            }
        });

        Button setAsideAnAmountButton = findViewById(R.id.set_aside_an_amount_button);
        setAsideAnAmountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTransactionToTargetDialog(target);
            }


        });
    }

    private void prepareTargetDetails(Target target){
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView targetNameTextView = findViewById(R.id.target_name_text_view);
        TextView amountTargetTextView = findViewById(R.id.amount_target_text_view);
        TextView amountTextView = findViewById(R.id.amount_text_view);
        TextView dateEndTargetTextView = findViewById(R.id.date_end_text_view);
        TextView progressTextView = findViewById(R.id.progress_text_view);
        LinearLayout buttonsLinearLayout = findViewById(R.id.linear_layout_buttons);
        targetNameTextView.setText(target.getTargetName());
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        amountTargetTextView.setText("Kwota docelowa: " + String.valueOf(decimalFormat.format(target.getTargetAmount())) + " zł");
        amountTextView.setText("Zebrana kwota: " + String.valueOf(decimalFormat.format(target.getTargetActualAmount())) + " zł");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateEndTargetTextView.setText("Data zakończenia: " + dateFormat.format(target.getTargetEndDate()));
        Double progress = target.getTargetActualAmount()/ target.getTargetAmount();
        progressTextView.setText(decimalFormat.format(progress*100) + "%");
        progressBar.setProgress((int) Math.round(progress*100));
        if(target.getTargetState().equals("Aktywny")){
            buttonsLinearLayout.setVisibility(View.VISIBLE);
        }else{
            buttonsLinearLayout.setVisibility(View.INVISIBLE);
        }
    }
    private void showAddTransactionToTargetDialog(Target target) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_transaction_to_target_dialog);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView saldoTextView = dialog.getWindow().findViewById(R.id.saldo_text_view);
        DatabaseManager databaseManager = new DatabaseManager(this);
        Double balance = 0.0;
        try {
            databaseManager.open();
            balance = databaseManager.getBalance(target.getUsername());
            databaseManager.close();
        } catch (SQLDataException | ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        saldoTextView.setText("Saldo: " + String.valueOf(decimalFormat.format(balance)));
        Button cancelButton = dialog.getWindow().findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button acceptButton = dialog.getWindow().findViewById(R.id.set_aside_an_amount_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTransactionToTarget(target, dialog);
            }
        });
    }

    private void addTransactionToTarget(Target target, Dialog dialog) {
        Double remainingAmount = target.getTargetAmount() - target.getTargetActualAmount();
        DatabaseManager databaseManager = new DatabaseManager(this);
        EditText amountEditText = dialog.getWindow().findViewById(R.id.target_amount_edit_text);
        String amount = String.valueOf(amountEditText.getText());
        if(amount.isEmpty()){
            Toast.makeText(getApplicationContext(), "Nie podano kwoty", Toast.LENGTH_SHORT).show();
        }else{
            if(amount.charAt(0) == '.' || amount.charAt(0) == ','){
                Toast.makeText(getApplicationContext(), "Niepoprawny format kwoty", Toast.LENGTH_SHORT).show();
            }else{
                Double balance = 0.0;
                try {
                    databaseManager.open();
                    balance = databaseManager.getBalance(target.getUsername());
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    decimalFormat.format(balance);
                    if(remainingAmount >= Double.parseDouble(amount)){
                        if(balance < Double.parseDouble(amount)){
                            Toast.makeText(getApplicationContext(), "Za małe saldo", Toast.LENGTH_SHORT).show();
                        }else{
                            databaseManager.updateUpdateDateTarget(target.getTargetId());
                            databaseManager.insertTransaction("Wpłata na cel - " + target.getTargetName(), Double.parseDouble(amount), target.getUsername(), "Cel", "Wpłata na cel - " + target.getTargetName(), target.getTargetId(),null);
                            target.setTargetActualAmount(target.getTargetActualAmount() + Double.parseDouble(amount));

                            if(target.getTargetActualAmount() >= target.getTargetAmount()){
                                databaseManager.updateArchiveTarget(target.getTargetId());
                                target.setTargetState("Nieaktywny");
                                Toast.makeText(getApplicationContext(), "Cel został osiągnięty!\nPrzeniesiono go do zarchiwizowanych", Toast.LENGTH_SHORT).show();
                            }
                            prepareTargetDetails(target);

                            dialog.dismiss();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Próbujesz dodać zbyt dużą kwotę", Toast.LENGTH_SHORT).show();
                    }

                    databaseManager.close();
                } catch (SQLDataException | ParseException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        }

    }


    public void archiveTarget(int targetId){
        DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
            databaseManager.updateArchiveTarget(targetId);
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void deleteTarget(int targetId){

        DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
            databaseManager.deleteTargetAndTransactions(targetId);
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}