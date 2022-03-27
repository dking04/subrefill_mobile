package com.kingsbyte.subrefill;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Dashboard extends AppCompatActivity implements RefreshBalanceListener {
    String currency = "NGN";
    static SwipeRefreshLayout swipeRefreshLayout;
    static TextView balanceText;
    private static RefreshBalanceListener refreshBalanceListener;
    Fragment fragment =null;
    Fragment fragment1 = new Home();
    Fragment fragment2 = new HistoryFragment();
    Fragment fragment3 = new Support();
    Fragment fragment4 = new SettingsFragment();
    Fragment active =fragment1;
    FragmentManager fragmentManager = getSupportFragmentManager();
    TextView homeBtn,historyBtn,settingBtn,supportBtn;
    TextView[] txtArr = new TextView[4];
    int selectedId = R.id.home;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        fragmentManager.beginTransaction().add(R.id.fragment_container,fragment3).hide(fragment3).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container,fragment4).hide(fragment4).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container,fragment2).hide(fragment2).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container,fragment1).commit();

         homeBtn = findViewById(R.id.home);
        historyBtn = findViewById(R.id.history);
         settingBtn = findViewById(R.id.settings);
        supportBtn = findViewById(R.id.support);
        txtArr[0]=homeBtn;
        txtArr[1]=historyBtn;
        txtArr[2]=settingBtn;
        txtArr[3]=supportBtn;


        setTextViewDrawableColor(homeBtn,getResources().getColor(R.color.voilet));

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().hide(active).show(fragment1).commit();
                active = fragment1;
                resetView(getResources().getColor(R.color.black));
                setTextViewDrawableColor(homeBtn,getResources().getColor(R.color.voilet));
                selectedId = R.id.home;

            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().hide(active).show(fragment2).commit();
                active = fragment2;
                resetView(getResources().getColor(R.color.black));
                setTextViewDrawableColor(historyBtn,getResources().getColor(R.color.voilet));
                selectedId = R.id.history;

            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().hide(active).show(fragment4).commit();
                active = fragment4;
                resetView(getResources().getColor(R.color.black));
                setTextViewDrawableColor(settingBtn,getResources().getColor(R.color.voilet));
                 selectedId = R.id.settings;

            }
        });

        supportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().hide(active).show(fragment3).commit();
                active = fragment3;
                resetView(getResources().getColor(R.color.black));
                setTextViewDrawableColor(supportBtn,getResources().getColor(R.color.voilet));
                selectedId = R.id.support;

            }
        });

        FloatingActionButton addFundBtn = findViewById(R.id.fab);
        addFundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Dashboard.this);
                bottomSheetDialog.setContentView(R.layout.fund_wallet_bottomsheet);
                ImageView closeBtn = bottomSheetDialog.findViewById(R.id.close);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setCancelable(true);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")) {
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        String id = dataObject.getString("id");
                        String transactionRef = dataObject.getString("txRef");
                        String orderRef = dataObject.getString("orderRef");
                        String flwRef = dataObject.getString("flwRef");
                        String transactionStatus = dataObject.getString("status");
                        String amount = dataObject.getString("amount");
                        completePayment(id,transactionRef,status,amount);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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


    private void completePayment(String transactionId,String reference,String transactionStatus,String amount){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        loader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        String url = "https://subrefill.com/api/user/payment/verify";
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("1")){
                        login(Dashboard.this);

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
                            Toast.makeText(Dashboard.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Dashboard.this, info, Toast.LENGTH_SHORT).show();

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
                Toast.makeText(Dashboard.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("transaction_id",transactionId);
                    jsonBody.put("status",transactionStatus);
                    jsonBody.put("reference",reference);
                    jsonBody.put("amount",amount);
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void forceUpdate(){
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo =  packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String currentVersion = packageInfo.versionName;
        new ForceUpdate(currentVersion,Dashboard.this).execute();
    }

    private void login(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email","");
        String password = sharedPreferences.getString("password","");
        //Dashboard.swipeRefreshLayout.setRefreshing(true);

        String url = "https://subrefill.com/api/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(Dashboard.swipeRefreshLayout!=null) {
                    Dashboard.swipeRefreshLayout.setRefreshing(false);
                }
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("0")){
                        JSONObject errorObj = object.optJSONObject("error");
                        String info = object.optString("info");
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

                        }
                    }else if(status.equals("1")){
                        JSONObject userObject = object.getJSONObject("user");
                        JSONObject accountObj = object.getJSONObject("account");
                        String balance = accountObj.getString("balance");
                        SharedPreferences sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("balance",balance);
                        editor.apply();
                        NumberFormat formatter= new DecimalFormat("#,###");
                        balance= formatter.format(Double.parseDouble(balance));
                        //Dashboard.balanceText.setText("NGN "+balance);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                Dashboard.swipeRefreshLayout.setRefreshing(false);
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
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email",email);
                params.put("password",password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }



    @Override
    protected void onResume() {
        super.onResume();
        forceUpdate();
        login(this);
    }

    public static void setRefreshBalanceListener(RefreshBalanceListener refreshBalanceListener1){
        refreshBalanceListener=refreshBalanceListener1;
    }


    @Override
    public void onRefreshBalance(Context context) {
       login(context);
    }


    private void login2(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email","");
        String password = sharedPreferences.getString("password","");

        String url = "https://subrefill.com/api/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("0")){
                        JSONObject errorObj = object.optJSONObject("error");
                        String info = object.optString("info");
                        if(errorObj!=null) {
                            Iterator<String> iterator = errorObj.keys();
                            String msg = "";
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                JSONArray arr = errorObj.getJSONArray(key);
                                String message = arr.getString(0);
                                msg += message + "..";
                            }

                        }else if(info!=null){

                        }
                    }else if(status.equals("1")){
                        JSONObject userObject = object.getJSONObject("user");
                        JSONObject accountObj = object.getJSONObject("account");
                        String balance = accountObj.getString("balance");
                        SharedPreferences sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("balance",balance);
                        editor.apply();
                        NumberFormat formatter= new DecimalFormat("#,###");
                        balance= formatter.format(Double.parseDouble(balance));
                        Dashboard.balanceText.setText("NGN "+balance);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                Dashboard.swipeRefreshLayout.setRefreshing(false);
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
                //Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email",email);
                params.put("password",password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        textView.setTextColor(color);
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }
    private void resetView(int color){
        for (int a=0;a<txtArr.length;a++){
            TextView textView = txtArr[a];
            textView.setTextColor(color);
            for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(selectedId!=R.id.home){
            fragmentManager.beginTransaction().hide(active).show(fragment1).commit();
            active = fragment1;
            resetView(getResources().getColor(R.color.black));
            setTextViewDrawableColor(homeBtn,getResources().getColor(R.color.voilet));
            selectedId = R.id.home;
        }else {
            super.onBackPressed();
        }
    }
}