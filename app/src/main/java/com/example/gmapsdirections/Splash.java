package com.example.gmapsdirections;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent changeActivity = new Intent(Splash.this, MainActivity.class);
                Splash.this.startActivity(changeActivity);
                Splash.this.finish();
            }
        }, 4000);
    }
}
