package com.kingsbyte.subrefill;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Dashboard.setRefreshBalanceListener(this);
        RelativeLayout bg = findViewById(R.id.rootview);
        swipeRefreshLayout=findViewById(R.id.swipe);
        //new Particles(this,bg,R.layout.image_xml,20);
        CardView airtimeBtn = findViewById(R.id.airtime);
        CardView dataBtn = findViewById(R.id.data);
        CardView tvBtn = findViewById(R.id.cable_tv);
        CardView electricityBtn = findViewById(R.id.electricity);
        CardView contactUsBtn = findViewById(R.id.contact_us);
        CardView logoutBtn = findViewById(R.id.logout);
        CardView addFundBtn = findViewById(R.id.fund);
        balanceText =  findViewById(R.id.balance);
        CardView transactionBtn = findViewById(R.id.history);
        CardView settingBtn = findViewById(R.id.settings);
        TextView fullnameTXT = findViewById(R.id.fullname);
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String balance = sharedPreferences.getString("balance","0");
        String fullname = sharedPreferences.getString("fullname","");
        fullnameTXT.setText("Hi, "+fullname);
        NumberFormat formatter= new DecimalFormat("#,###");
        balance= formatter.format(Double.parseDouble(balance));
        balanceText.setText("NGN "+balance);

        addFundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFundDialog dialog = new AddFundDialog(Dashboard.this);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        airtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Airtime.class);
                startActivity(intent);
            }
        });

        dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Data.class);
                startActivity(intent);
            }
        });

        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Tv.class);
                startActivity(intent);
            }
        });

        electricityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Electricity.class);
                startActivity(intent);
            }
        });

        contactUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactUsDialog dialog = new ContactUsDialog(Dashboard.this);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
                builder.setMessage("Are you sure?");
                builder.setTitle("Logout");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Intent intent = new Intent(Dashboard.this,Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
               builder.show();

            }
        });

        transactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Transactions.class);
                startActivity(intent);
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this,Settings.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                login(Dashboard.this);
            }
        });

        //login2(this);

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
        Dashboard.swipeRefreshLayout.setRefreshing(true);

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


}