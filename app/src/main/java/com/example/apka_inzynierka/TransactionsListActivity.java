package com.example.apka_inzynierka;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;


import java.sql.SQLDataException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TransactionsListActivity extends AppCompatActivity {
    EditText startDateEditText;
    EditText endDateEditText;
    Button refreshButton;
    EditText topAmountEditText;
    EditText bottomAmountEditText;
    EditText transactionNameEditText;
    int cardsCounter = 0;
    LinearLayout layout;
    Boolean showedAlert = false;
    float alphaValue = 1.0f;
    ColorStateList filterChipTextColor;
    ColorStateList filterChipBackgroundColor;
    ScrollView containerScrollView;
    List<String> selectedCategoriesList;
    int whichCalendarButton = 0;
    String endDate;
    String startDate;
    String topAmount;
    String bottomAmount;
    String transactionName;
    Chip filterChip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_list);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        layout = findViewById(R.id.container);
        layout.removeAllViews();
        filterChip = findViewById(R.id.filter_chip);
        endDate = "";
        startDate = "";
        topAmount = "";
        bottomAmount = "";
        transactionName = "";
        selectedCategoriesList = new ArrayList<>();
        showTransactionsList("new");

        /*ImageView targetsImageView = findViewById(R.id.target_image_view);
        targetsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent targetsListActivityIntent = new Intent(MainActivity.this, TargetsListActivity.class);
                startActivityForResult(targetsListActivityIntent, 1);
            }
        });*/
        filterChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        filterChip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDate = "";
                startDate = "";
                topAmount = "";
                bottomAmount = "";
                transactionName = "";
                selectedCategoriesList.clear();
                cardsCounter=0;
                containerScrollView.fullScroll(View.FOCUS_UP);
                layout.removeAllViews();
                showTransactionsList("new");
                containerScrollView.fullScroll(View.FOCUS_UP);

                showedAlert = false;
                filterChip.setChecked(false);
                filterChip.setChipIconVisible(true);
                filterChip.setCloseIconVisible(false);
                filterChip.setChipBackgroundColor(filterChipBackgroundColor);
                filterChip.setTextColor(filterChipTextColor);
            }
        });
        filterChipTextColor = filterChip.getTextColors();
        filterChipBackgroundColor = filterChip.getChipBackgroundColor();
        refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView refreshTextView = new TextView(TransactionsListActivity.this);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0,0);
                refreshTextView.setLayoutParams(layoutParams);
                layout.addView(refreshTextView);
            }
        });
        ImageView addTransactionButton = findViewById(R.id.add_transaction_button);
        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addTargetActivityIntent = new Intent(TransactionsListActivity.this, AddTransactionActivity.class);
                startActivityForResult(addTargetActivityIntent, 1);
            }
        });
        containerScrollView = findViewById(R.id.scrollView2);
        containerScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(containerScrollView.getChildAt(0).getBottom() <= (containerScrollView.getHeight() + containerScrollView.getScrollY())){

                    if(cardsCounter != 0){
                        showTransactionsList("new");
                        View loadingBar = findViewById(R.id.loading_bar);
                        layout.removeView(loadingBar);
                    }

                }
                if(containerScrollView.getChildAt(0).getBottom() <= (containerScrollView.getHeight() + containerScrollView.getScrollY()) + 130){
                    View loadingBar = findViewById(R.id.loading_bar);
                    if (loadingBar != null){
                        int containerPlace = containerScrollView.getScrollY() + containerScrollView.getHeight() - containerScrollView.getChildAt(0).getBottom();
                        alphaValue = 1.0f + 0.0125f * containerPlace;
                        loadingBar.setAlpha(alphaValue);
                    }
                }
            }
        });
    }


    private void showFilterDialog() {
        Dialog filterDialog = new Dialog(this);
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.transaction_filter_dialog);
        filterDialog.show();
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView chooseCategoryTextView = filterDialog.findViewById(R.id.choose_category_text_view);
        LinearLayout layoutMultibox = filterDialog.findViewById(R.id.container_multibox);
        ScrollView scrollviewMultibox = filterDialog.findViewById(R.id.scroll_view_multibox);
        startDateEditText = filterDialog.findViewById(R.id.start_date_edit_text);
        endDateEditText = filterDialog.findViewById(R.id.end_date_edit_text);
        topAmountEditText = filterDialog.findViewById(R.id.top_amount_edit_text);
        bottomAmountEditText = filterDialog.findViewById(R.id.bottom_amount_edit_text);
        transactionNameEditText = filterDialog.findViewById(R.id.transaction_name_edit_text);
        startDateEditText.setText(startDate);
        endDateEditText.setText(endDate);
        topAmountEditText.setText(topAmount);
        bottomAmountEditText.setText(bottomAmount);
        transactionNameEditText.setText(transactionName);
        List<String> categories = Arrays.asList("Cel","Edukacja", "Elektronika", "Prezenty", "Rachunki", "Rozrywka", "Samochód", "Transport", "Trening", "Wpłata", "Wypoczynek", "Zakupy", "Zdrowie");


        ArrayList<CheckBox> checkboxList = new ArrayList<>();
        for(int i = 0; i < categories.size(); i++){
            CheckBox checkbox =  new CheckBox(this);
            checkbox.setText(categories.get(i));
            if(selectedCategoriesList.contains(categories.get(i))){
                checkbox.setChecked(true);
            }
            layoutMultibox.addView(checkbox);
            checkboxList.add(checkbox);
        }
        String selectedCategories = String.join(", ", selectedCategoriesList);
        chooseCategoryTextView.setText(selectedCategories);


        chooseCategoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scrollviewMultibox.getVisibility() == View.VISIBLE){
                    List<String> tmpSelectedCategoriesList = new ArrayList<>();
                    scrollviewMultibox.setVisibility(View.INVISIBLE);
                    chooseCategoryTextView.setBackgroundResource(R.drawable.spinner_bg);

                    for(int i = 0; i < layoutMultibox.getChildCount();i++){
                        CheckBox checkbox = (CheckBox) layoutMultibox.getChildAt(i);
                        if(checkbox.isChecked()){
                            tmpSelectedCategoriesList.add((String) checkbox.getText());
                        }
                    }
                    String selectedCategories = String.join(", ", tmpSelectedCategoriesList);
                    chooseCategoryTextView.setText(selectedCategories);
                }else{
                    scrollviewMultibox.setVisibility(View.VISIBLE);
                    chooseCategoryTextView.setBackgroundResource(R.drawable.spinner_bg_up);

                }

            }
        });
        ImageView startCalendarImageView = filterDialog.findViewById(R.id.start_calendar_image_view);
        ImageView endCalendarImageView = filterDialog.findViewById(R.id.end_calendar_image_view);
        Button acceptButton = filterDialog.findViewById(R.id.accept_button);
        Button cancelButton = filterDialog.findViewById(R.id.cancel_button);

        startCalendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichCalendarButton = 1;
                showDatePicker();
            }
        });

        endCalendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichCalendarButton=2;
                showDatePicker();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionNameEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(transactionNameEditText.getWindowToken(),0);

                filterDialog.cancel();


            }
        });
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scrollviewMultibox.getVisibility() == View.VISIBLE){
                    chooseCategoryTextView.performClick();
                }

                transactionNameEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(transactionNameEditText.getWindowToken(),0);
                endDate = String.valueOf(endDateEditText.getText());
                startDate = String.valueOf(startDateEditText.getText());
                topAmount = String.valueOf(topAmountEditText.getText());
                bottomAmount = String.valueOf(bottomAmountEditText.getText());
                transactionName = String.valueOf(transactionNameEditText.getText());
                selectedCategoriesList.clear();
                for(int i = 0; i < layoutMultibox.getChildCount();i++){
                    CheckBox checkbox = (CheckBox) layoutMultibox.getChildAt(i);
                    if(checkbox.isChecked()){
                        selectedCategoriesList.add((String) checkbox.getText());
                    }
                }
                filterDialog.cancel();
                cardsCounter=0;
                containerScrollView.fullScroll(View.FOCUS_UP);
                layout.removeAllViews();
                showTransactionsList("new");
                containerScrollView.fullScroll(View.FOCUS_UP);

                showedAlert = false;

            }
        });

        filterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(endDate.length() == 0 && startDate.length() == 0 && topAmount.length() == 0 && bottomAmount.length() == 0 && transactionName.length() == 0 && selectedCategoriesList.size() == 0)
                {
                    layout.removeAllViews();
                    showedAlert = false;
                    showTransactionsList("update");
                    filterChip.setChecked(false);
                    filterChip.setChipIconVisible(true);
                    filterChip.setChipBackgroundColor(filterChipBackgroundColor);
                    filterChip.setTextColor(filterChipTextColor);
                    filterChip.setCloseIconVisible(false);
                }
                else{

                    filterChip.setChecked(true);
                    filterChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D2DCBFFF")));
                    filterChip.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF6200EE")));
                    filterChip.setChipIconVisible(false);
                    filterChip.setCloseIconVisible(true);
                }
                refreshButton.performClick();
            }
        });

        filterDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(endDate.length() == 0 && startDate.length() == 0 && topAmount.length() == 0 && bottomAmount.length() == 0 && transactionName.length() == 0 && selectedCategoriesList.size() == 0)
                {
                    layout.removeAllViews();
                    showedAlert = false;
                    showTransactionsList("update");
                    filterChip.setChecked(false);
                    filterChip.setChipIconVisible(true);
                    filterChip.setChipBackgroundColor(filterChipBackgroundColor);
                    filterChip.setTextColor(filterChipTextColor);
                    filterChip.setCloseIconVisible(false);

                }else{
                    filterChip.setChecked(true);
                    filterChip.setChipIconVisible(false);
                    filterChip.setCloseIconVisible(true);
                    filterChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D2DCBFFF")));
                    filterChip.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF6200EE")));

                }
                refreshButton.performClick();
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
           if(whichCalendarButton == 1){
               String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
               startDateEditText.setText(selectedDate);
               Toast.makeText(TransactionsListActivity.this, "Wybrano datę początkową: " + selectedDate, Toast.LENGTH_SHORT).show();
           }else{
               String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
               endDateEditText.setText(selectedDate);
               Toast.makeText(TransactionsListActivity.this, "Wybrano datę końcową: " + selectedDate, Toast.LENGTH_SHORT).show();

           }
        }
    };
    private void showTransactionsList(String mode){
        List<Transaction> transactionList = new ArrayList<>();
        int oldValueCardsCounter = cardsCounter;
        TextView usernameTextView = findViewById(R.id.username_text_view);
        DatabaseManager databaseManager = new DatabaseManager(this);
        if(mode.equals("update")){
            cardsCounter = 0;
        }
        Double balance = 0.0;


        try {
            databaseManager.open();

            if(mode.equals("update")){
                for(int i = 0; i <= (oldValueCardsCounter-1) / 10; i++){
                    transactionList.addAll(databaseManager.getTransactionsList(String.valueOf(usernameTextView.getText()), String.valueOf(i*10), selectedCategoriesList, transactionName, topAmount, bottomAmount, endDate, startDate));                }
            }else{
                transactionList = databaseManager.getTransactionsList(String.valueOf(usernameTextView.getText()), String.valueOf(cardsCounter), selectedCategoriesList, transactionName, topAmount, bottomAmount, endDate, startDate);
            }
            for (int j = 0; j < transactionList.size(); j++) {
                prepareTransactionsCards(transactionList.get(j));
                cardsCounter++;
            }
            balance = databaseManager.getBalance((String) usernameTextView.getText());
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            TextView saldoTextView = findViewById(R.id.saldo_text_view);
            saldoTextView.setText("Saldo: " + decimalFormat.format(balance));
            databaseManager.close();
        }catch (SQLDataException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        if(cardsCounter == 0){
            TextView emptyListTextView = new TextView(this);
            emptyListTextView.setTextColor(Color.parseColor("#474747"));
            emptyListTextView.setText("Brak elementów do wyświetlenia");
            emptyListTextView.setTextSize(24);
            emptyListTextView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,400,0,0);
            emptyListTextView.setLayoutParams(params);
            layout.addView(emptyListTextView);
        }
        if((oldValueCardsCounter != cardsCounter  && !mode.equals("update")) || (mode.equals("update") && oldValueCardsCounter == cardsCounter)){
            prepareLoadingScreen();
        }else if((oldValueCardsCounter == cardsCounter  && !mode.equals("update")) || (mode.equals("update") && oldValueCardsCounter != cardsCounter)){
            if(mode.equals("update") && oldValueCardsCounter == cardsCounter) showedAlert = false;
            if(!showedAlert){
                showedAlert = !showedAlert;
                Toast.makeText(getApplicationContext(), "Wszystkie dane zostały wyświetlone", Toast.LENGTH_SHORT).show();
            }

        }
    }



    private void prepareTransactionsCards(Transaction transaction){
        LinearLayout layout = findViewById(R.id.container);
        View view = getLayoutInflater().inflate(R.layout.transaction_card, null);
        TextView transactionNameTextView = view.findViewById(R.id.text_view_transaction_name);
        TextView transactionAmountTextView = view.findViewById(R.id.text_view_transaction_amount);
        TextView transactionCategoryTextView = view.findViewById(R.id.text_view_transaction_category);
        TextView transactionDateTextView = view.findViewById(R.id.text_view_transaction_date);


        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        transactionNameTextView.setText(transaction.getTransactionName());
        if(transaction.getTransactionCategory().equals("Wpłata")){
            transactionAmountTextView.setTextColor(0xFF23E700);
            transactionAmountTextView.setText("+ " + decimalFormat.format(transaction.getTransactionAmount()) + " zł");
        }else{
            transactionAmountTextView.setText("- " + decimalFormat.format(transaction.getTransactionAmount()) + " zł");
        }

        transactionCategoryTextView.setText("Kategoria: " + transaction.getTransactionCategory());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        transactionDateTextView.setText("Data dodania: " + dateFormat.format(transaction.getTransactionDate()));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionsListActivity.this, TransactionDetailsActivity.class);
                intent.putExtra("transaction_details", transaction);
                startActivityForResult(intent, 1);
            }
        });
        layout.addView(view);
    }

    private void prepareLoadingScreen(){
        View view = getLayoutInflater().inflate(R.layout.loading_bar, null);
        view.setId(R.id.loading_bar);
        layout.addView(view);
        view.setAlpha(0.0f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            layout.removeAllViews();
            showedAlert = false;
            showTransactionsList("update");
        }else if (requestCode == 1) {
            cardsCounter=0;
            containerScrollView.fullScroll(View.FOCUS_UP);
            layout.removeAllViews();
            showTransactionsList("new");
            containerScrollView.fullScroll(View.FOCUS_UP);

            showedAlert = false;
        }
    }
}