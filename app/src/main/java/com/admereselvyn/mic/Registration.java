package com.admereselvyn.mic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.admereselvyn.mic.api.Api;
import com.admereselvyn.mic.api.core.CoreApiDetails;
import com.admereselvyn.mic.api.models.registration.RegisterResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class Registration extends AppCompatActivity {
    private EditText name,mail,pass;
    private Button createAccount;
    ProgressDialog progressDialog;
    RequestQueue queue;
    ImageView password_hide;
    boolean isPasswordVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog=new ProgressDialog(Registration.this);
        name=findViewById(R.id.name);
        mail=findViewById(R.id.mail);
        pass=findViewById(R.id.pass);
        createAccount=findViewById(R.id.createAccount);
        password_hide=findViewById(R.id.password_hide);

        queue = Volley.newRequestQueue(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mpass,memail,mname;
                mname=name.getText().toString().trim();
                mpass=pass.getText().toString().trim();
                memail=mail.getText().toString().trim();
                if(TextUtils.isEmpty(mname))
                {
                    name.setError("Required Field");
                    return;
                }
                if(TextUtils.isEmpty(mpass))
                {
                    pass.setError("Required Field");
                    return;
                }
                if(TextUtils.isEmpty(memail))
                {
                    mail.setError("Required Field");
                    return;

                }
                if(mpass.length()<6 || mname.length()<6)
                {
                    Toast.makeText(Registration.this, "Minimum length should be 6", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.setMessage("Processing...");
                progressDialog.show();
                performRegistration(memail,mname,mpass);
            }
        });

        password_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPasswordVisible){
                    ((ImageView)(v)).setImageResource(R.drawable.password_toggle_hide);

                    //Show Password
                    pass.setTransformationMethod(new PasswordTransformationMethod());
                    isPasswordVisible=false;
                }
                else{
                    ((ImageView)(v)).setImageResource(R.drawable.password_toggle_show);

                    //Hide Password
                    pass.setTransformationMethod(null);
                    isPasswordVisible=true;
                }
            }
        });
    }

    void performRegistration(String username,String name,String password){
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username",username);
            requestBody.put("name",name);
            requestBody.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Api api = new CoreApiDetails();
        String url = api.getRegisterUrl();
        Gson gson = new Gson();

        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                RegisterResponse registerResponse = gson.fromJson(response.toString(),RegisterResponse.class);
                Log.e("response",registerResponse.toString());

                //TODO: all code for success will go here

                if (registerResponse.getStatus().equals("success")){
                    progressDialog.dismiss();
                    Intent intent=new Intent(Registration.this,EmailVerification.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(Registration.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    //TODO: all code for error will go here
                    progressDialog.dismiss();
                    if(error instanceof NoConnectionError){
                        Toast.makeText(Registration.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject response = new JSONObject(responseBody);
                        Toast.makeText(Registration.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.e("Exception",e.getMessage());
                }
            }
        });
        queue.add(jsonObjectRequest);
    }
}