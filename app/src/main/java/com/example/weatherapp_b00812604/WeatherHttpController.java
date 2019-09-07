package com.example.weatherapp_b00812604;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

//singleton class to initialize volley core objects
public class WeatherHttpController  {
    private RequestQueue mRequestQueue;
    private static WeatherHttpController mInstance;
    private static Context mCtx;

    private WeatherHttpController(Context context){
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized WeatherHttpController getInstance(Context context) {
        if(mInstance==null){
            mInstance = new WeatherHttpController(context.getApplicationContext());
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

}
