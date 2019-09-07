package com.example.weatherapp_b00812604;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText cityTxt;
    Button btnSearch;
    TextView showCity, showTemp, showMaxMinTemp, showMain, showDesc, showHumidClouds;
    ImageView weatherIcon;

    String cityEntered="";

    public static String API_KEY = "aaed111fe12b68e1a6e6aa4157cca49a";
    private String CONSTANT_WEATHER = "https://api.openweathermap.org/data/2.5/weather?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide the title bar and make the app full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //defining the components
        cityTxt = findViewById(R.id.cityTxt);
        showCity = findViewById(R.id.showCity);
        showTemp = findViewById(R.id.showTemp);
        showMaxMinTemp = findViewById(R.id.showMaxMinTemp);
        showMain = findViewById(R.id.showMain);
        showDesc = findViewById(R.id.showDesc);
        showHumidClouds = findViewById(R.id.showHumidClouds);
        btnSearch = findViewById(R.id.btnSearch);
        weatherIcon=findViewById(R.id.weatherIcon);

        //event on click of a button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calling function to check internet connectivity
                boolean checkNetwork = isInternetAvailable(getApplicationContext());

                if(!checkNetwork){
                    showCity.setText("No Internet Connectivity.\n Please enable Mobile Data/Wifi");
                    showTemp.setText("");
                    showMaxMinTemp.setText("");
                    showMain.setText("");
                    showDesc.setText("");
                    showHumidClouds.setText("");
                    weatherIcon.setImageIcon(null);
                }
                else{

                    try{
                        cityEntered = cityTxt.getText().toString();
                    } catch(Exception e){
                        e.printStackTrace();
                    }

                    String url = CONSTANT_WEATHER + cityEntered + "&appid=" + API_KEY;

                    final JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.GET, //API call : GET method
                            url,
                            null,
                            new Response.Listener<JSONObject>(){
                                @Override
                                public void onResponse(JSONObject response){
                                    /**
                                     * Checking if a digit is typed instead of a city name
                                     */
                                    try{
                                        int in = Integer.parseInt(cityEntered);
                                        showCity.setText("Please type a city that exists.\nA digit like "+in+" \nis not a valid city.");
                                        showTemp.setText("");
                                        showMaxMinTemp.setText("");
                                        showMain.setText("");
                                        showDesc.setText("");
                                        showHumidClouds.setText("");
                                        weatherIcon.setImageIcon(null);

                                    }
                                    catch(NumberFormatException e){
//                                    e.printStackTrace();
                                        try{
                                            JSONArray jsonArray= response.getJSONArray("weather");
                                            JSONObject jsonResponse = new JSONObject();
                                            for(int i=0;i<jsonArray.length();i++){
                                                JSONObject weather = jsonArray.getJSONObject(i);

                                                jsonResponse.put("main",weather.getString("main"));
                                                jsonResponse.put("desc",weather.getString("description"));
                                                //retrieving image from the image id in the response
                                                String urlIcon = "i"+weather.getString("icon");
                                                Resources resources = getApplicationContext().getResources();
                                                final int resIcon = resources.getIdentifier(urlIcon,"drawable",getApplicationContext().getPackageName());
                                                weatherIcon.setImageResource(resIcon);
                                            }

                                            JSONObject mainObj = response.getJSONObject("main");
                                            /**
                                             * Converting temperature in Kelvin to Celsius
                                             */
                                            jsonResponse.put("minTemp",((int)((mainObj.getDouble("temp_min"))-273.15)));
                                            jsonResponse.put("maxTemp",((int)((mainObj.getDouble("temp_max"))-273.15)));
                                            jsonResponse.put("temp",((int)((mainObj.getDouble("temp"))-273.15)));
                                            jsonResponse.put("humid",mainObj.getInt("humidity"));

                                            JSONObject cloudObj = response.getJSONObject("clouds");
                                            jsonResponse.put("clouds",cloudObj.getInt("all"));

                                            jsonResponse.put("city",response.getString("name"));

                                            /**
                                             * setting the weather details based on response
                                             */
                                            if(jsonResponse.length()!=0){
                                                showCity.setText(jsonResponse.getString("city"));
                                                showTemp.setText(jsonResponse.get("temp")+"°C");
                                                showMaxMinTemp.setText("Min "+jsonResponse.get("minTemp")+"°C"+"    Max "+jsonResponse.get("maxTemp")+"°C");
                                                showMain.setText((String)(jsonResponse.get("main")));
                                                showDesc.setText((String)(jsonResponse.get("desc")));
                                                showHumidClouds.setText("Humidity "+jsonResponse.get("humid")+"%"+"     Clouds "+jsonResponse.get("clouds")+"%");
                                            }
                                        }catch(Exception et){
//                                        et.printStackTrace();
                                        }
                                    }


                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                /**
                                 * Handling error if no city is typed or incorrect city name is typed
                                 */
                                public void onErrorResponse(VolleyError error) {

                                    if(error.getMessage()==null){
                                        showCity.setText("Please type a city that exists.\n"+(cityEntered.isEmpty()?"No city typed":cityEntered+" does not exist."));
                                        showTemp.setText("");
                                        showMaxMinTemp.setText("");
                                        showMain.setText("");
                                        showDesc.setText("");
                                        showHumidClouds.setText("");
                                        weatherIcon.setImageIcon(null);
                                    }
                                }
                            }
                    );
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(showCity.getWindowToken(), 0);
                    WeatherHttpController.getInstance(getApplicationContext()).addToRequestQueue(request);

                }



            } //endof onclick
        });
    }

    /**
     * function to check internet connectivity
     * @param context
     * @return
     */
    public boolean isInternetAvailable(Context context) {

        if (context == null) {
            return true;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
        if (nwInfo != null && nwInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
