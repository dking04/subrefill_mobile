package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Register extends AppCompatActivity {
    String firstname,lastname,email,phone,password,password2;
    TextInputLayout firstnameTx,lastnameTx,emailTx,phoneTx,passwordTx,password2Tx;
    EditText firstnameEDT,lastnameEDT,emailEDT,phoneEDT,passwordEDT,password2EDT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        firstnameTx = findViewById(R.id.firstname_tx);
        firstnameEDT = findViewById(R.id.firstname);
        lastnameTx = findViewById(R.id.lastname_tx);
        lastnameEDT = findViewById(R.id.lastname);
        emailTx = findViewById(R.id.email_tx);
        emailEDT = findViewById(R.id.email);
        phoneTx = findViewById(R.id.phone_no_tx);
        phoneEDT = findViewById(R.id.phone_no);
        password2Tx = findViewById(R.id.confirm_password_tx);
        password2EDT = findViewById(R.id.confirm_password);
        passwordTx = findViewById(R.id.password_tx);
        passwordEDT = findViewById(R.id.password);


        Button registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateInput()) {
                    registerUser();
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

    private boolean validateInput(){
        boolean valid  = true;
        firstname = firstnameEDT.getText().toString();
        lastname = lastnameEDT.getText().toString();
        email = emailEDT.getText().toString();
        phone = phoneEDT.getText().toString();
        password = passwordEDT.getText().toString();
        password2 = password2EDT.getText().toString();

        if(firstname.isEmpty()){
            firstnameTx.setError("Firstname is required");
            valid=false;
        }else if(lastname.isEmpty()){
            lastnameTx.setError("Lastname is required");
            valid = false;
        }else if(email.isEmpty()){
            emailTx.setError("Email is required");
            valid = false;
        }else if(!isValid(email)){
            emailTx.setError("Email is not valid");
            valid = false;
        }

        else if(phone.isEmpty()){
            phoneTx.setError("Phone number is required");
            valid = false;
        }else if(phone.length()!=11){
            phoneTx.setError("Phone number must be 11 digits");
            valid=false;
        }
        else if(password.isEmpty()){
            passwordTx.setError("Password is required");
            valid = false;

        }else if(!password.equals(password2)){
            passwordTx.setError("Password doesn't match");
            valid = false;
        }
        return valid;
    }
    private boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void registerUser(){
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        loader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        String url = "https://subrefill.com/api/signup";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response","r "+response);
                loader.dismiss();
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("0")){
                        JSONObject errorObj = object.getJSONObject("error");
                        Iterator<String> iterator = errorObj.keys();
                        String msg = "";
                        while (iterator.hasNext()){
                            String key = iterator.next();
                            JSONArray arr = errorObj.getJSONArray(key);
                            String message = arr.getString(0);
                            msg+=message+"..";
                        }
                        Toast.makeText(Register.this,msg,Toast.LENGTH_SHORT).show();
                    }else if(status.equals("1")){
                        String message = "A verification email has been sent to "+email;
                        RegisterSuccessDialog dialog = new RegisterSuccessDialog(Register.this,message,email,"1");
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        Window window = dialog.getWindow();
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                Toast.makeText(Register.this,message,Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("firstname",firstname);
                params.put("lastname",lastname);
                params.put("email",email);
                params.put("phone",phone);
                params.put("password",password);
                params.put("confirmpassword",password2);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }
}