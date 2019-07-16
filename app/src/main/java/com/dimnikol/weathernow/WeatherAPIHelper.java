package com.dimnikol.weathernow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import lombok.Data;

import static com.dimnikol.weathernow.Utils.getPrefix;
import static com.dimnikol.weathernow.activities.MainActivity.APP_ID;
import static com.dimnikol.weathernow.activities.MainActivity.GET_LOCATION_CODE;

@Data
public class WeatherAPIHelper {

    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    // Time between location updates (5000 milliseconds or 5 seconds) and distance between location updates (1000m or 1km)
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;

    private String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    private WeatherModel currentWeather;
    private Context context;

    public WeatherAPIHelper(Context context) {
        this.context = context;
    }


    public void sentGetRequest(RequestParams params, String url) {
        // SyncHttpClient belongs to the loopj dependency.
        SyncHttpClient client = new SyncHttpClient();

        // Making an HTTP GET request by providing a URL and the parameters.
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d(getPrefix(this), "Success! JSON: " + response.toString());
                WeatherModel weather = new WeatherModel();
                weather.fromJson(response);
                currentWeather = weather;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                Log.e(getPrefix(this), "Fail " + e.toString());
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Request Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(getPrefix(this), "Status code " + statusCode);
                Log.d(getPrefix(this), "Here's what we got instead " + response.toString());
            }

        });
    }


    // Configuring the parameters when a new city has been entered:
    public void getWeatherForNewCity(String city) {
        Log.d(getPrefix(this), "Getting weather for new city");
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        sentGetRequest(params, WEATHER_URL);

    }


    // Location Listener callbacks here, when the location has changed.
    public void getWeatherForCurrentLocation(LocationManager locationManager, LocationListener locationListener, Activity activity) {

        Log.d(getPrefix(this), "Getting weather for current location");
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d(getPrefix(this), "onLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(getPrefix(this), "longitude is: " + longitude);
                Log.d(getPrefix(this), "latitude is: " + latitude);

                // Providing 'lat' and 'lon' (spelling: Not 'long') parameter values
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                sentGetRequest(params, WEATHER_URL);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Log statements to help you debug your app.
                Log.d(getPrefix(this), "onStatusChanged() callback received. Status: " + status);
                Log.d(getPrefix(this), "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(getPrefix(this), "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(getPrefix(this), "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        // This is the permission check to access (fine) location.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOCATION_CODE);
            return;
        }

        //Speed up update on screen by using last known location.
        Location lastLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
        String longitude = String.valueOf(lastLocation.getLongitude());
        String latitude = String.valueOf(lastLocation.getLatitude());
        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("lon", longitude);
        params.put("appid", APP_ID);
        sentGetRequest(params, WEATHER_URL);

        // Some additional log statements to help you debug
        Log.d(getPrefix(this), "Location Provider used: "
                + locationManager.getProvider(LOCATION_PROVIDER).getName());
        Log.d(getPrefix(this), "Location Provider is enabled: "
                + locationManager.isProviderEnabled(LOCATION_PROVIDER));
        Log.d(getPrefix(this), "Last known location (if any): "
                + locationManager.getLastKnownLocation(LOCATION_PROVIDER));
        Log.d(getPrefix(this), "Requesting location updates");




    }



}
