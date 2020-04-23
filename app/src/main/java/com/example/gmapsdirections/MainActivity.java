package com.example.gmapsdirections;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Toast notification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        TextView lat1 = findViewById(R.id.Lat1);
        TextView lat2 = findViewById(R.id.Lat2);
        TextView long1 = findViewById(R.id.Long1);
        TextView long2 = findViewById(R.id.Long2);

        button.setOnClickListener((v)->{

            if(lat1.getText().toString().equals("") || lat2.getText().toString().equals("") || long1.getText().toString().equals("") || long2.getText().toString().equals("")){
                this.notification = Toast.makeText(getApplicationContext(),
                        "Todos los campos son requeridos", Toast.LENGTH_LONG);
            }else {
                Intent changeActivity = new Intent(this, MapsActivity.class);
                changeActivity.putExtra("lat1", lat1.getText().toString());
                changeActivity.putExtra("lat2", lat2.getText().toString());
                changeActivity.putExtra("long1", long1.getText().toString());
                changeActivity.putExtra("long2", long2.getText().toString());
                startActivity(changeActivity);
            }

            this.notification.show();
        });
    }
}
