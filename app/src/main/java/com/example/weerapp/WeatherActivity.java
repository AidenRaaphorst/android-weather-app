package com.example.weerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {
    private static final String GEOCODING_API_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "45714266c06a3ab0980ca535e9548d4c";
    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    private EditText locationInput;
    private TextView locationCountryText;
    private TextView degreesText;
    private TextView weatherText;
    private TextView windText;
    private TextView humidityText;
    private TextView pressureText;
    private ImageView weatherIcon;
    private String weatherIconUrl;
    private Button clearButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestQueue = Volley.newRequestQueue(this);
        locationInput = findViewById(R.id.locationInput);
        locationCountryText = findViewById(R.id.placeAndCountryName);
        degreesText = findViewById(R.id.degrees);
        weatherText = findViewById(R.id.weather);
        windText = findViewById(R.id.wind);
        humidityText = findViewById(R.id.humidity);
        pressureText = findViewById(R.id.pressure);
        clearButton = findViewById(R.id.wClearButton);
        backButton = findViewById(R.id.wBackButton);
        weatherIcon = findViewById(R.id.weatherIcon);

        clearButton.setOnClickListener((view) -> {
            locationCountryText.setText(getString(R.string.placeholder_location_landcode));
            degreesText.setText(getString(R.string.placeholder_degrees_celsius));
            weatherText.setText(getString(R.string.placeholder_weather_info));
            windText.setText(getString(R.string.placeholder_wind));
            humidityText.setText(getString(R.string.placeholder_humidity));
            pressureText.setText(getString(R.string.placeholder_pressure));
            weatherIcon.setImageIcon(null);
        });

        backButton.setOnClickListener((view) -> onBackPressed());

        locationInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_DONE) {
                String input = locationInput.getText().toString().trim();
                locationInput.setText(input);
                if(!TextUtils.isEmpty(input)) {
                    getWeatherByLocation(input);
                }

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locationInput.getWindowToken(), 0);

                return true;
            }

            return false;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("LOCATION_COUNTRY_TEXT", locationCountryText.getText().toString());
        outState.putString("DEGREES_TEXT", degreesText.getText().toString());
        outState.putString("WEATHER_TEXT", weatherText.getText().toString());
        outState.putString("WIND_TEXT", windText.getText().toString());
        outState.putString("HUMIDITY_TEXT", humidityText.getText().toString());
        outState.putString("PRESSURE_TEXT", pressureText.getText().toString());
        outState.putString("WEATHER_ICON_URL", weatherIconUrl);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        locationCountryText.setText(savedInstanceState.getString("LOCATION_COUNTRY_TEXT"));
        degreesText.setText(savedInstanceState.getString("DEGREES_TEXT"));
        weatherText.setText(savedInstanceState.getString("WEATHER_TEXT"));
        windText.setText(savedInstanceState.getString("WIND_TEXT"));
        humidityText.setText(savedInstanceState.getString("HUMIDITY_TEXT"));
        pressureText.setText(savedInstanceState.getString("PRESSURE_TEXT"));

        weatherIconUrl = savedInstanceState.getString("WEATHER_ICON_URL");
        Glide.with(weatherIcon.getContext()).load(weatherIconUrl).into(weatherIcon);
    }

    private void getWeatherByLocation(String location) {
        // Get lat & lon with place name
        String geocodingApiString = String.format("%s?q=%s&appId=%s", GEOCODING_API_URL, location, API_KEY);
        sendApiRequest(geocodingApiString, new ApiResponseListener() {
            @Override
            public void onResponse(JSONObject jsonResponse) throws JSONException {
                JSONObject place = jsonResponse.getJSONArray("response").getJSONObject(0);
                double lat = place.getDouble("lat");
                double lon = place.getDouble("lon");

                // Get weather information with lat & lon from the geocoding API
                getWeatherByLatLon(lat, lon);
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void getWeatherByLatLon(double lat, double lon) {
        String weatherApiString = String.format("%s?lat=%s&lon=%s&lang=nl&units=metric&appid=%s", WEATHER_API_URL, lat, lon, API_KEY);
        sendApiRequest(weatherApiString, new ApiResponseListener() {
            @Override
            public void onResponse(JSONObject jsonResponse) throws JSONException {
                jsonResponse = jsonResponse.getJSONObject("response");

                // Get weather information with json response
                String placeName = jsonResponse.getString("name");
                String countryCode = jsonResponse.getJSONObject("sys").getString("country");
                double degreesC = jsonResponse.getJSONObject("main").getDouble("temp");
                double degreesFeelC = jsonResponse.getJSONObject("main").getDouble("feels_like");
                double pressureInmilliB  = jsonResponse.getJSONObject("main").getDouble("pressure");
                int humidityPercentage  = jsonResponse.getJSONObject("main").getInt("humidity");
                String weatherInfo = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("icon");
                double windSpeedInMps = jsonResponse.getJSONObject("wind").getDouble("speed");
                double windMeteorologicalDegrees = jsonResponse.getJSONObject("wind").getDouble("speed");

                // Convert some units
                double windSpeedInKmph = windSpeedInMps * 3.6;
                String windCardinalDegrees = convertMeteorologicalDegreesToCardinal(windMeteorologicalDegrees);

                // Update UI
                locationCountryText.setText(String.format("%s, %s", placeName, countryCode));
                degreesText.setText(String.format("%s°C (Voelt als %s°C)", (int)degreesC, (int)degreesFeelC));
                weatherText.setText(String.format("%s%s", weatherInfo.substring(0, 1).toUpperCase(), weatherInfo.substring(1)));
                windText.setText(String.format("Wind: %s %s km/u", windCardinalDegrees, Math.round(windSpeedInKmph)));
                humidityText.setText(String.format("Vochtigheid: %s%%", humidityPercentage));
                pressureText.setText(String.format("Druk: %s mb", pressureInmilliB));

                weatherIconUrl = String.format("https://openweathermap.org/img/wn/%s@2x.png", icon);
                Glide.with(weatherIcon.getContext()).load(weatherIconUrl).into(weatherIcon);
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private interface ApiResponseListener {
        void onResponse(JSONObject jsonResponse) throws JSONException;
        void onError(VolleyError error);
    }

    private void sendApiRequest(String url, ApiResponseListener listener) {
        // Clear previous request if any
        if(stringRequest != null) {
            stringRequest.cancel();
        }

        stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                (response) -> {
                    try {
                        listener.onResponse(new JSONObject(String.format("{response:%s}", response)));
                    } catch (JSONException e) {
                        System.out.println(e);
                        Snackbar snackbar = Snackbar.make(getCurrentFocus(), "Plaats niet gevonden", Snackbar.LENGTH_LONG);
                        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
                        snackbar.setTextColor(Color.WHITE);
                        snackbar.setBackgroundTint(Color.DKGRAY);
                        snackbar.show();
                    }
                },
                (error) -> listener.onError(error)
        );

        requestQueue.add(stringRequest);
    }

    private String convertMeteorologicalDegreesToCardinal(double degrees) {
        String windDirectionString = "";

        if(degrees >= 348.75 || degrees <= 11.25) {
            windDirectionString = "N";
        } else if(degrees >= 11.25 && degrees <= 33.75) {
            windDirectionString = "NNO";
        } else if(degrees >= 33.75 && degrees <= 56.25) {
            windDirectionString = "NO";
        } else if(degrees >= 56.25 && degrees <= 78.75) {
            windDirectionString = "ONO";
        } else if(degrees >= 78.75 && degrees <= 101.25) {
            windDirectionString = "O";
        } else if(degrees >= 101.25 && degrees <= 123.75) {
            windDirectionString = "OZO";
        } else if(degrees >= 123.75 && degrees <= 146.25) {
            windDirectionString = "ZO";
        } else if(degrees >= 146.25 && degrees <= 168.75) {
            windDirectionString = "ZZO";
        } else if(degrees >= 168.75 && degrees <= 191.25) {
            windDirectionString = "Z";
        } else if(degrees >= 191.25 && degrees <= 213.75) {
            windDirectionString = "ZZW";
        } else if(degrees >= 213.75 && degrees <=  236.25) {
            windDirectionString = "ZW";
        } else if(degrees >=  236.25 && degrees <= 258.75) {
            windDirectionString = "WZW";
        } else if(degrees >= 258.75 && degrees <= 281.25) {
            windDirectionString = "W";
        } else if(degrees >= 281.25 && degrees <= 303.75) {
            windDirectionString = "WNW";
        } else if(degrees >= 303.75 && degrees <= 326.25) {
            windDirectionString = "NW";
        } else if(degrees >= 326.25) {
            windDirectionString = "NNW";
        }

        return windDirectionString;
    }
}