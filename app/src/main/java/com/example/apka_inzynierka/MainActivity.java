package com.example.apka_inzynierka;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;


import java.sql.SQLDataException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Dialog animateDialog;
    LinearLayout logout_layout;
    LinearLayout layout;
    TextView username_text_view;
    TextView username_text_view_popup;
    DatabaseManager databaseManager;
    String interval = "Tydzień";
    String chartType = "Saldo";
    String category = "";

    Date currentDate = new Date();
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isNotFirstRun = getIntent().getBooleanExtra("isNotFirstRun", false);
        if(!isNotFirstRun){
            Intent giveUsernameActivityIntent = new Intent(this, GiveUsernameActivity.class);
            startActivity(giveUsernameActivityIntent);
            finish();
        }

        username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        ShapeableImageView photoFrameImageView = findViewById(R.id.photo_frame_image_view);
        autoArchive();
        refreshBalance();

        photoFrameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpSettingsWindow();
            }
        });

        ImageView calculatorImageView = findViewById(R.id.calculator_image_view);
        calculatorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpCalculatorWindow();
            }
        });

        ImageView targetsImageView = findViewById(R.id.target_image_view);
        targetsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent targetsListActivityIntent = new Intent(MainActivity.this, TargetsListActivity.class);
                startActivityForResult(targetsListActivityIntent, 1);
            }
        });
        ImageView historyImageView = findViewById(R.id.history_image_view);
        historyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent transactionListActivityIntent = new Intent(MainActivity.this, TransactionsListActivity.class);
                startActivityForResult(transactionListActivityIntent, 1);
            }
        });
        ImageView addTransactionButton = findViewById(R.id.add_transaction_button);
        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addTargetActivityIntent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivityForResult(addTargetActivityIntent, 1);
                // TextView emptyListTextView = new TextView(TransactionsListActivity.this);
                //layout.addView(emptyListTextView);
            }

        });
        layout = findViewById(R.id.container);
        prepareMainChart();


    }

    private void showChartsSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(R.layout.chart_settings_dialog);
        settingsDialog.show();
        settingsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView intervalSpinnerTextView = settingsDialog.findViewById(R.id.interval_spinner_text_view);
        TextView categorySpinnerTextView = settingsDialog.findViewById(R.id.category_spinner_text_view);
        LinearLayout layoutIntervalMultibox = settingsDialog.findViewById(R.id.container_interval_multibox);
        LinearLayout layoutCategoryMultibox = settingsDialog.findViewById(R.id.container_category_multibox);
        ScrollView scrollViewCategorySettings= settingsDialog.findViewById(R.id.scroll_view_category_multibox);
        ScrollView scrollViewIntervalSettings= settingsDialog.findViewById(R.id.scroll_view_interval_multibox);
        intervalSpinnerTextView.setText(interval);
        TextView categoryTextView = settingsDialog.findViewById(R.id.category_text_view);
        Chip incomeChip = settingsDialog.findViewById(R.id.income_chip);
        Chip outcomeChip= settingsDialog.findViewById(R.id.outcome_chip);
        Chip balanceChip= settingsDialog.findViewById(R.id.balance_chip);
        if(chartType.equals("Saldo")) {
            balanceChip.setChecked(true);
        }
        else if(chartType.equals("Wydatki")) {
            outcomeChip.setChecked(true);
            categoryTextView.setVisibility(View.VISIBLE);
            categorySpinnerTextView.setVisibility(View.VISIBLE);
        }
        else {
            incomeChip.setChecked(true);
        }
        categorySpinnerTextView.setText(category);
        incomeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!incomeChip.isChecked()) incomeChip.setChecked(true);
                categorySpinnerTextView.setText("Wpłata");
                categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                categoryTextView.setVisibility(View.INVISIBLE);
                categorySpinnerTextView.setVisibility(View.INVISIBLE);
                scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                scrollViewCategorySettings.setVisibility(View.INVISIBLE);
            }
        });
        outcomeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!outcomeChip.isChecked()) outcomeChip.setChecked(true);
                categorySpinnerTextView.setText("");
                categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                categoryTextView.setVisibility(View.VISIBLE);
                categorySpinnerTextView.setVisibility(View.VISIBLE);
                scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                scrollViewCategorySettings.setVisibility(View.INVISIBLE);
            }
        });
        balanceChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!balanceChip.isChecked()) balanceChip.setChecked(true);
                categorySpinnerTextView.setText("");
                categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                categoryTextView.setVisibility(View.INVISIBLE);
                categorySpinnerTextView.setVisibility(View.INVISIBLE);
                scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                scrollViewCategorySettings.setVisibility(View.INVISIBLE);
            }
        });
        List<String> intervals = Arrays.asList("Rok","Miesiąc", "Tydzień");
        List<String> categories = Arrays.asList("Cel","Edukacja", "Elektronika", "Prezenty", "Rachunki", "Rozrywka", "Samochód", "Transport", "Trening", "Wpłata", "Wypoczynek", "Zakupy", "Zdrowie");
        ArrayList<TextView> textviewsCategoriesList = new ArrayList<>();
        for(int i = 0; i < categories.size(); i++){
            TextView textView =  new TextView(this);
            textView.setTextSize(18);
            textView.setText(categories.get(i));

            layoutCategoryMultibox.addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    categorySpinnerTextView.setText(textView.getText());
                    categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                    scrollViewCategorySettings.setVisibility(View.INVISIBLE);
                }
            });
            textviewsCategoriesList.add(textView);
        }

        ArrayList<TextView> textviewsIntervalsList = new ArrayList<>();
        for(int i = 0; i < intervals.size(); i++){
            TextView textView =  new TextView(this);
            textView.setTextSize(18);
            textView.setText(intervals.get(i));

            layoutIntervalMultibox.addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intervalSpinnerTextView.setText(textView.getText());
                    intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                    scrollViewCategorySettings.setVisibility(View.INVISIBLE);
                }
            });
            textviewsIntervalsList.add(textView);


        }

        intervalSpinnerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scrollViewIntervalSettings.getVisibility() == View.VISIBLE){
                    intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                }else{
                    scrollViewCategorySettings.setVisibility(View.INVISIBLE);
                    scrollViewIntervalSettings.setVisibility(View.VISIBLE);
                    categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg_up);
                }
            }
        });

        categorySpinnerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scrollViewCategorySettings.getVisibility() == View.VISIBLE){
                    categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    scrollViewCategorySettings.setVisibility(View.INVISIBLE);
                }else{
                    scrollViewCategorySettings.setVisibility(View.VISIBLE);
                    scrollViewIntervalSettings.setVisibility(View.INVISIBLE);
                    intervalSpinnerTextView.setBackgroundResource(R.drawable.spinner_bg);
                    categorySpinnerTextView.setBackgroundResource(R.drawable.spinner_bg_up);
                }
            }
        });

        Button acceptButton = settingsDialog.findViewById(R.id.accept_button);
        Button clearButton = settingsDialog.findViewById(R.id.cancel_button);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interval = String.valueOf(intervalSpinnerTextView.getText());
                if(balanceChip.isChecked()) chartType = String.valueOf(balanceChip.getText());
                else if(incomeChip.isChecked()) chartType = String.valueOf(incomeChip.getText());
                else chartType = String.valueOf(outcomeChip.getText());
                category = String.valueOf(categorySpinnerTextView.getText());
                settingsDialog.dismiss();
                layout.removeAllViews();
                currentDate = new Date();
                prepareMainChart();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interval = "Tydzień";
                chartType = "Saldo";
                category = "";
                settingsDialog.dismiss();
                layout.removeAllViews();
                currentDate = new Date();
                prepareMainChart();

            }
        });

        settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

    }


    private void prepareMainChart() {
       // System.out.println("TEST: " + currentDate);
       // System.out.println(currentDate.getMonth());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        String dateStart;
        String dateEnd;
        List<String> dates = new ArrayList<>();
        List<String> dataDates = new ArrayList<>();
        View view = getLayoutInflater().inflate(R.layout.bar_chart, null);
        TextView chartTitleTextView = view.findViewById(R.id.bar_title_text_view);
        String[] tmpDates = {"Styczeń","Luty","Marzec","Kwiecień","Maj","Czerwiec","Lipiec","Sierpień","Wrzesień","Październik","Listopad","Grudzień"};

        String titleTime;
        int howManyBars = 0;
        if(interval.equals("Tydzień")) {
            SimpleDateFormat chartFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            howManyBars = 7;
            calendar.add(Calendar.DAY_OF_YEAR, 2 - calendar.get(Calendar.DAY_OF_WEEK));
            dateStart = dataFormat.format(calendar.getTime());
            for(int i = 0; i < howManyBars - 1; i++){
                dates.add(chartFormat.format(calendar.getTime()));
                dataDates.add(dataFormat.format(calendar.getTime()));

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
          //  System.out.println(dates);
            dates.add(chartFormat.format(calendar.getTime()));
            dataDates.add(dataFormat.format(calendar.getTime()));
            dateEnd = dataFormat.format(calendar.getTime());
          //  System.out.println(dateStart);
           // System.out.println(dateEnd);
            titleTime = dateStart + " - " + dateEnd;
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda\n" + titleTime);
            } else if(chartType.equals("Wydatki")) {
                if(category.equals("")){
                    chartTitleTextView.setText("Wykres wydatków\n" + titleTime);
                }else{
                    chartTitleTextView.setText("Wykres wydatków w kategorii " + category.toLowerCase() + "\n" + titleTime);
                }
            }else{
                chartTitleTextView.setText("Wykres dochodów\n" + titleTime);
            }

        }
        else if(interval.equals("Miesiąc")){
            howManyBars = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            SimpleDateFormat chartFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_MONTH));
            dateStart = dataFormat.format(calendar.getTime());
            for(int i = 0; i < howManyBars - 1; i++){
                dates.add(chartFormat.format(calendar.getTime()));
                dataDates.add(dataFormat.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
           // System.out.println(dates);
            dates.add(chartFormat.format(calendar.getTime()));
            dataDates.add(dataFormat.format(calendar.getTime()));
            dateEnd = dataFormat.format(calendar.getTime());
            titleTime = tmpDates[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda\n" + titleTime);
            } else if(chartType.equals("Wydatki")) {
                if(category.equals("")){
                    chartTitleTextView.setText("Wykres wydatków\n" + titleTime);
                }else{
                    chartTitleTextView.setText("Wykres wydatków w kategorii " + category.toLowerCase() + "\n" + titleTime);
                }
            }else{
                chartTitleTextView.setText("Wykres dochodów\n" + titleTime);
            }
        } else {
            howManyBars = 12;
            SimpleDateFormat chartFormat = new SimpleDateFormat("MMMM yyyy", new Locale("pl", "PL"));
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_YEAR));
            dateStart = dataFormat.format(calendar.getTime());
            for(int i = 0; i < howManyBars - 1; i++){
                dataDates.add(dataFormat.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_YEAR, 31);
            }
            for(int i = 0; i < howManyBars; i++){
                dates.add(tmpDates[i] + " " + calendar.get(Calendar.YEAR));
            }

            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            dates.add(chartFormat.format(calendar.getTime()));
            dataDates.add(dataFormat.format(calendar.getTime()));
            dateEnd = dataFormat.format(calendar.getTime());
            titleTime = "Rok " + calendar.get(Calendar.YEAR);
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda - " + titleTime.toLowerCase());
            } else if(chartType.equals("Wydatki")) {
                if(category.equals("")){
                    chartTitleTextView.setText("Wykres wydatków - " + titleTime.toLowerCase());
                }else{
                    chartTitleTextView.setText("Wykres wydatków w kategorii " + category.toLowerCase() + "\n" + titleTime);
                }
            }else{
                chartTitleTextView.setText("Wykres dochodów - " + titleTime.toLowerCase());
            }
           // System.out.println(dates);
        }
       // System.out.println("BARS: " + howManyBars);
       // System.out.println("POCZĄTEK: " + dateStart);
       // System.out.println("KONIEC: " + dateEnd);


        ArrayList<Double> barValues = new ArrayList<>();
        try {
            databaseManager.open();
            if(chartType.equals("Saldo")){
                barValues.addAll(databaseManager.getDataToMainChartBalance(String.valueOf(username_text_view.getText()), dateStart, dateEnd, interval, howManyBars, "Wpłata", dataDates));

            } else {
                barValues.addAll(databaseManager.getDataToMainChart(String.valueOf(username_text_view.getText()), dateStart, dateEnd, interval, howManyBars, category, dataDates));
            }
           // System.out.println(databaseManager.getDataToCategoriesChart("Anastazja", dateStart, dateEnd));
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        }




        howManyBars = barValues.size();

        ArrayList barArrayList = new ArrayList<>();
        for(int i = 0; i < howManyBars; i++){
            barArrayList.add(new BarEntry(0f + i * 1f, barValues.get(i).intValue()));
        }

        BarChart barChart = view.findViewById(R.id.chart);
        BarDataSet barDataSet = new BarDataSet(barArrayList, "WYKRES");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setColors(Color.parseColor("#00CED1"));
        barDataSet.setValueTextSize(0f);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);



        final String[] datesArray = dates.toArray(new String[0]);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(datesArray));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-90f);

        layout.addView(view);

        View dateSwitcherView = getLayoutInflater().inflate(R.layout.date_switcher, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.topMargin = 40;
        layoutParams.bottomMargin = 40;

        dateSwitcherView.setLayoutParams(layoutParams);
        layout.addView(dateSwitcherView);
        ImageView chartSettingsImageView = dateSwitcherView.findViewById(R.id.chart_settings);
        ImageView switchDateLeftImageView = dateSwitcherView.findViewById(R.id.date_left);
        ImageView switchDateRightImageView = dateSwitcherView.findViewById(R.id.date_right);

        chartSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChartsSettingsDialog();
            }
        });

        switchDateLeftImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(currentDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                if(interval.equals("Tydzień")){
                    calendar.add(Calendar.DAY_OF_MONTH, - 7);
                } else if(interval.equals("Miesiąc")){
                    calendar.add(Calendar.DAY_OF_MONTH, - calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                } else {
                    calendar.add(Calendar.YEAR, - 1);
                }

                currentDate = calendar.getTime();
                System.out.println(currentDate);
                layout.removeAllViews();
                prepareMainChart();
            }
        });

        switchDateRightImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat chartFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                System.out.println(dates);
                System.out.println(currentDate);
                System.out.println(chartFormat.format(new Date().getTime()));
                if((!interval.equals("Rok") && dates.contains(chartFormat.format(new Date().getTime()))) || (interval.equals("Rok") && currentDate.getYear() == new Date().getYear())){
                    Toast.makeText(getApplicationContext(), "Dotarłeś do aktualnej daty", Toast.LENGTH_SHORT).show();
                } else {

                    System.out.println(currentDate);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    if(interval.equals("Tydzień")){
                        calendar.add(Calendar.DAY_OF_MONTH,7);
                    } else if(interval.equals("Miesiąc")){
                        calendar.add(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    } else {
                        calendar.add(Calendar.YEAR, 1);
                    }
                    System.out.println(dates);

                    currentDate = calendar.getTime();
                    System.out.println(currentDate);
                    layout.removeAllViews();
                    prepareMainChart();
                }

            }
        });

        //if(chartType.equals("Saldo") || (chartType.equals("Wydatki") && category.equals(""))){
        //    preparePieChart(dateStart, dateEnd, titleTime);
       // }
      //  System.out.println(chartType);
      //  System.out.println(category);

    }

   /* private void preparePieChart(String dateStart, String dateEnd, String titleTime) {
        View view = getLayoutInflater().inflate(R.layout.pie_chart, null);
        TextView chartTitleTextView = view.findViewById(R.id.pie_title_text_view);

        if(interval.equals("Tydzień")) {
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda\n" + titleTime);
            } else if(chartType.equals("Wydatki")) {
                chartTitleTextView.setText("Wykres wydatków\n" + titleTime);
            }

        }
        else if(interval.equals("Miesiąc")){
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda\n" + titleTime);
            } else if(chartType.equals("Wydatki")) {
                chartTitleTextView.setText("Wykres wydatków\n" + titleTime);

            }
        } else {
            if(chartType.equals("Saldo")){
                chartTitleTextView.setText("Wykres salda - " + titleTime.toLowerCase());
            } else if(chartType.equals("Wydatki")) {

                chartTitleTextView.setText("Wykres wydatków - " + titleTime.toLowerCase());

            }
            // System.out.println(dates);
        }
        ArrayList<Double> barValues = new ArrayList<>();
        try {
            databaseManager.open();
            barValues.addAll(databaseManager.getDataToCategoriesChart("Anastazja", dateStart, dateEnd));
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        List<String> categories = Arrays.asList("Cel","Edukacja", "Elektronika", "Prezenty", "Rachunki", "Rozrywka", "Samochód", "Transport", "Trening", "Wypoczynek", "Zakupy", "Zdrowie");
        System.out.println(barValues);
        ArrayList<PieEntry> pieEntryList = new ArrayList<>();
        for(int i = 0; i < categories.size(); i++){
            if(barValues.get(i) != 0.0){
                System.out.println("DODANO " + barValues.get(i));
                pieEntryList.add(new PieEntry(Float.parseFloat(String.valueOf(barValues.get(i))), categories.get(i)));
            }

        }

        PieChart pieChart = view.findViewById(R.id.chart);
        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawSliceText(false);
        pieChart.getData().setDrawValues(false);
        System.out.println(pieData.getDataSet().getColors());
        Legend legend = pieChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        LegendEntry[] legendEntries = new LegendEntry[pieData.getEntryCount()];

        layout.addView(view);
    }*/



    private void autoArchive() {
        List<Target> targetsList;
        TextView usernameTextView = findViewById(R.id.username_text_view);
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
            targetsList = databaseManager.getTargetsList(String.valueOf(usernameTextView.getText()), "Aktywny", String.valueOf(0));
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < targetsList.size(); i++) {
            Date targetUpdateDate = targetsList.get(i).getTargetUpdateDate();
            Date targetEndDate = targetsList.get(i).getTargetEndDate();
            if(targetsList.get(i).getTargetState().equals("Aktywny")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(targetEndDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                targetEndDate = calendar.getTime();
                if (targetEndDate.compareTo(targetUpdateDate) == -1) {
                    try {
                        databaseManager.open();
                        databaseManager.updateArchiveTarget(targetsList.get(i).getTargetId());
                        databaseManager.close();
                    } catch (SQLDataException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


        }
    }

    private void PopUpCalculatorWindow() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.calculator_choice_popup);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopUpAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        LinearLayout layoutCurrency = dialog.getWindow().findViewById(R.id.currency_converter_layout);
        layoutCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent currencyCalculatorIntent = new Intent(getApplicationContext(), CurrencyCalculator.class);
                startActivity(currencyCalculatorIntent);
                dialog.dismiss();
            }
        });
    }



    private void PopUpSettingsWindow() {

        animateDialog = new Dialog(this);
        animateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        animateDialog.setContentView(R.layout.settings_popup);
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view_popup = (TextView) animateDialog.findViewById(R.id.username_text_view);
        username_text_view_popup.setText(sessionToken);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        animateDialog.show();
        animateDialog.getWindow().setLayout(width,height);
        animateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        animateDialog.getWindow().setGravity(Gravity.TOP);
        LinearLayout settingsLayout = animateDialog.findViewById(R.id.animate_settings_layout);
        ViewGroup.LayoutParams layoutParams = settingsLayout.getLayoutParams();
        layoutParams.height = 200;
        settingsLayout.setLayoutParams(layoutParams);
        startTimer();
    }

    private final Handler handler = new Handler();
    private final Runnable animation = new Runnable() {
        @Override
        public void run() {
            LinearLayout settingsLayout = animateDialog.findViewById(R.id.animate_settings_layout);
            ViewGroup.LayoutParams layoutParams = settingsLayout.getLayoutParams();
            if(layoutParams.height < 1050){
                layoutParams.height = layoutParams.height + 20;
                settingsLayout.setLayoutParams(layoutParams);
                handler.postDelayed(this, 20);
            }
            else{
                stopTimer();
            }


        }
    };

    public void startTimer() {
        handler.post(animation);
    }

    public void stopTimer() {

        handler.removeCallbacks(animation);
    }

    public void logout(View v){

        animateDialog.dismiss();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("sessionToken");
        editor.apply();
        finish();

        Intent giveUsernameActivityIntent = new Intent(this, GiveUsernameActivity.class);
        startActivity(giveUsernameActivityIntent);

    }


    private void refreshBalance(){

        TextView usernameTextView = findViewById(R.id.username_text_view);
        Double balance = 0.0;
        try {
            databaseManager.open();
            balance = databaseManager.getBalance((String) usernameTextView.getText());
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            TextView saldoTextView = findViewById(R.id.saldo_text_view);
            saldoTextView.setText("Saldo: " + decimalFormat.format(balance));
            databaseManager.close();
        }catch (SQLDataException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshBalance();
        layout.removeAllViews();
        currentDate = new Date();
        prepareMainChart();
    }

}