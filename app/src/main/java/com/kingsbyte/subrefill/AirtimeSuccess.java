package com.kingsbyte.subrefill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AirtimeSuccess extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airtime_success);
        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        String phone = intent.getStringExtra("phone");
        String status = intent.getStringExtra("status");

        RelativeLayout circleBg = findViewById(R.id.circle);
        new MyAnimation(circleBg);
        LinearLayout closeBtn = findViewById(R.id.close);
        LinearLayout buyAgainBtn = findViewById(R.id.buy_again);
        TextView statusTxt = findViewById(R.id.status);
        TextView msgTxt = findViewById(R.id.message);
        try {
            status = status.substring(0, 1).toUpperCase() + "" + status.substring(1);
        }catch (Exception e){
        }
        statusTxt.setText(status);
        msgTxt.setText("NGN"+amount+" Airtime purchase on "+phone+" was successful");

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(AirtimeSuccess.this,Dashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(1,intent);
                startActivity(intent);*/
                new Dashboard().onRefreshBalance(AirtimeSuccess.this);
                finish();
            }
        });

        buyAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AirtimeSuccess.this,Airtime.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}