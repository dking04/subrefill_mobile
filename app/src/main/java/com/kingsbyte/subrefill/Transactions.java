package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Transactions extends AppCompatActivity {
    private List<TransactionObject> list;
    TransactionAdapter adapter;
    private int currentPage=1;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Transactions");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        RecyclerView recyclerView = findViewById(R.id.transactions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();
        adapter = new TransactionAdapter(this,list);
        recyclerView.setAdapter(adapter);
        getTransactions();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    currentPage++;
                    getTransactions();

                    // Do something

                    loading = true;
                }
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



    private void getTransactions(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        String userId = sharedPreferences.getString("userID","");
        Loader loader = new Loader(this);
        if(currentPage==1) {
            loader.setCancelable(false);
            loader.setCanceledOnTouchOutside(false);
            loader.show();
        }
        String url = "https://subrefill.com/api/user/transactions/"+userId+"?page="+currentPage;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();
                Log.i("response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("1")){
                        JSONObject transactionObj = jsonObject.getJSONObject("transactions");
                        JSONArray dataARR = transactionObj.getJSONArray("data");
                        for (int a=0;a<dataARR.length();a++){
                            JSONObject obj = dataARR.getJSONObject(a);
                            String id = obj.getString("id");
                            String type = obj.getString("type");
                            String accountNumber = obj.getString("accountnumber");
                            String tokenCode = obj.getString("tokencode");
                            String reference = obj.getString("reference");
                            String transactionStatus = obj.getString("status");
                            String phone = obj.getString("phone");
                            String amount = obj.getString("amount");
                            String date = obj.getString("created_at");
                            TransactionObject t = new TransactionObject();
                            t.setTransactionType(type);
                            t.setStatus(transactionStatus);
                            t.setPrice(amount);
                            t.setDate(date);
                            t.setPhone(phone);
                            t.setTokenCode(tokenCode);
                            t.setAccountNumber(accountNumber);
                            list.add(t);
                        }
                        adapter.notifyDataSetChanged();

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
                            Toast.makeText(Transactions.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Transactions.this, info, Toast.LENGTH_SHORT).show();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
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
                Toast.makeText(Transactions.this,message,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=UTF-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("meternumber","");
                    String requestBody = jsonBody.toString();
                    return requestBody.getBytes("utf-8");
                }catch (UnsupportedEncodingException | JSONException e){
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization","Bearer "+token);
                headers.put("Accept","application/json");
                return headers;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}