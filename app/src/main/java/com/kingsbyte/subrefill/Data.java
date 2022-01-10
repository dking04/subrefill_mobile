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

public class Data extends AppCompatActivity {
    AutoCompleteTextView networkSelect,dataplanSelect;
    EditText phoneEDT;
    TextInputLayout networkSelectTX,dataPlanSelectTX,phoneTX;
    String network,dataplan,phone,dataName;
    private static final int PERMISSION_REQUEST_CONTACT = 12;
    static List<Networks> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    static List<String> networkList = new ArrayList<>();
    static List<DataObject> dataList = new ArrayList<>();
    List<String> dataNameList = new ArrayList<>();
    List<DataObject> selectedDataList = new ArrayList<>();
    ArrayAdapter adapter1;
    List<DataObject> dl = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Buy Data");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        phoneEDT = findViewById(R.id.phone_no);
        networkSelectTX = findViewById(R.id.networks_wrapper);
        dataPlanSelectTX = findViewById(R.id.data_plans_wrapper);
        phoneTX = findViewById(R.id.phone_no_wrapper);

       if(networkList.isEmpty()){
           getNetworks();
       }

         adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_text_item,
                        networkList);

        networkSelect =
                findViewById(R.id.networks);
        networkSelect.setAdapter(adapter);
        networkSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                network = list.get(i).getId();
                if(dataList.isEmpty()) {
                    getBundles();
                }else{
                    dl = filterDataList(network,dataList);
                }
            }
        });


        adapter1 = new ArrayAdapter<>(this, R.layout.dropdown_text_item, dataNameList);

        dataplanSelect =
                findViewById(R.id.data_plans);
        dataplanSelect.setHint("Select Data Plan");

        dataplanSelect.setAdapter(adapter1);
        dataplanSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dataplanId = dl.get(i).getId();
                String price = dl.get(i).getPlanAmount();
                String networkName = dl.get(i).getNetworkName();
                String networkId = dl.get(i).getPlanNetwork();
                String api = dl.get(i).getApi();
                dataplan = dataplanId+"-"+price+"-"+networkName+"-"+api+"-"+networkId;
                Log.i("response","dataplan "+dataplan);
                dataName = dl.get(i).getPlanName();
            }
        });

        Button buyDataBtn = findViewById(R.id.buyData);
        buyDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput()){
                    buyData();
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

    private void getBundles(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        /*Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();*/
        dataplanSelect.setHint("Loading...");
        String url = "https://subrefill.com/api/user/data/bundles";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dataplanSelect.setHint("Select Data Plan");
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray priceARR = object.getJSONArray("pricing");
                    for (int a=0;a<priceARR.length();a++){
                        JSONObject obj = priceARR.getJSONObject(a);
                        String id = obj.getString("id");
                        String bundle = obj.getString("bundle");
                        //String dataCode = obj.getString("datacode");
                        String price = obj.getString("price");
                        String networkId = obj.getString("networkid");
                        String networkName = obj.getString("networkname");
                        String api = obj.getString("api");
                        DataObject d = new DataObject();
                        d.setId(id);
                        d.setPlanName(bundle+" - "+"N"+price);
                        d.setPlanAmount(price);
                        d.setPlanNetwork(networkId);
                        //d.setPlanSize(dataCode);
                        d.setApi(api);
                        d.setNetworkName(networkName);
                        dataList.add(d);
                    }
                    dl=filterDataList(network,dataList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dataplanSelect.setHint("");
                retryBundleDialog();
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
                Toast.makeText(Data.this,message,Toast.LENGTH_SHORT).show();
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
        boolean valid=true;
        phone = phoneEDT.getText().toString();
        networkSelectTX.setErrorEnabled(false);
        dataPlanSelectTX.setErrorEnabled(false);
        phoneTX.setErrorEnabled(false);
        if(network.isEmpty()){
            networkSelectTX.setError("Select a network");
            valid = false;
        }else if(dataplan.isEmpty()){
            dataPlanSelectTX.setError("Select a data plan");
            valid = false;
        }else if(phone.isEmpty()){
            phoneTX.setError("Phone number is required");
            valid = false;
        }else if(phone.length()!=11){
            phoneTX.setError("Phone number must be 11 digits");
            valid = false;
        }
        return valid;
    }

    private void buyData(){
        Log.i("response","dataplan "+dataplan);
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        String url = "https://subrefill.com/api/user/data/buy";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("1")){
                        JSONObject transactionObj = jsonObject.getJSONObject("transaction");
                        String type = transactionObj.getString("type");
                        String transactionStatus = transactionObj.getString("status");
                        String servicePlan = transactionObj.getString("serviceplan");
                        String phone = transactionObj.getString("phone");
                        String amount = transactionObj.getString("amount");
                        Intent intent = new Intent(Data.this,DataSuccess.class);
                        intent.putExtra("status",transactionStatus);
                        intent.putExtra("phone",phone);
                        intent.putExtra("amount",amount);
                        intent.putExtra("dataname",dataName);
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
                            Toast.makeText(Data.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(Data.this, info, Toast.LENGTH_SHORT).show();

                        }                    }
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
                    CheckDialog dialog = new CheckDialog(Data.this);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                Toast.makeText(Data.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("dataphone",phone);
                    jsonBody.put("datanetwork",network);
                    jsonBody.put("dataplan",dataplan);
                    String requestBody = jsonBody.toString();
                    return requestBody.getBytes("utf-8");
                }catch (UnsupportedEncodingException | JSONException e){
                    return null;
                }
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization","Bearer "+token);
                params.put("Accept","application/json");

                return params;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token",token);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

    private void getNetworks(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");


        String url = "https://subrefill.com/api/user/data/networks";
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray networkArr = jsonObject.getJSONArray("networks");
                    for (int a = 0; a < networkArr.length(); a++) {
                        JSONObject obj = networkArr.getJSONObject(a);
                        String id = obj.getString("id");
                        String network = obj.getString("networkname");
                        String networkCode = obj.getString("networkcode");
                        Networks n = new Networks();
                        n.setId(id);
                        n.setNetworkCode(networkCode);
                        n.setNetworkName(network);
                        networkList.add(network);
                        list.add(n);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                retryNetworkDialog();
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
                Toast.makeText(Data.this, message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization","Bearer "+token);
                params.put("Accept","application/json");

                return params;
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

    private List<DataObject> filterDataList(String networkId,List<DataObject> list){
        List<DataObject> l = new ArrayList<>();
        dataNameList.clear();
        for (int a=0;a<list.size();a++){
            String id = list.get(a).getPlanNetwork();
            if(id.equals(networkId)){
                String dataId = list.get(a).getId();
                String bundle = list.get(a).getPlanName();
                String network = list.get(a).getPlanNetwork();
                String price = list.get(a).getPlanAmount();
                String code = list.get(a).getPlanSize();
                String api = list.get(a).getApi();
                String networkName = list.get(a).getNetworkName();
                DataObject object = new DataObject();
                object.setId(dataId);
                object.setPlanSize(code);
                object.setPlanNetwork(network);
                object.setPlanAmount(price);
                object.setPlanName(bundle);
                object.setNetworkName(networkName);
                object.setApi(api);
                l.add(object);
                dataNameList.add(bundle);
            }
        }
        adapter1.notifyDataSetChanged();
        return l;

    }

    private void retryNetworkDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you are connected to the internet and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getNetworks();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void retryBundleDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you are connected to the internet and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getBundles();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

}