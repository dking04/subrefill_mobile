package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String fullname = sharedPreferences.getString("fullname","");
        String email = sharedPreferences.getString("email","");
        String phone = sharedPreferences.getString("phone","");
        String[] nameARR = fullname.split(" ");
        String name1 = nameARR[0];
        String name2 = nameARR[1];
        name1 = name1.substring(0,1).toUpperCase()+""+name1.substring(1);
        name2 = name2.substring(0,1).toUpperCase()+""+name2.substring(1);
        email = email.substring(0,1).toUpperCase()+""+email.substring(1);
        TextView fullnameTXT = findViewById(R.id.fullname);
        TextView emailTXT= findViewById(R.id.email);
        TextView phoneTXT = findViewById(R.id.phone);

        fullnameTXT.setText(name1+" "+name2);
        emailTXT.setText(email);
        phoneTXT.setText(phone);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }
}