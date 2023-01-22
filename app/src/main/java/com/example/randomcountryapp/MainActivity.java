package com.example.randomcountryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener{

    TextView randomCountry;
    TextView randomCapital;
    TextView shakeInformation;
    Button button;
    Button searchButton;
    ImageView imageView;
    String mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.splashCreenTheme);
        setContentView(R.layout.activity_main);

        randomCountry = findViewById(R.id.randomCountry);
        randomCapital = findViewById(R.id.capital);
        shakeInformation = findViewById(R.id.txtShakeStatus);
        button = findViewById(R.id.button);
        searchButton = findViewById(R.id.search);
        imageView = findViewById(R.id.imgFlag);

        button.setOnClickListener(view -> callApi());

        searchButton.setOnClickListener(view -> searchInGoogle());

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }

    private void searchInGoogle() {
        String urlString = "https://www.google.com/maps/place/Bialystok+University+of+Technology/@53.1271575,23.1230781,14.87z/data=!4m5!3m4!1s0x471ffbfb9415aee5:0x1f6449ccc6c966f7!8m2!3d53.1170989!4d23.146686";
        if(mapView != null) {
            urlString = mapView;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android/id.chrome");
        try {
            this.startActivity(intent);
        }
        catch (ActivityNotFoundException ex) {
            intent.setPackage(null);
            this.startActivity(intent);
        }
    }

    public static final String TAG = "MyTag";
    StringRequest stringRequest;
    RequestQueue requestQueue;
    private void callApi() {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        RequestQueue queue = Volley.newRequestQueue(this);

        Random r = new Random();
        int random = r.nextInt(250);

        String url ="https://restcountries.com/v3.1/all";

        stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String capital = "Brak stolicy";
                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            JSONObject object= jsonarray.getJSONObject(random);
                            String country = getCountryNameFromApi(object, random);

                            if(!country.equals("Antarctica"))
                                 capital = getCapitalName(object);
                            mapView = getMapUrl(object);
                            String flagImageUrl = getFlag(object);
                            Picasso.get().load(flagImageUrl).resize(1300,600).into(imageView);

                            randomCountry.setText(country);
                            if(capital != null)
                                randomCapital.setText(capital);
                            else randomCapital.setText(R.string.capitalNotAvailable);

                            shakeInformation.setText(R.string.textview_text_shake);
                        }
                        catch (JSONException err){
                            Log.d("Error", err.toString());
                        }
                    }

                    private String getCapitalName(JSONObject object) throws JSONException {
                        Log.d("object", object.toString());
                        JSONArray capitalsArray = object.getJSONArray("capital");
                        String capital = (String) capitalsArray.get(0);
                        Log.d("capital", capital);
                        return  capital;
                    }

                    private String getFlag(JSONObject object) throws JSONException {
                        Log.d("flags", object.toString());
                        JSONObject flagsArray = object.getJSONObject("flags");
                        String png = flagsArray.getString("png");
                        Log.d("linkToFlag", png);
                        return png;
                    }


                    private String getMapUrl(JSONObject object) throws JSONException {
                        Log.d("googleMaps", object.toString());
                        JSONObject capitalsArray = object.getJSONObject("maps");
                        String mapLink = capitalsArray.getString("googleMaps");
                        Log.d("linkToMap", mapLink);
                        return mapLink;
                    }

                    @NonNull
                    private String getCountryNameFromApi(JSONObject object, int random) throws JSONException {
                        Log.d("object", object.toString());
                        JSONObject jsonArray = object.getJSONObject("name");
                        String name = jsonArray.getString("common");
                        Log.d("lalala", name);
                        return name;
                    }
                }, error -> Log.d("Error", "ERROR"));

        queue.add(stringRequest);
        stringRequest.setTag(TAG);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null)
            requestQueue.cancelAll(TAG);
    }

    @Override
    public void hearShake() {
        callApi();
        shakeInformation.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake));
        shakeInformation.setText(R.string.shakeInformationCountryRandomizing);

    }
}