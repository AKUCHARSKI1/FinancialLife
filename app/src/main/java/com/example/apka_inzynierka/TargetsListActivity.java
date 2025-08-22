package com.example.apka_inzynierka;

import static java.lang.Thread.sleep;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.sql.SQLDataException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TargetsListActivity extends AppCompatActivity {
    String listMode = "Aktywny";
    int cardsCounter = 0;
    ScrollView containerScrollView;
    LinearLayout layout;
    Boolean showedAlert = false;
    float alphaValue = 1.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets_list);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        layout = findViewById(R.id.container);
        Chip activeChip = findViewById(R.id.active_chip);
        TextView targetsTypeTextView = findViewById(R.id.targets_type_text_view);
        layout.removeAllViews();
        showTargetsList(listMode, "new");
        containerScrollView = findViewById(R.id.container_scroll_view);
        activeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!activeChip.isChecked()) activeChip.setChecked(true);
                targetsTypeTextView.setText("Aktywne cele");
                listMode = "Aktywny";
                containerScrollView.fullScroll(View.FOCUS_UP);
                layout.removeAllViews();
                cardsCounter = 0;
                showTargetsList(listMode, "new");
                containerScrollView.fullScroll(View.FOCUS_UP);
            }
        });

        Chip inactiveChip = findViewById(R.id.inactive_chip);
        inactiveChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!inactiveChip.isChecked()) inactiveChip.setChecked(true);
                targetsTypeTextView.setText("Nieaktywne cele");
                listMode = "Nieaktywny";
                containerScrollView.fullScroll(View.FOCUS_UP);
                cardsCounter = 0;
                layout.removeAllViews();
                showTargetsList(listMode,"new");
                containerScrollView.fullScroll(View.FOCUS_UP);
            }
        });
        ImageView addTargetButton = findViewById(R.id.add_target_button);
        addTargetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addTargetActivityIntent = new Intent(TargetsListActivity.this, AddTargetActivity.class);
                startActivityForResult(addTargetActivityIntent, 1);
            }
        });
        containerScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(containerScrollView.getChildAt(0).getBottom() <= (containerScrollView.getHeight() + containerScrollView.getScrollY())){
                    if(cardsCounter != 0){
                        showTargetsList(listMode,"new");
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

    private void showTargetsList(String state, String mode){
        List<Target> targetList = new ArrayList<>();
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
                    targetList.addAll(databaseManager.getTargetsList(String.valueOf(usernameTextView.getText()), state, String.valueOf(i*10)));
                }
            }else{
                targetList = databaseManager.getTargetsList(String.valueOf(usernameTextView.getText()), state, String.valueOf(cardsCounter));
            }
            for (int j = 0; j < targetList.size(); j++) {
                prepareTargetsCards(targetList.get(j));
                cardsCounter++;
            }

            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
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



    private void prepareTargetsCards(Target target){
        View view = getLayoutInflater().inflate(R.layout.target_card, null);
        TextView targetNameTextView = view.findViewById(R.id.text_view_target_name);
        TextView CurrentAmountTextView = view.findViewById(R.id.text_view_current_amount);
        TextView targetAmountTextView = view.findViewById(R.id.text_view_target_amount);
        TextView targetDateTextView = view.findViewById(R.id.text_view_target_date);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        Double progress = target.getTargetActualAmount()/ target.getTargetAmount();
        progressBar.setProgress((int) Math.round(progress*100));
        targetNameTextView.setText(target.getTargetName());
        CurrentAmountTextView.setText("Aktualna kwota: " + String.valueOf(decimalFormat.format(target.getTargetActualAmount())) + " zł");
        targetAmountTextView.setText("Cel: " + String.valueOf(decimalFormat.format(target.getTargetAmount())) + " zł");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        targetDateTextView.setText("Data zakończenia: " + dateFormat.format(target.getTargetEndDate()));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetsListActivity.this, TargetDetailsActivity.class);
                intent.putExtra("target_details", target);
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
            showTargetsList(listMode, "update");
            showedAlert = false;
        }else if (requestCode == 1) {
            cardsCounter=0;
            containerScrollView.fullScroll(View.FOCUS_UP);
            layout.removeAllViews();
            showTargetsList(listMode, "new");
            containerScrollView.fullScroll(View.FOCUS_UP);
            TextView targetsTypeTextView = findViewById(R.id.targets_type_text_view);
            if(listMode.equals("Aktywny")){
                targetsTypeTextView.setText("Aktywne cele");
            }else{
                targetsTypeTextView.setText("Nieaktywne cele");
            }
            showedAlert = false;
        }
    }
}