package com.example.hotelfinder;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.hotelfinder.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient locationClient;
    private LatLng currentLatLng;

    private static final int LOCATION_REQUEST = 100;
    private static final String API_KEY = "AIzaSyBKpSvu4pYLp6Jh1PPbBr6HFPstRUzhnCU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);
        getCurrentLocation();

        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(MapsActivity.this, HotelDetailActivity.class);
            intent.putExtra("name", marker.getTitle());
            intent.putExtra("lat", marker.getPosition().latitude);
            intent.putExtra("lng", marker.getPosition().longitude);
            intent.putExtra("address", marker.getSnippet());
            startActivity(intent);
            return true;
        });
    }

    private void getCurrentLocation() {
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                searchNearbyHotels();
            }
        });
    }

    private void searchNearbyHotels() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=2000&type=lodging&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject hotel = results.getJSONObject(i);
                            JSONObject loc = hotel.getJSONObject("geometry")
                                    .getJSONObject("location");

                            LatLng latLng = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng"));

                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(hotel.getString("name"))
                                    .snippet(hotel.optString("vicinity")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this,
                        "Failed to load hotels", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(); // retry getting location after permission granted
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
