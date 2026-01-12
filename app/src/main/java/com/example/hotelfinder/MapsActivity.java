package com.example.hotelfinder;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import android.location.Address;
import android.location.Geocoder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient locationClient;
    private LatLng currentLatLng;
    private Circle rangeCircle;
    private List<Hotel> hotelList = new ArrayList<>();
    private HotelAdapter hotelAdapter;
    private static final int LOCATION_REQUEST = 100;
    private static final String API_KEY = "AIzaSyBKpSvu4pYLp6Jh1PPbBr6HFPstRUzhnCU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSearchLocation.setOnClickListener(v -> {
            String location = binding.etSearchLocation.getText().toString().trim();
            if (!location.isEmpty()) {
                searchLocationByName(location);
            }
        });

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        setupRecyclerView();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Highlight Maps icon
        bottomNavigationView.setSelectedItemId(R.id.nav_maps);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(MapsActivity.this, HomePage.class));
                finish(); // optional, to prevent multiple stacked activities
                return true;
            } else if (id == R.id.nav_maps) {
                // Already on Maps
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                finish(); // optional
                return true;
            }
            return false;
        });
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
            // Just show info window, do nothing else
            marker.showInfoWindow();
            return true;
        });
    }

    private void searchLocationByName(String locationName) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);

            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addressList.get(0);
            currentLatLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Move camera
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

            // Update range circle
            drawRangeCircle(currentLatLng, 1000);

            // Clear old markers
            mMap.clear();

            // Search hotels from new location
            searchNearbyHotels();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRangeCircle(LatLng center, double radiusMeters) {

        // Remove previous circle if exists
        if (rangeCircle != null) {
            rangeCircle.remove();
        }

        rangeCircle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters) // meters
                .strokeWidth(4f)
                .strokeColor(0x55007AFF) // blue stroke
                .fillColor(0x22007AFF)); // light blue fill
    }

    private void getCurrentLocation() {
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && mMap != null) {

                currentLatLng = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                // Move camera to current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                // 1 km range circle
                drawRangeCircle(currentLatLng, 1000);

                // Search hotels
                searchNearbyHotels();
            }
        });
    }

    private void searchNearbyHotels() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=2000&type=lodging&keyword=hotel&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        hotelList.clear();

                        mMap.clear();
                        drawRangeCircle(currentLatLng, 1000);

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject hotel = results.getJSONObject(i);
                            JSONObject loc = hotel.getJSONObject("geometry")
                                    .getJSONObject("location");

                            LatLng latLng = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng")
                            );

                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(hotel.getString("name"))
                                    .snippet(hotel.optString("vicinity")));

                            float[] distanceResult = new float[1];
                            Location.distanceBetween(
                                    currentLatLng.latitude,
                                    currentLatLng.longitude,
                                    latLng.latitude,
                                    latLng.longitude,
                                    distanceResult
                            );

                            float distanceKm = distanceResult[0] / 1000f;

                            JSONArray photos = hotel.optJSONArray("photos");
                            String photoRef = null;
                            if (photos != null && photos.length() > 0) {
                                photoRef = photos.getJSONObject(0).getString("photo_reference");
                            }

                            String placeId = hotel.getString("place_id");

                            Hotel h = new Hotel(
                                    hotel.getString("name"),
                                    hotel.optString("vicinity"),
                                    latLng,
                                    distanceKm,
                                    photoRef,
                                    placeId
                            );

                            hotelList.add(h);
                        }

                        // ✅ Notify AFTER loop
                        hotelAdapter.notifyDataSetChanged();

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

                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true); // enables current location
                    getCurrentLocation();
                }

            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setupRecyclerView() {
        hotelAdapter = new HotelAdapter(hotelList, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelSelected(Hotel hotel) {
                // Navigate to HotelDetailActivity
                Intent intent = new Intent(MapsActivity.this, HotelDetailActivity.class);
                intent.putExtra("name", hotel.name);
                intent.putExtra("lat", hotel.location.latitude);
                intent.putExtra("lng", hotel.location.longitude);
                intent.putExtra("address", hotel.address);
                intent.putExtra("photoRef", hotel.photoReference);
                intent.putExtra("placeId", hotel.placeId);
                startActivity(intent);
            }

            @Override
            public void onHotelFocused(Hotel hotel) {
                // Move map camera to hotel location
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hotel.location, 16));
                }
            }
        });

        binding.rvHotels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvHotels.setAdapter(hotelAdapter);
    }


}