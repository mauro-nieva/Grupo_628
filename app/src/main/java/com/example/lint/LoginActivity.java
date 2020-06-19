package com.example.lint;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnRegister;
    private TextView txtEmail;
    private TextView txtPassword;

    public IntentFilter filtro;
    private LoginActivity.ReceptorOperacion receiver=new LoginActivity.ReceptorOperacion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail=(TextView) findViewById(R.id.txtEmail);
        txtPassword=(TextView) findViewById(R.id.txtPassword);

        btnLogin=(Button) findViewById(R.id.btnLogin);
        btnRegister=(Button) findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(HandlerCmdLogin);
        btnRegister.setOnClickListener(HandlerCmdRegister);

        configurarBroadcastreceiver();
    }

    View.OnClickListener HandlerCmdLogin=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject obj = new JSONObject();

            try {
                obj.put("env", "DEV");
                obj.put("name", "");
                obj.put("lastname", "");
                obj.put("dni", "");
                obj.put("email", txtEmail.getText());
                obj.put("password", txtPassword.getText());
                obj.put("commission", "");
                obj.put("group", "");

                Intent i = new Intent(LoginActivity.this, Service_Post.class);

                i.putExtra("url", "http://so-unlam.net.ar/api/api/login");
                i.putExtra("datosJson", obj.toString());

                startService(i);

                Log.i("LOG_LOGIN","Se hizo click.");

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("LOG_LOGIN","Error Login: "+e.getMessage());
            }
        }
    };

    View.OnClickListener HandlerCmdRegister=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        }
    };

    private void configurarBroadcastreceiver(){

        filtro=new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_OPERACION");

        filtro.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filtro);

    }

    public class ReceptorOperacion extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent){
            try{
                String datosJsonString= intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);
                String state = datosJson.getString("state");


                Log.i("LOG_LOGIN","Datos Json Login Thread:"+datosJsonString);


                if(state.equals("success"))
                {
                    Toast.makeText(getApplicationContext(), "Ingreso correcto", Toast.LENGTH_LONG).show();
                    Log.i("LOG_LOGIN","Token:"+datosJson.get("token"));
                    //finish();
                    Intent Int_Linterna=new Intent(LoginActivity.this,LinternaActivity.class);
                    startActivity(Int_Linterna);
                }
                else
                {
                    String msg=datosJson.getString("msg");
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
            catch(Exception e)
            {
                Log.e("LOG_LOGIN","Error Json Login Thread:"+e.getMessage());
            }

        }
    }
}
