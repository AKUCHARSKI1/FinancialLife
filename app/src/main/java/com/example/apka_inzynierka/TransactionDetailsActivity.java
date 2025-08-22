package com.example.apka_inzynierka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLDataException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TransactionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        Intent intent = getIntent();
        Transaction transaction = (Transaction) intent.getSerializableExtra("transaction_details");
        prepareTransactionDetails(transaction);
        LinearLayout deleteButton = findViewById(R.id.linear_layout_buttons);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTransaction(transaction.getTransactionId());
            }
        });
    }

    private void prepareTransactionDetails(Transaction transaction){
        TextView transactionNameTextView = findViewById(R.id.transaction_name_text_view);
        TextView transactionAmountTextView = findViewById(R.id.text_view_transaction_amount);
        TextView transactionCategoryTextView = findViewById(R.id.text_view_transaction_category);
        TextView transactionDateTextView = findViewById(R.id.text_view_transaction_date);
        TextView transactionDescriptionTextView = findViewById(R.id.text_view_transaction_description);
        LinearLayout buttonsLinearLayout = findViewById(R.id.linear_layout_buttons);
        transactionNameTextView.setText(transaction.getTransactionName());
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if(transaction.getTransactionCategory().equals("Wpłata")){
            transactionAmountTextView.setTextColor(0xFF23E700);
            transactionAmountTextView.setText("Kwota: + " + decimalFormat.format(transaction.getTransactionAmount()) + " zł");
        }else{
            transactionAmountTextView.setText("Kwota: - " + decimalFormat.format(transaction.getTransactionAmount()) + " zł");
        }
        transactionCategoryTextView.setText("Kategoria: " + transaction.getTransactionCategory());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        transactionDateTextView.setText("Data dodania: " + dateFormat.format(transaction.getTransactionDate()));
        transactionDescriptionTextView.setText(transaction.getTransactionDescription());

        if(transaction.getState().equals("Aktywny")){
            buttonsLinearLayout.setVisibility(View.VISIBLE);
        }else{
            buttonsLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void deleteTransaction(int transactionId){

        DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
            databaseManager.deleteTransaction(transactionId);
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