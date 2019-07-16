package com.dimnikol.weathernow;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Utils {

    private Utils(){}

    public static String getPrefix(Object object) {
        String className = object.getClass().getSimpleName();
        return Constants.WEATHER_NOW + className ;
    }

}
