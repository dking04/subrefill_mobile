package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class Utility extends AppCompatActivity {
    String mtnAirtimeCode = "*556#";
    String airtelAirtimeCode = "*123#";
    String gloAirtimeCode = "*124#";
    String nMobileAirtimeCode="*232#";
    String mtnDataCode = "*461*4#";
    String airtelDataCode="*140#";
    String gloDataCode="*127*0#";
    String nMobileDataCode="*228#";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Utility");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        CardView mtnAirtimeBtn = findViewById(R.id.mtn_airtime);
        CardView gloAirtimeBtn = findViewById(R.id.glo_airtime);
        CardView airtelAirtimeBtn = findViewById(R.id.airtel_airtime);
        CardView nineAirtimeBtn = findViewById(R.id.nine_mobile_airtime);
        CardView mtnDataBtn = findViewById(R.id.mtn_data);
        CardView gloDataBtn = findViewById(R.id.glo_data);
        CardView airtelDataBtn = findViewById(R.id.airtel_data);
        CardView nineDataBtn = findViewById(R.id.nine_mobile_data);

        mtnAirtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(mtnAirtimeCode));
                startActivity(intent);
            }
        });

        gloAirtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(gloAirtimeCode));
                startActivity(intent);
            }
        });

        airtelAirtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(airtelAirtimeCode));
                startActivity(intent);
            }
        });

        nineAirtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(nMobileAirtimeCode));
                startActivity(intent);
            }
        });
        mtnDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(mtnDataCode));
                startActivity(intent);
            }
        });

        gloDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(gloDataCode));
                startActivity(intent);
            }
        });

        airtelDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(airtelDataCode));
                startActivity(intent);
            }
        });

        nineDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(ussdToCallableUri(nMobileDataCode));
                startActivity(intent);
            }
        });


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

    private Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if(!ussd.startsWith("tel:"))
            uriString += "tel:";

        for(char c : ussd.toCharArray()) {

            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }
}