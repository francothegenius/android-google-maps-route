package com.example.gmapsdirections;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        points = new ArrayList<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                 //borrar marcadores cuando pase de 2 puntos
                if (points.size() == 2){
                    points.clear();
                    mMap.clear();
                }
                //guardar primera posicion
                points.add(latLng);
                //crear marcador en posicion
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (points.size() == 1){
                    //agregar primer marcador al mapa
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } else {
                    //agregar segundo marcador al mapa
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                mMap.addMarker(markerOptions);

                if (points.size() == 2){
                    //se necesita un URL para obtener la direccion de primer marcador a segundo marcador
                    String url = getRequestUrl(points.get(0), points.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        });
    }

    private String getRequestUrl(LatLng from, LatLng to) {

        String str_from = "origin="+ from.latitude + "," + from.longitude;
        String str_to = "destination="+ to.latitude + "," + to .longitude;

        //sensor
        String sensor ="sensor=false";
        //mode
        String mode ="mode=driving";
        //API
        String api_key ="key=KEY";
        //result
        String result = str_from + "&" + str_to + "&" + sensor + "&"+ mode+"&"+api_key;
        //output format
        String output = "json";
        //creating url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + result;
        Log.d("Direction", "https://maps.googleapis.com/maps/api/directions/" + output + "?" + result);
        return url;
    }

    private String requestDirection(String url) throws IOException {
        String response = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL newUrl = new URL(url);
            httpURLConnection = (HttpURLConnection) newUrl.openConnection();
            httpURLConnection.connect();

            //get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            response = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return response;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }


     public class TaskRequestDirections extends AsyncTask<String, Void, String> {
         @Override
         protected String doInBackground(String... strings) {
             String response = "";
             try {
                 response = requestDirection(strings[0]);
             } catch (IOException e){
                 e.printStackTrace();
             }
             return response;
         }

         @Override
         protected void onPostExecute(String s) {
             super.onPostExecute(s);
             //parse json
             TaskParser taskParser = new TaskParser();
             taskParser.execute(s);
         }
     }

     public class TaskParser extends AsyncTask<String, Void,  List<List<HashMap<String, String>>> >{

         @Override
         protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
             JSONObject jsonObject = null;

             List<List<HashMap<String, String>>> routes = null;
             try {
                 jsonObject = new JSONObject(strings[0]);
                 DirectionsParser directionsParser = new DirectionsParser();
                 routes = directionsParser.parse(jsonObject);

             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return routes;
         }

         @Override
         protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
             //get list route and put it on the map

             ArrayList points = null;

             PolylineOptions polylineOptions = null;

             for (List<HashMap<String, String>> path : lists){
                 points = new ArrayList();
                 polylineOptions = new PolylineOptions();

                 for (HashMap<String, String> point : path){
                     double lat = Double.parseDouble(point.get("lat"));
                     double lon = Double.parseDouble(point.get("lon"));

                     points.add(new LatLng(lat,lon));
                 }

                 polylineOptions.addAll(points);
                 polylineOptions.width(15);
                 polylineOptions.color(Color.BLUE);
                 polylineOptions.geodesic(true);
             }

             if (polylineOptions != null){
                 mMap.addPolyline(polylineOptions);
             } else {
                 Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_SHORT).show();
             }
         }
     }
}
