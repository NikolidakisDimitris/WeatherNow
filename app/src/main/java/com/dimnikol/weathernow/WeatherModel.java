package com.dimnikol.weathernow;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Data;

@Data
public class WeatherModel extends Thread {

    private String temperature;
    private String city;
    private String condition;
    private int conditionCode;


    public WeatherModel fromJson(JSONObject jsonObject) {
        WeatherModel weather = new WeatherModel();

        try {
            city = jsonObject.getString("name");
            conditionCode = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            condition = updateBackground(conditionCode);

            double tempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            int roundedValue = (int) Math.rint(tempResult);

            temperature = Integer.toString(roundedValue);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weather;
    }

    // Get the number code and deduct the background image
    private String updateBackground(int conditionCode) {

        if (conditionCode >= 0 && conditionCode < 300) {
            return "Storm";
        } else if (conditionCode >= 300 && conditionCode < 600) {
            return "Rain";
        } else if (conditionCode >= 600 && conditionCode <= 700) {
            return "Snow";
        } else if (conditionCode >= 701 && conditionCode <= 771) {
            return "Fog";
        } else if (conditionCode >= 772 && conditionCode < 800) {
            return "Storm";
        } else if (conditionCode == 800) {
            return "Sunny";
        } else if (conditionCode >= 801 && conditionCode <= 804) {
            return "Cloudy";
        } else if (conditionCode >= 900 && conditionCode <= 902) {
            return "Storm";
        } else if (conditionCode == 903) {
            return "Snow";
        } else if (conditionCode == 904) {
            return "Sunny";
        } else if (conditionCode >= 905 && conditionCode <= 1000) {
            return "Storm";
        }

        return "dunno";
    }

}
