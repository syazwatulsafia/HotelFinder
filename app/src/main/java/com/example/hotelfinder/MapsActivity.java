package com.example.hotelfinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.hotelfinder.databinding.ActivityMapsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        setupRecyclerView();
        setupNavigation();

        binding.btnSearchLocation.setOnClickListener(v -> {
            String location = binding.etSearchLocation.getText().toString().trim();
            if (!location.isEmpty()) {
                searchLocationByName(location);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMapAndSearch();
                }
            });
        }
    }

    private void updateMapAndSearch() {
        if (mMap != null && currentLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            drawRangeCircle(currentLatLng, 1000);
            searchNearbyHotels();
        }
    }

    private void drawRangeCircle(LatLng center, double radiusMeters) {
        if (rangeCircle != null) rangeCircle.remove();

        rangeCircle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters)
                .strokeWidth(5f)
                .strokeColor(0xFF759EA5) // Brand Teal
                .fillColor(0x22759EA5));
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
                            JSONObject loc = hotel.getJSONObject("geometry").getJSONObject("location");
                            LatLng latLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                            mMap.addMarker(new MarkerOptions().position(latLng).title(hotel.getString("name")));

                            float[] dist = new float[1];
                            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, latLng.latitude, latLng.longitude, dist);

                            String photoRef = hotel.has("photos") ? hotel.getJSONArray("photos").getJSONObject(0).getString("photo_reference") : null;

                            hotelList.add(new Hotel(hotel.getString("name"), hotel.optString("vicinity"), latLng, dist[0] / 1000f, photoRef, hotel.getString("place_id")));
                        }
                        hotelAdapter.notifyDataSetChanged();
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Failed to load hotels", Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    private void setupRecyclerView() {
        hotelAdapter = new HotelAdapter(hotelList, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelSelected(Hotel hotel) {
                Intent intent = new Intent(MapsActivity.this, HotelDetailActivity.class);
                intent.putExtra("placeId", hotel.placeId);
                startActivity(intent);
            }

            @Override
            public void onHotelFocused(Hotel hotel) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hotel.location, 17));
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvHotels.setLayoutManager(layoutManager);
        binding.rvHotels.setAdapter(hotelAdapter);

        // --- SNAP TO CENTER LOGIC ---
        SnapHelper snapHelper = new PagerSnapHelper();
        binding.rvHotels.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(binding.rvHotels);

        // --- AUTO-MOVE MAP ON SWIPE ---
        binding.rvHotels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int pos = layoutManager.getPosition(centerView);
                        if (pos != RecyclerView.NO_POSITION && pos < hotelList.size()) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelList.get(pos).location, 16));
                        }
                    }
                }
            }
        });
    }

    private void searchLocationByName(String locationName) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                currentLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                updateMapAndSearch();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_maps);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomePage.class)); return true; }
            if (id == R.id.nav_profile) { startActivity(new Intent(this, UserProfileActivity.class)); return true; }
            return id == R.id.nav_maps;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        }
    }
}