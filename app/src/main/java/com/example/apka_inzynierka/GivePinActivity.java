package com.example.apka_inzynierka;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLDataException;

import pub.devrel.easypermissions.EasyPermissions;

public class GivePinActivity extends AppCompatActivity implements View.OnClickListener{

    EditText first_pin_edit_text;
    EditText second_pin_edit_text;
    EditText third_pin_edit_text;
    EditText fourth_pin_edit_text;
    Button zero_button;
    Button one_button;
    Button two_button;
    Button three_button;
    Button four_button;
    Button five_button;
    Button six_button;
    Button seven_button;
    Button eight_button;
    Button nine_button;
    ImageButton delete_button;
    ImageButton confirm_button;
    TextView hello_text_view;
    String username;
    DatabaseManager databaseManager;
    TextView error_text_view;
    ShapeableImageView photo_frame_image_view;
    Uri selectedImage = null;
    User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_give_pin);



        hello_text_view = (TextView) findViewById(R.id.hello_text_view);

        Intent intent = getIntent();
        username = intent.getStringExtra("usernameKey");
        hello_text_view.setText("Cześć " + username);

        databaseManager = new DatabaseManager(this);

        first_pin_edit_text = (EditText) findViewById(R.id.first_pin_edit_text);
        second_pin_edit_text = (EditText) findViewById(R.id.second_pin_edit_text);
        third_pin_edit_text = (EditText) findViewById(R.id.third_pin_edit_text);
        fourth_pin_edit_text = (EditText) findViewById(R.id.fourth_pin_edit_text);
        zero_button = (Button) findViewById(R.id.zero_button);
        one_button = (Button) findViewById(R.id.one_button);
        two_button = (Button) findViewById(R.id.two_button);
        three_button = (Button) findViewById(R.id.three_button);
        four_button = (Button) findViewById(R.id.four_button);
        five_button = (Button) findViewById(R.id.five_button);
        six_button = (Button) findViewById(R.id.six_button);
        seven_button = (Button) findViewById(R.id.seven_button);
        eight_button = (Button) findViewById(R.id.eight_button);
        nine_button = (Button) findViewById(R.id.nine_button);
        delete_button = (ImageButton) findViewById(R.id.delete_button);
        confirm_button = (ImageButton) findViewById(R.id.confirm_button);
        error_text_view = (TextView) findViewById(R.id.error_text_view);
        photo_frame_image_view = (ShapeableImageView) findViewById(R.id.photo_frame_get_pin_view);

        zero_button.setOnClickListener(this);
        one_button.setOnClickListener(this);
        two_button.setOnClickListener(this);
        three_button.setOnClickListener(this);
        four_button.setOnClickListener(this);
        five_button.setOnClickListener(this);
        six_button.setOnClickListener(this);
        seven_button.setOnClickListener(this);
        eight_button.setOnClickListener(this);
        nine_button.setOnClickListener(this);
        final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 123;


        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        user = databaseManager.selectUserByUsername(username);
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(Uri.parse(user.getPhoto()));
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            photo_frame_image_view.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        databaseManager.close();

    }

    @Override
    public void onClick(View view) {
        Button button = findViewById(view.getId());
        button.getText();
        if(first_pin_edit_text.getText().length()==0){
            first_pin_edit_text.setText(button.getText());
        } else if(second_pin_edit_text.getText().length()==0){
            second_pin_edit_text.setText(button.getText());
        } else if(third_pin_edit_text.getText().length()==0){
            third_pin_edit_text.setText(button.getText());
        } else if(fourth_pin_edit_text.getText().length()==0){
            fourth_pin_edit_text.setText(button.getText());
        }
    }

    public void clickConfirmPinButton(View v) throws NoSuchAlgorithmException, SQLDataException {

        String pin = String.valueOf(first_pin_edit_text.getText()).
                concat(String.valueOf(second_pin_edit_text.getText())).
                concat(String.valueOf(third_pin_edit_text.getText())).
                concat(String.valueOf(fourth_pin_edit_text.getText()));

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] encodedhash = digest.digest(pin.getBytes(Charset.forName("UTF-8")));

        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }


        if(hexString.toString().equals(user.getHashedPin())){
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String sessionToken = preferences.getString("sessionToken", null);

            if (sessionToken == null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sessionToken", username);
                editor.apply();
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("isNotFirstRun", true);
            startActivity(intent);
            finish();
        } else {
            error_text_view.setVisibility(View.VISIBLE);
            error_text_view.setText("Podano błędny PIN");
        }

    }

    public void clickDeletePinButton(View v){

        if(fourth_pin_edit_text.getText().length()!=0){
            fourth_pin_edit_text.setText("");
        } else if(third_pin_edit_text.getText().length()!=0){
            third_pin_edit_text.setText("");
        } else if(second_pin_edit_text.getText().length()!=0){
            second_pin_edit_text.setText("");
        } else if(first_pin_edit_text.getText().length()!=0){
            first_pin_edit_text.setText("");
        }
    }

    public void clickNotThisAccount(View v){

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("sessionToken");
        editor.apply();
        finish();
        Intent intent = new Intent(this, GiveUsernameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("isNotFirstRun", true);
        startActivity(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}