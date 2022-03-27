package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Tv extends AppCompatActivity {
    AutoCompleteTextView servicetypeATX,bouquetATX;
    EditText decoderEDT,phoneEDT;
    TextInputLayout serviceTypeTXT,bouquetTXT,decoderTXT,phoneTXT;
    String serviceType,bouquet,decoderNo,phone,cableId;
    static List<ServiceCompanyObject> list = new ArrayList<>();
    static List<String> companyNames = new ArrayList<>();
    ArrayAdapter adapter,adapter2;
     List<BounquetObject> bounquetList= new ArrayList<>();
     List<String> bounquetNames = new ArrayList<>();
     String reference="";
    BottomSheetDialog bottomSheetDialog;
    private static final int PERMISSION_REQUEST_CONTACT = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Recharge Tv");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        servicetypeATX = findViewById(R.id.service_type);
        bouquetATX = findViewById(R.id.bouquets);
        decoderEDT = findViewById(R.id.decoder_no);
        phoneEDT = findViewById(R.id.phone_no);
        serviceTypeTXT = findViewById(R.id.service_type_wrapper);
        bouquetTXT = findViewById(R.id.bouquets_wrapper);
        decoderTXT = findViewById(R.id.decoder_no_wrapper);
        phoneTXT = findViewById(R.id.phone_no_wrapper);
        if(companyNames.isEmpty()) {
            getServiceCompanies();
        }

        adapter =
                new ArrayAdapter<>(this,
                        R.layout.dropdown_text_item,
                        companyNames);
        servicetypeATX.setAdapter(adapter);
        servicetypeATX.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                serviceType = list.get(i).getServiceType();
                cableId = list.get(i).getId();
                getBonquets();

            }
        });

        adapter2 =
                new ArrayAdapter<>(this,
                        R.layout.dropdown_text_item,
                        bounquetNames);
        bouquetATX.setAdapter(adapter2);
        bouquetTXT.setHint("Select a Bouquet");
        bouquetATX.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String price = bounquetList.get(i).getPrice();
                String months = bounquetList.get(i).getMonthsPaidFor();
                String code = bounquetList.get(i).getCode();
                bouquet = price+"-"+code+"-"+months;
            }
        });

        Button rechargeBtn = findViewById(R.id.recharge);
        rechargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    verifyDecoderNo(cableId,decoderNo);
                }
            }
        });

        CardView contactBtn = findViewById(R.id.contact);
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContact();
            }
        });
    }
    private void getServiceCompanies(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");

        String url = "https://subrefill.com/api/user/cables";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response","response "+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray cableArr = jsonObject.getJSONArray("cables");
                    for (int a=0;a<cableArr.length();a++){
                        JSONObject obj = cableArr.getJSONObject(a);
                        String id = obj.getString("id");
                        String serviceType = obj.getString("servicetype");
                        String provider = obj.getString("provider");
                        ServiceCompanyObject s = new ServiceCompanyObject();
                        s.setId(id);
                        s.setProvider(provider);
                        s.setServiceType(serviceType);
                        list.add(s);
                        companyNames.add(serviceType);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                retryServiceDialog();
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
                Toast.makeText(Tv.this,message,Toast.LENGTH_SHORT).show();
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

    private void getBonquets(){
        bouquetTXT.setHint("Loading...");
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        String url = "https://subrefill.com/api/user/cable/bouquets/"+serviceType;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response","response "+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")){
                        bouquetTXT.setHint("Select a Bouquet");

                        bounquetNames.clear();
                        bounquetList.clear();
                        JSONArray dataArr = jsonObject.getJSONArray("data");
                        for (int a=0;a<dataArr.length();a++){
                            JSONObject obj = dataArr.getJSONObject(a);
                            String name = obj.getString("name");
                            String code = obj.getString("code");
                            JSONArray optionArr = obj.getJSONArray("availablePricingOptions");
                            for (int b=0;b<optionArr.length();b++){
                                JSONObject o = optionArr.getJSONObject(b);
                                String monthsPaidFor=o.getString("monthsPaidFor");
                                String price = o.getString("price");
                                String invoicePeriod = o.getString("invoicePeriod");
                                BounquetObject bounquent = new BounquetObject();
                                bounquent.setCode(code);
                                bounquent.setInvoicePeriod(invoicePeriod);
                                bounquent.setMonthsPaidFor(monthsPaidFor);
                                bounquent.setPrice(price);
                                bounquent.setName(name);
                                bounquetList.add(bounquent);
                                NumberFormat formatter= new DecimalFormat("#,###");
                                String p= formatter.format(Double.parseDouble(price));
                                String displayName = name+" "+monthsPaidFor+" Month - NGN "+p;
                                bounquetNames.add(displayName);
                            }
                        }
                        adapter2.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                bouquetTXT.setHint("");
                retryBouquetsDialog();
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
                Toast.makeText(Tv.this,message,Toast.LENGTH_SHORT).show();
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

    private boolean validate(){
        boolean valid=true;
        decoderNo = decoderEDT.getText().toString();
        phone = phoneEDT.getText().toString();
        serviceTypeTXT.setErrorEnabled(false);
        bouquetTXT.setErrorEnabled(false);
        decoderTXT.setErrorEnabled(false);
        phoneTXT.setErrorEnabled(false);

        if(decoderNo.isEmpty()){
            decoderTXT.setError("Decoder number is required");
            valid=false;
        }else if(serviceType.isEmpty()){
            serviceTypeTXT.setError("Select a service type");
            valid=false;
        }else if(bouquet.isEmpty()){
            bouquetTXT.setError("Select a bouquet");
            valid = false;
        }else if(!phone.isEmpty() && phone.length()!=11){
            phoneTXT.setError("Phone number must be 11 digits");
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

    private void verifyDecoderNo(String cableId,String smartCardNo){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        String url = "https://subrefill.com/api/user/cable/verify";
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
                        showBottomSheetDialog(customerName,decoderNo);

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
                            Toast.makeText(Tv.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Tv.this, info, Toast.LENGTH_SHORT).show();

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
                Toast.makeText(Tv.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("smartcard",smartCardNo);
                    jsonBody.put("servicetype",cableId);
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

    private void showBottomSheetDialog(String customerName,String decoderNo) {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tv_bottom_sheet_dialog_layout);
        TextView customerTX= bottomSheetDialog.findViewById(R.id.customer_name);
        TextView decoderTX = bottomSheetDialog.findViewById(R.id.decoder_no);
        Button rechargeBtn = bottomSheetDialog.findViewById(R.id.recharge_cont);
        customerTX.setText("Customer Name: "+customerName);
        decoderTX.setText("Smartcard Number: "+decoderNo);
        rechargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyTvSubscription();
            }
        });
        ImageView closeBtn = bottomSheetDialog.findViewById(R.id.close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();
    }

    private void buyTvSubscription(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        String url = "https://subrefill.com/api/user/cable/recharge/"+reference;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();
                Log.i("response","response "+response);
                try {
                    bottomSheetDialog.dismiss();
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("1")){
                        JSONObject obj = jsonObject.getJSONObject("transaction");
                        String transactionStatus = obj.getString("status");
                        String message = obj.getString("message");
                        Intent intent = new Intent(Tv.this,TvSuccess.class);
                        intent.putExtra("status",transactionStatus);
                        intent.putExtra("message",message);
                        startActivity(intent);

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
                            Toast.makeText(Tv.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Tv.this, info, Toast.LENGTH_SHORT).show();

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
                    CheckDialog dialog = new CheckDialog(Tv.this);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                Toast.makeText(Tv.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("smartcard",decoderNo);
                    jsonBody.put("serviceplan",serviceType);
                    jsonBody.put("phonenumber",phone);
                    jsonBody.put("cable",cableId);
                    jsonBody.put("bouquet",bouquet);
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(100000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    private void retryServiceDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you are connected to the internet and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getServiceCompanies();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void retryBouquetsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you are connected to the internet and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getBonquets();
            }
        });
        builder.setCancelable(false);
        builder.show();
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
                    phoneEDT.setText(phone);

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



}