package com.akshat.earthquako.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.akshat.earthquako.Model.EarthQuake;
import com.akshat.earthquako.R;
import com.akshat.earthquako.UI.CustomInfoWindow;
import com.akshat.earthquako.Util.Constants;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private BitmapDescriptor[] iconColors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        iconColors=new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
               // BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)

        };

        queue = Volley.newRequestQueue(this);
        getEarthQuakes();
    }

    public void getEarthQuakes() {
        final EarthQuake earthQuake = new EarthQuake();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0; i < Constants.LIMIT; i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);


                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));
                                earthQuake.setLat(lat);
                                earthQuake.setLon(lon);

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date
                                        (Long.valueOf(properties.getLong("time"))).getTime());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.icon(iconColors[Constants.randomInt(iconColors.length,0)]);
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
                                markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude() +
                                        "\n" + "Date: " + formattedDate);
                                if (earthQuake.getMagnitude()>=2){
                                    CircleOptions circleOptions=new CircleOptions();
                                    circleOptions.center(new LatLng(earthQuake.getLat(),earthQuake.getLon()));
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.radius(30000);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    mMap.addCircle(circleOptions);
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location!=null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng latLng = new LatLng(lat, lon);
                    String locLat = String.valueOf(lat) + "," + String.valueOf(lon);
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Hello"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    ==PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
            Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getQuakeDetails(marker.getTag().toString());
    }

    private void getQuakeDetails(String url) {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String detailsUrl="";
                        try {
                            JSONObject properties=response.getJSONObject("properties");
                            JSONObject products=properties.getJSONObject("products");
                            JSONArray geoserve=products.getJSONArray("geoserve");

                            for (int i=0;i<geoserve.length();i++){
                                JSONObject geoserveObj=geoserve.getJSONObject(i);
                                JSONObject contentObj=geoserveObj.getJSONObject("contents");
                                JSONObject geoJsonObj=contentObj.getJSONObject("geoserve.json");

                                detailsUrl=geoJsonObj.getString("url");

                            }
                            Log.d("URL:",detailsUrl);
                            getMoreDetails(detailsUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }
    public void getMoreDetails(String url){
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialogBuilder=new AlertDialog.Builder(MapsActivity.this);
                View view= getLayoutInflater().inflate(R.layout.popup,null);
                final Button dismissButton=(Button) view.findViewById(R.id.dismissPop);
                Button dismissButtonTop=(Button) view.findViewById(R.id.dismissPopTop);
                TextView popList=(TextView) view.findViewById(R.id.popList);
                WebView htmlpop=(WebView) view.findViewById(R.id.htmlWebview);

                StringBuilder stringBuilder=new StringBuilder();
                try {
                    JSONArray cities=response.getJSONArray("cities");
                    for (int i=0;i<cities.length();i++){
                        JSONObject citiesObj=cities.getJSONObject(i);
                        stringBuilder.append("City: " +citiesObj.getString("name") +
                        "\n" + "Distance: " + citiesObj.getString("distance") +
                        "\n" + "Population: " +citiesObj.getString("population"));
                        stringBuilder.append("\n\n");
                    }
                    popList.setText(stringBuilder);
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setView(view);
                    dialog=dialogBuilder.create();
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
