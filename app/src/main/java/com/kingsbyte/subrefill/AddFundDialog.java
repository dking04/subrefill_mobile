package com.kingsbyte.subrefill;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flutterwave.raveandroid.RaveUiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;

public class AddFundDialog extends Dialog {
    Activity context;
    String amount;
    String currency = "NGN";

    public AddFundDialog(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fund_dialog_layout);
        Button addBtn = findViewById(R.id.add);
        ImageView closeBtn = findViewById(R.id.close);
        EditText amountEDT = findViewById(R.id.amount);
        amountEDT.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        },500);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(amountEDT.getWindowToken(),0);
                dismiss();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = amountEDT.getText().toString();
                if(amount.isEmpty()){
                    Toast.makeText(getContext(),"Amount is empty",Toast.LENGTH_SHORT).show();
                }else {
                    InputMethodManager imm= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(amountEDT.getWindowToken(),0);
                    dismiss();
                  initilizePayment(amount);

                }
            }
        });
    }

    private void makePayment(String amount,String reference){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String fullname= sharedPreferences.getString("fullname","");
        String email = sharedPreferences.getString("email","");
        String phone = sharedPreferences.getString("phone","");


        new RaveUiManager(context).setAmount(Double.parseDouble(amount))
                .setCurrency(currency)
                .setEmail(email)
                .setfName(fullname)
                .setlName("")
                .setNarration("Kingsbyte Technologies Limited")
                .setPublicKey(getContext().getResources().getString(R.string.public_key))
                .setEncryptionKey(getContext().getResources().getString(R.string.encryption_key))
                .setTxRef(reference)
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


    private void initilizePayment(String amount){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(context);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        loader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        String url = "https://subrefill.com/api/user/payment/initialize";
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();
                Log.i("response", "response " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("1")){
                        JSONObject transactionObj = jsonObject.getJSONObject("transaction");
                        String reference = transactionObj.getString("reference");
                        makePayment(amount,reference);

                    }else if(status.equals("0")){
                        JSONObject errorObj = jsonObject.optJSONObject("error");
                        String info = jsonObject.optString("info");
                        if(errorObj!=null) {
                            Iterator<String> iterator = errorObj.keys();
                            String msg = "";
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                JSONArray arr = errorObj.getJSONArray(key);
                                String message = arr.getString(0);
                                msg += message + "..";
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(context, info, Toast.LENGTH_SHORT).show();

                        }                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                loader.dismiss();
                String message = null;
                if (volleyError instanceof NetworkError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again after some time!!";
                } else if (volleyError instanceof AuthFailureError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof ParseError) {
                    message = "Parsing error! Please try again after some time!!";
                } else if (volleyError instanceof NoConnectionError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof TimeoutError) {
                    message = "Connection TimeOut! Please check your internet connection.";
                }
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization","Bearer "+token);
                headers.put("Accept","application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=UTF-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("fundamount",amount);
                    String requestBody = jsonBody.toString();
                    return requestBody.getBytes("utf-8");
                }catch (UnsupportedEncodingException | JSONException e){
                    return null;
                }
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

}
