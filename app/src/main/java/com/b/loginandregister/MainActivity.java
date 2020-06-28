package com.b.loginandregister;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText name,email,password,c_password;
    private Button btn_regist;
    private ProgressBar loading;
    private static String URL_REGIST = "http://192.168.100.3/Android_REG_LOG/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = findViewById(R.id.loading);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        c_password=findViewById(R.id.c_password);
        btn_regist = findViewById(R.id.btn_regist);

        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Regist();
            }
        });
    }

    private void Regist(){
        loading.setVisibility(View.VISIBLE);
        btn_regist.setVisibility(View.GONE);

        final String name = this.name.getText().toString();
        final String email = this.email.getText().toString();
        final String password = this.password.getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGIST, new Response.Listener <String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject s = new JSONObject(new String(response));
                    String success = s.getString("success");

                    if(success.equals("1")){
                        Toast.makeText(MainActivity.this,"Register Success!",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"Register Error! "+e.toString(),Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                    btn_regist.setVisibility(View.VISIBLE);
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"Register Error! "+error.toString(),Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        btn_regist.setVisibility(View.VISIBLE);
                    }
                }
        )
        {
            protected Map<String,String> getParams() throws AuthFailureError{
                Map<String,String> params = new HashMap <>();
                params.put("name",name);
                params.put("email",email);
                params.put("password",password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

}
