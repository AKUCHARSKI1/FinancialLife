package com.example.apka_inzynierka;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLDataException;

public class SetPinActivity extends AppCompatActivity implements View.OnClickListener{

    EditText first_pin_edit_text;
    EditText second_pin_edit_text;
    EditText third_pin_edit_text;
    EditText fourth_pin_edit_text;
    EditText repeat_first_pin_edit_text;
    EditText repeat_second_pin_edit_text;
    EditText repeat_third_pin_edit_text;
    EditText repeat_fourth_pin_edit_text;
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
    TextView username_text_view;
    String username;
    TextView error_text_view;
    DatabaseManager databaseManager;
    ImageButton change_photo_button;
    ShapeableImageView photo_frame_image_view;
    Uri selectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        username_text_view = (TextView) findViewById(R.id.username_text_view);

        Intent intent = getIntent();
        username = intent.getStringExtra("usernameKey");
        username_text_view.setText(username);

        databaseManager = new DatabaseManager(this);

        first_pin_edit_text = (EditText) findViewById(R.id.first_pin_edit_text);
        second_pin_edit_text = (EditText) findViewById(R.id.second_pin_edit_text);
        third_pin_edit_text = (EditText) findViewById(R.id.third_pin_edit_text);
        fourth_pin_edit_text = (EditText) findViewById(R.id.fourth_pin_edit_text);
        repeat_first_pin_edit_text = (EditText) findViewById(R.id.repeat_first_pin_edit_text);
        repeat_second_pin_edit_text = (EditText) findViewById(R.id.repeat_second_pin_edit_text);
        repeat_third_pin_edit_text = (EditText) findViewById(R.id.repeat_third_pin_edit_text);
        repeat_fourth_pin_edit_text = (EditText) findViewById(R.id.repeat_fourth_pin_edit_text);
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
        change_photo_button = (ImageButton) findViewById(R.id.change_photo_button);


        change_photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            selectedImage = data.getData();
            photo_frame_image_view = (ShapeableImageView) findViewById(R.id.photo_frame_image_view);

            File folder = new File(getExternalFilesDir(null),"Photos");
            if(!folder.exists()){
                folder.mkdirs();
            }

            File photo = new File(folder, "photo_qwer.jpg");

            try(FileOutputStream fos = new FileOutputStream(photo)){

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            File[] files = folder.listFiles();

            if(files != null && files.length > 0){
                File file = files[0];
                Bitmap bitmapPhoto = BitmapFactory.decodeFile(file.getAbsolutePath());
                photo_frame_image_view.setImageBitmap(bitmapPhoto);
            }
        }
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
        } else if(repeat_first_pin_edit_text.getText().length()==0){
            repeat_first_pin_edit_text.setText(button.getText());
        } else if(repeat_second_pin_edit_text.getText().length()==0){
            repeat_second_pin_edit_text.setText(button.getText());
        } else if(repeat_third_pin_edit_text.getText().length()==0){
            repeat_third_pin_edit_text.setText(button.getText());
        } else if(repeat_fourth_pin_edit_text.getText().length()==0){
            repeat_fourth_pin_edit_text.setText(button.getText());
        }
    }

    public void clickConfirmSetPinButton(View v) throws NoSuchAlgorithmException, SQLDataException {
        String pin = String.valueOf(first_pin_edit_text.getText()).
                concat(String.valueOf(second_pin_edit_text.getText())).
                concat(String.valueOf(third_pin_edit_text.getText())).
                concat(String.valueOf(fourth_pin_edit_text.getText()));
        String repeatPin = String.valueOf(repeat_first_pin_edit_text.getText()).
                concat(String.valueOf(repeat_second_pin_edit_text.getText())).
                concat(String.valueOf(repeat_third_pin_edit_text.getText())).
                concat(String.valueOf(repeat_fourth_pin_edit_text.getText()));

        if(pin.equals(repeatPin) && pin.length()==4) {

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
            databaseManager.open();
            if(selectedImage != null) databaseManager.insertUser(username,hexString.toString(),selectedImage.toString());
            else databaseManager.insertUser(username,hexString.toString(), String.valueOf(selectedImage));
            databaseManager.close();

            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("sessionToken", username);
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("isNotFirstRun", true);
            startActivity(intent);
            finish();

        } else if(pin.length()!=repeatPin.length() ||  pin.length() == 0){
            error_text_view.setVisibility(View.VISIBLE);
            error_text_view.setText("Nie podałeś wszystkich liczb");
        }else if (!pin.equals(repeatPin)) {
            error_text_view.setVisibility(View.VISIBLE);
            error_text_view.setText("Piny nie są takie same");
        }
    }

    public void clickDeletePinButton(View v){

        if(repeat_fourth_pin_edit_text.getText().length()!=0){
            repeat_fourth_pin_edit_text.setText("");
        } else if(repeat_third_pin_edit_text.getText().length()!=0){
            repeat_third_pin_edit_text.setText("");
        } else if(repeat_second_pin_edit_text.getText().length()!=0){
            repeat_second_pin_edit_text.setText("");
        } else if(repeat_first_pin_edit_text.getText().length()!=0){
            repeat_first_pin_edit_text.setText("");
        } else if(fourth_pin_edit_text.getText().length()!=0){
            fourth_pin_edit_text.setText("");
        } else if(third_pin_edit_text.getText().length()!=0){
            third_pin_edit_text.setText("");
        } else if(second_pin_edit_text.getText().length()!=0){
            second_pin_edit_text.setText("");
        } else if(first_pin_edit_text.getText().length()!=0){
            first_pin_edit_text.setText("");
        }
    }
}