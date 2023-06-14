package com.example.weerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button weatherButton = findViewById(R.id.weatherButton);
        Button earthquakesNButton = findViewById(R.id.eathquakesNButton);
        Button earthquakesWWButton = findViewById(R.id.earthquakesWWButton);

        weatherButton.setOnClickListener(this::startWeatherActivity);
        earthquakesNButton.setOnClickListener(this::startEarthquakesNetherlandsActivity);
        earthquakesWWButton.setOnClickListener(this::startEarthquakesWorldwideActivity);
    }

    public void startWeatherActivity(View view) {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }

    public void startEarthquakesNetherlandsActivity(View view) {
        Intent intent = new Intent(this, EarthquakesNetherlandsActivity.class);
        startActivity(intent);
    }

    public void startEarthquakesWorldwideActivity(View view) {
        Intent intent = new Intent(this, EarthquakesWorldwideActivity.class);
        startActivity(intent);
    }
}