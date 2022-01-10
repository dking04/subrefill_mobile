package com.kingsbyte.subrefill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class Splashscreen extends AppCompatActivity {
    static final int DELAY=3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
                String remember = sharedPreferences.getString("remember","0");
                if(remember.equals("0")) {
                    Intent intent = new Intent(Splashscreen.this, Login.class);
                    startActivity(intent);
                    finish();
                }else if(remember.equals("1")) {
                    Intent intent = new Intent(Splashscreen.this, Dashboard.class);
                    startActivity(intent);
                    finish();
                }
            }
        },DELAY);
    }
}