package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Electricity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CONTACT = 12;
    private EditText recipient,meterEDT,amountEDT;
    static List<ElectricityObject> list=new ArrayList<>();
    static List<String> discoNames=new ArrayList<>();
    ArrayAdapter<String> adapter;
    String discoId,recipientPhone,meterNo,amount;
    TextInputLayout recipientTXT,meterNoTXT,amountTxt,discoTXT;
    AutoCompleteTextView discos;
    String reference="";
    BottomSheetDialog bottomSheetDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electricity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Buy Electricity");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        recipient = findViewById(R.id.recipient);
        meterEDT = findViewById(R.id.meter_no);
        amountEDT = findViewById(R.id.amount);
        recipientTXT = findViewById(R.id.recipient_wrapper);
        meterNoTXT = findViewById(R.id.meter_no_wrapper);
        amountTxt = findViewById(R.id.amountWrapper);
        discoTXT = findViewById(R.id.disco_wrapper);
        if(discoNames.isEmpty()) {
            getDiscos();
        }


         adapter =
                new ArrayAdapter<>(this,
                        R.layout.dropdown_text_item,
                        discoNames);

         discos =
                findViewById(R.id.discos);
        discos.setAdapter(adapter);
        discos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                discoId = list.get(i).getId();
            }
        });

        CardView contactBtn = findViewById(R.id.contact);
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContact();
            }
        });
        Button verifyBtn = findViewById(R.id.verify);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput()){
                    verifyMeter();
                }
            }
        });
    }

    private void getDiscos(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");

        String url = "https://subrefill.com/api/user/electricity/discos";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response","response "+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray electricityArr = jsonObject.getJSONArray("electricity");
                    for (int a=0;a<electricityArr.length();a++){
                        JSONObject obj = electricityArr.getJSONObject(a);
                        String id = obj.getString("id");
                        String serviceType = obj.getString("servicetype");
                        String serviceplan = obj.getString("serviceplan");
                        String fullname = obj.getString("fullname");
                        String shortCode = obj.getString("shortcode");
                        ElectricityObject e = new ElectricityObject();
                        e.setFullname(fullname);
                        e.setShortCode(shortCode);
                        e.setServiceType(serviceType);
                        e.setServicePlan(serviceplan);
                        e.setId(id);
                        list.add(e);
                        discoNames.add(shortCode+" "+serviceplan);

                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                retryDiscoDialog();
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
                Toast.makeText(Electricity.this,message,Toast.LENGTH_SHORT).show();
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

    private boolean validateInput(){
        boolean valid = true;
        amount = amountEDT.getText().toString();
        meterNo = meterEDT.getText().toString();
        recipientPhone = recipient.getText().toString();
        amountTxt.setErrorEnabled(false);
        meterNoTXT.setErrorEnabled(false);
        recipientTXT.setErrorEnabled(false);
        if(meterNo.isEmpty()){
            meterNoTXT.setError("Meter Number is required");
            valid=false;
        }else if(discoId.isEmpty()){
            discoTXT.setError("Select a Service type");
            valid=false;
        }else if(amount.isEmpty()){
            amountTxt.setError("Amount is required");
            valid=false;
        }else if(Integer.parseInt(amount)<1000){
            amountTxt.setError("Minimum amount is NGN 1000");
            valid=false;
        }
        else if(recipientPhone.isEmpty()){
            recipientTXT.setError("Recipient phone number is required");
            valid=false;
        }else if(recipientPhone.length()!=11){
            recipientTXT.setError("Recipient phone number must be 11 digits");
            valid=false;
        }
        return valid;
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

    public void askForContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else{
                getContact();
            }
        }
        else{
            getContact();
        }
    }

    private void getContact(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            Cursor cursor = null;
            try {
                Uri uri = data.getData();
                cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    phone = phone.replace(" ","");
                    recipient.setText(phone);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST_CONTACT){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContact();
            }
        }
    }

    private void showBottomSheetDialog(String customerName,String meterNo) {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.electricity_bottom_sheet_dialog_layout);
        TextView customerTX= bottomSheetDialog.findViewById(R.id.customer_name);
        TextView decoderTX = bottomSheetDialog.findViewById(R.id.meter_no);
        Button continueBtn = bottomSheetDialog.findViewById(R.id.recharge_cont);
        customerTX.setText("Customer Name: "+customerName);
        decoderTX.setText("Meter Number: "+meterNo);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyElectricity();
            }
        });
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ImageView closeBtn = bottomSheetDialog.findViewById(R.id.close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    private void verifyMeter(){
            SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token","");
            Loader loader = new Loader(this);
            loader.setCancelable(false);
            loader.setCanceledOnTouchOutside(false);
            loader.show();
            String url = "https://subrefill.com/api/user/meternumber/verify";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    loader.dismiss();
                    Log.i("response","response "+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("1")){
                            String customerName = jsonObject.getString("customername");
                            reference = jsonObject.getString("reference");
                            showBottomSheetDialog(customerName,meterNo);

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
                                Toast.makeText(Electricity.this, msg, Toast.LENGTH_SHORT).show();

                            }else if(info!=null){
                                Toast.makeText(Electricity.this, info, Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(Electricity.this,message,Toast.LENGTH_SHORT).show();
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
                        jsonBody.put("meternumber",meterNo);
                        jsonBody.put("company",discoId);
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

    private void buyElectricity(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        String url = "https://subrefill.com/api/user/electricity/buy/"+reference;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();
                try {
                    bottomSheetDialog.dismiss();
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("1")){
                        JSONObject obj = jsonObject.getJSONObject("transaction");
                        String transactionStatus = obj.getString("status");
                        String type = obj.getString("type");
                        String accountNumber = obj.getString("accountnumber");
                        String serviceType = obj.getString("servicetype");
                        String tokencode = obj.getString("tokencode");
                        Intent intent = new Intent(Electricity.this,ElectricitySuccess.class);
                        intent.putExtra("token",tokencode);
                        intent.putExtra("status",transactionStatus);
                        startActivity(intent);
                        finish();
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
                            Toast.makeText(Electricity.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Electricity.this, info, Toast.LENGTH_SHORT).show();

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
                    CheckDialog dialog = new CheckDialog(Electricity.this);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                Toast.makeText(Electricity.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("meternumber",meterNo);
                    jsonBody.put("company",discoId);
                    jsonBody.put("phonenumber",recipientPhone);
                    jsonBody.put("billamount",amount);
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
                params.put("reference",reference);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(100000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    private void retryDiscoDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you are connected to the internet and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDiscos();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }


}