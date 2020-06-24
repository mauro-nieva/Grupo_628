package com.example.lint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

public class LogActivity extends AppCompatActivity {

    private TextView txtHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtHistorial=(TextView) findViewById(R.id.txtHistorial);

        SharedPreferences preferences=getSharedPreferences("Historial", Context.MODE_PRIVATE);
        String contenido=preferences.getString("log","");
        txtHistorial.setText(contenido);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogActivity.this, LinternaActivity.class);
                startActivity(intent);
            }
        });

    }
}

