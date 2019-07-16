package com.dimnikol.weathernow.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dimnikol.weathernow.R;
import com.dimnikol.weathernow.WeatherAPIHelper;
import com.dimnikol.weathernow.WeatherModel;

import static com.dimnikol.weathernow.Utils.getPrefix;

public class MainActivity extends AppCompatActivity {

    public static final int GET_LOCATION_CODE = 1;
    public static final int NEW_CITY_CODE = 2;

    public static final String APP_ID = "cae1774df2eaf18c394ae23ee02fbc59";

    private WeatherModel weatherModel;



    private LocationManager locationManager;
    private LocationListener locationListener;
    private RelativeLayout relativeLayout;

    //Flag to determine if the user wannt his location,or another location
    boolean userLocation = true;

    //TextView for the Temperature
    private TextView weatherTemperature;

    //Object to call the webservice, and parse the response
    private WeatherAPIHelper apiHelper = new WeatherAPIHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.outerBox);
        weatherTemperature = findViewById(R.id.weatherDegrees);

        ImageButton changeCityButton = findViewById(R.id.changeCityButton);

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ChangeCity.class);
                // Using startActivityForResult since we just get back the city name.
                // Providing an arbitrary request code to check against later.
                startActivityForResult(myIntent, NEW_CITY_CODE);
            }
        });
    }

    // onResume() lifecycle callback:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getPrefix(this), "onResume() called");
        if (userLocation) {


            Thread thread = new Thread(){
                public void run(){
                    apiHelper.getWeatherForCurrentLocation(locationManager, locationListener, MainActivity.this);
                    weatherModel = apiHelper.getCurrentWeather();
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateUI(weatherModel);
            userLocation=false;
        }
    }

    // Freeing up resources when the app enters the paused state.
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // Callback received when a new city name is entered on the second screen.
    // Checking request code and if result is OK before making the API call.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(getPrefix(this), "onActivityResult() called");

        if (requestCode == NEW_CITY_CODE) {
            if (resultCode == RESULT_OK) {
                final String city = data.getStringExtra("City");
                Log.d(getPrefix(this), "New city is " + city);

                userLocation = false;

                Thread thread = new Thread(){
                    public void run(){
                        apiHelper.getWeatherForNewCity(city);
                        weatherModel = apiHelper.getCurrentWeather();
                    }
                };
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(getPrefix(this), "The weather is " + weatherModel.getTemperature());
                updateUI(weatherModel);
            }
        }
    }

    // This is the callback that's received when the permission is granted (or denied)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checking against the request code we specified earlier.
        if (requestCode == GET_LOCATION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(getPrefix(this), "onRequestPermissionsResult(): Permission granted!");

                // Getting weather only if we were granted permission.
                apiHelper.getWeatherForCurrentLocation(locationManager, locationListener, this);
            } else {
                Log.d(getPrefix(this), "Permission denied =( ");
            }
        }
    }

    // Updates the information shown on screen.
    private void updateUI(WeatherModel weather) {
        weatherTemperature.setText(weather.getCity()+"\n"+weather.getCondition() + "\n" + weather.getTemperature() + " \u2103");

        int resourceID = getResources().getIdentifier(weather.getCondition().toLowerCase(), "drawable", getPackageName());
        Drawable resources = getResources().getDrawable(resourceID);
        relativeLayout.setBackground(resources);

    }

}
