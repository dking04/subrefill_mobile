package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;

import java.util.UUID;

public class AddFund extends AppCompatActivity {
    String currency = "NGN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fund);

        String amount = getIntent().getStringExtra("amount");
       WebView  mWebview = findViewById(R.id.webview);
        ProgressBar progressBar = findViewById(R.id.progress);
        SharedPreferences sharedPreferences = getSharedPreferences("loginDetail", Context.MODE_PRIVATE);
        String db = sharedPreferences.getString("db","");
        mWebview.loadUrl("http://www.google.com");

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient());
        mWebview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if(newProgress==100){
                    progressBar.setVisibility(View.GONE);
                    //mWebview.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

    }

    private void makePayment(String amount){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String fullname= sharedPreferences.getString("fullname","");
        String email = sharedPreferences.getString("email","");
        String phone = sharedPreferences.getString("phone","");

        String txRef = "DATRFL-"+  UUID.randomUUID().toString();

        new RaveUiManager(this).setAmount(Double.parseDouble(amount))
                .setCurrency(currency)
                .setEmail(email)
                .setfName("")
                .setlName("")
                .setNarration("Datarefill")
                .setPublicKey(getResources().getString(R.string.public_key))
                .setEncryptionKey(getResources().getString(R.string.encryption_key))
                .setTxRef(txRef)
                .setPhoneNumber(phone, false)
                .acceptAccountPayments(false)
                .acceptCardPayments(true)
                .acceptBankTransferPayments(false)
                .acceptUssdPayments(true)
                .allowSaveCardFeature(true)
                .onStagingEnv(false)
                .isPreAuth(true).shouldDisplayFee(false)
                .showStagingLabel(false)
                .withTheme(R.style.RaveDesignTheme)
                .initialize();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "Payment cancelled ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


}