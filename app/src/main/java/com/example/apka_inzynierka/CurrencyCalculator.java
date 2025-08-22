package com.example.apka_inzynierka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrencyCalculator extends AppCompatActivity {
    List<JSONObject> currencies = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_calculator);
        TextView username_text_view = (TextView) findViewById(R.id.username_text_view);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        username_text_view.setText(sessionToken);
        DownloadDataTask ddt = new DownloadDataTask();
        JSONArray rates = null;
        new DownloadDataTask().execute();
        EditText firstMoney = findViewById(R.id.first_money);
        firstMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ddt.calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showCurrenciesDescription();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void showCurrenciesDescription() throws JSONException {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.currency_decription);
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sessionToken = preferences.getString("sessionToken", null);
        TextView username_text_view_popup = (TextView) dialog.findViewById(R.id.username_text_view);
        username_text_view_popup.setText(sessionToken);
        TableLayout tableLayout = dialog.findViewById(R.id.table_layout);

        for (int i = 0; i < currencies.toArray().length; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView textView1 = new TextView(this);
            textView1.setTextSize(16);
            textView1.setText(currencies.get(i).getString("currency"));
            textView1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView textView2 = new TextView(this);
            textView1.setTextSize(16);
            textView2.setText(currencies.get(i).getString("code"));
            textView2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            tableRow.addView(textView1);
            tableRow.addView(textView2);

            tableLayout.addView(tableRow);
        }
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setLayout(width,height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void swapCurrency(View v){
        Spinner spinnerCurrencyFirst = (Spinner) findViewById(R.id.spinner_currency_first);
        Spinner spinnerCurrencySecond = (Spinner) findViewById(R.id.spinner_currency_second);
        int positionFirst = spinnerCurrencyFirst.getSelectedItemPosition();
        int positionSecond = spinnerCurrencySecond.getSelectedItemPosition();
        spinnerCurrencyFirst.setSelection(positionSecond);
        spinnerCurrencySecond.setSelection(positionFirst);
    }

    private class DownloadDataTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... voids) {
            JSONArray rates = null;
            try {
                URL url = new URL("https://api.nbp.pl/api/exchangerates/tables/a");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());

                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    rates = jsonObject.getJSONArray("rates");
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("CurrencyCalculator", "Błąd pobierania danych: " + e.getMessage());
            }
            return rates;
        }

        @Override
        protected void onPostExecute(JSONArray rates) {
            if (rates != null) {
                try {
                    updateSpinner(rates);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        protected void updateSpinner(JSONArray rates) throws JSONException {
            Spinner spinnerCurrencyFirst = findViewById(R.id.spinner_currency_first);
            Spinner spinnerCurrencySecond = findViewById(R.id.spinner_currency_second);
            List<String> options = new ArrayList<>();
            int indexEUR = 1;
            
            JSONObject entryPLN = new JSONObject();
            entryPLN.put("currency", "polski złoty");
            entryPLN.put("code", "PLN");
            entryPLN.put("mid", 1);
            currencies.add(entryPLN);
            try {
                
                for (int i = 0; i < rates.length(); i++) {
                    currencies.add(rates.getJSONObject(i));
                }
                for (int i = 0; i < currencies.size(); i++) {
                    String code = currencies.get(i).getString("code");
                    if(code.equals("EUR")) indexEUR = i;
                    options.add(code);
                }
            } catch (Exception e) {
                Log.e("CurrencyCalculator", "Błąd przetwarzania danych: " + e.getMessage());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(CurrencyCalculator.this, android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


            spinnerCurrencyFirst.setAdapter(adapter);
            spinnerCurrencyFirst.setSelection(0);
            spinnerCurrencySecond.setAdapter(adapter);
            spinnerCurrencySecond.setSelection(indexEUR);

            spinnerCurrencyFirst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    calculate();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            spinnerCurrencySecond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    calculate();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        protected void calculate() {
            EditText firstMoney = findViewById(R.id.first_money);
            String firstMoneySum = String.valueOf(firstMoney.getText());

            if(firstMoneySum.length() != 0){
                TextView secondMoney = (TextView) findViewById(R.id.second_money);
                Spinner spinnerCurrencyFirst = (Spinner) findViewById(R.id.spinner_currency_first);
                Spinner spinnerCurrencySecond = (Spinner) findViewById(R.id.spinner_currency_second);
                String firstCurrency = (String) spinnerCurrencyFirst.getSelectedItem();
                String secondCurrency = (String) spinnerCurrencySecond.getSelectedItem();
                double firstCost = 0.0;
                double secondCost = 0.0;
                for (int i = 0; i < currencies.size(); i++) {
                    String code = null;
                    try {
                        code = currencies.get(i).getString("code");
                        double cost = currencies.get(i).getDouble("mid");
                        if(code.equals(firstCurrency)){
                            firstCost = cost;
                        }
                        if(code.equals(secondCurrency)){
                            secondCost = cost;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                double result = Double.parseDouble(firstMoneySum) / secondCost * firstCost;
                secondMoney.setText(String.format("%.2f", result));
            }

        }

    }
}