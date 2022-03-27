package com.kingsbyte.subrefill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {
    EditText oldPasswordEDT,newPasswordEDT,confirmPasswordEDT;
    TextInputLayout oldPasswordTIL,newPasswordTIL,confirmPasswordTIL;
    String currentpassword,newpassword,password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Password");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        TextView fullnameTXT = findViewById(R.id.fullname);
        TextView emailTXT= findViewById(R.id.email);
        TextView phoneTXT = findViewById(R.id.phone);
        oldPasswordEDT = findViewById(R.id.old_password);
        newPasswordEDT = findViewById(R.id.new_password);
        confirmPasswordEDT = findViewById(R.id.confirm_password);
        oldPasswordTIL = findViewById(R.id.old_password_wrapper);
        newPasswordTIL = findViewById(R.id.new_password_wrapper);
        confirmPasswordTIL = findViewById(R.id.confirm_password_wrapper);
        Button changePasswordBtn = findViewById(R.id.change_password);

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    changePassword(currentpassword,newpassword,password2);
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

    private boolean validate(){
        currentpassword = oldPasswordEDT.getText().toString();
        newpassword = newPasswordEDT.getText().toString();
        password2= confirmPasswordEDT.getText().toString();
        boolean valid = true;

        oldPasswordTIL.setErrorEnabled(false);
        newPasswordTIL.setErrorEnabled(false);
        confirmPasswordTIL.setErrorEnabled(false);

        if(currentpassword.isEmpty()){
            valid=false;
            oldPasswordTIL.setError("Current Password is required");
        }else if(newpassword.isEmpty()){
            valid=false;
            newPasswordTIL.setError("New password is required");
        }else if(!password2.equals(newpassword)){
            valid = false;
            confirmPasswordTIL.setError("Password doesn't match");
        }

        return valid;
    }

    private void changePassword(String currentPassword,String newpassword,String password2){
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        Loader loader = new Loader(this);
        loader.setCancelable(false);
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        loader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        String url = "https://subrefill.com/api/user/password/update";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loader.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("1")){
                        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("password",newpassword);
                        editor.apply();

                        AlertDialog.Builder builder = new AlertDialog.Builder(  ChangePassword.this);
                        builder.setMessage("Password Changed Successfully");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                oldPasswordEDT.setText("");
                                newPasswordEDT.setText("");
                                confirmPasswordEDT.setText("");
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();


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
                            Toast.makeText(ChangePassword.this, msg, Toast.LENGTH_SHORT).show();

                        }else if(info!=null){
                            Toast.makeText(ChangePassword.this, info, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChangePassword.this,message,Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("password",currentPassword);
                    jsonBody.put("newpassword",newpassword);
                    jsonBody.put("confirmpassword",password2);
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
}