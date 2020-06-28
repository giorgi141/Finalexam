package com.b.loginandregister;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    TextView userName, userMail;
    ListView locationView;
    Button LogOut,getLocation;
    SessionManager sessionManager;


    private static String URL_SEND_LOCATION="http://192.168.100.3/Android_REG_LOG/sendlocation.php";
    private static String URL_GET_LOCATION = "http://192.168.100.3/Android_REG_LOG/getlocation.php";


    // location
    private LocationManager locationManager;
    private LocationListener locationListener;
    public String Lat,Lon;

    //Initialize variable
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Assign variable
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        //Initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();


        userMail = findViewById(R.id.userMail);
        userName = findViewById(R.id.userName);
        getLocation = findViewById(R.id.getLocation);


        LogOut = findViewById(R.id.logOut);


        HashMap <String, String> user = sessionManager.getUserDetail();
        String mName = user.get(sessionManager.NAME);
        final String mEmail = user.get(sessionManager.EMAIL);

        userName.setText(mName);
        userMail.setText(mEmail);

        //Location lan lon
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lon = Double.toString(location.getLongitude());
                String lat = Double.toString(location.getLatitude());
                Lat = lat;
                Lon = lon;
                sendLocation(mEmail);
                Toast.makeText(HomeActivity.this,lat + "/n" + lon , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);return;
        }else{
            locationManager.requestLocationUpdates("gps", 60000, 100, locationListener);
        }

        LogOut.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                sessionManager.logout();

            }
        });

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetLocation(mEmail);
            }
        });


    }

    public void GetLocation(final String email){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_LOCATION, new Response.Listener <String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject s = new JSONObject(new String(response));
                    String success = s.getString("success");
                    JSONArray jsonArray = s.getJSONArray("getLocation");

                    if(success.equals("1")){
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject object =jsonArray.getJSONObject(i);
                            final Double Lat = Double.parseDouble(object.getString("Latitude"));
                            final Double Lon = Double.parseDouble(object.getString("Longitude"));

                            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(GoogleMap googleMap) {
                                    LatLng latLng = new LatLng(Lat,Lon);
                                    MarkerOptions options =new MarkerOptions().position(latLng).title(email).draggable(true);
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,100));
                                    googleMap.addMarker(options);
                                }
                            });
                        }
                        Toast.makeText(HomeActivity.this,"Success! get Location",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(HomeActivity.this,"not Success! get Location 1",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this,"not Success! get Location 2" + e,Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this,"not Success! get Location 3",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map <String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap <>();
                params.put("email",email);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public void sendLocation(final String email){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SEND_LOCATION, new Response.Listener <String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject s = new JSONObject(new String(response));
                    String success = s.getString("success");

                    if(success.equals("1")){
                        Toast.makeText(HomeActivity.this,"Register Success! 1",Toast.LENGTH_SHORT).show();
//                        finish();
                    }else{
                        Toast.makeText(HomeActivity.this,"Register Success! 2",Toast.LENGTH_SHORT).show();
                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this,"Register Error! 2"+e.toString(),Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this,"Register Error! 3"+error.toString(),Toast.LENGTH_SHORT).show();

            }
        }){
            protected Map <String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap <>();
                params.put("email",email);
                params.put("Latitude",Lat);
                params.put("Longitude",Lon);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


}
