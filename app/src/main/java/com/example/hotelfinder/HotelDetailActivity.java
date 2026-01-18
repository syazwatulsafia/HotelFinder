package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class HotelDetailActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyBKpSvu4pYLp6Jh1PPbBr6HFPstRUzhnCU";

    private TextView txtName, txtAddress, txtWebsite, txtRating, txtStatus;
    private Button btnCall, btnNavigate, btnSeeReview;
    private ImageView imgHotel, btnBack;

    private String currentHotelName, currentHotelAddress, currentPlaceId;
    private double currentLat, currentLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        btnBack = findViewById(R.id.btn_back_arrow);
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtWebsite = findViewById(R.id.txtWebsite);
        txtRating = findViewById(R.id.txtRating);
        txtStatus = findViewById(R.id.txtStatus);
        btnCall = findViewById(R.id.btnCall);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnSeeReview = findViewById(R.id.btnSeeReview);
        imgHotel = findViewById(R.id.imgHotel);

        currentHotelName = getIntent().getStringExtra("hotelName");
        currentHotelAddress = getIntent().getStringExtra("hotelAddress");
        currentPlaceId = getIntent().getStringExtra("placeId");
        String photoRef = getIntent().getStringExtra("photoRef");
        currentLat = getIntent().getDoubleExtra("lat", 0);
        currentLng = getIntent().getDoubleExtra("lng", 0);

        txtName.setText(currentHotelName != null ? currentHotelName : "Hotel Detail");
        txtAddress.setText(currentHotelAddress != null ? currentHotelAddress : "");

        btnBack.setOnClickListener(v -> finish());

        btnNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + currentLat + "," + currentLng + "&query_place_id=" + currentPlaceId);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        btnSeeReview.setOnClickListener(v -> {
            Intent intent = new Intent(HotelDetailActivity.this, ListReviewActivity.class);
            intent.putExtra("hotelName", currentHotelName);
            intent.putExtra("hotelAddress", currentHotelAddress);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        });

        if (photoRef != null && !photoRef.isEmpty()) {
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=1200" +
                    "&photo_reference=" + photoRef +
                    "&key=" + API_KEY;
            Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_profile_placeholder).into(imgHotel);
        }

        if (currentPlaceId != null) {
            fetchPlaceDetails(currentPlaceId);
        }
    }

    private void fetchPlaceDetails(String placeId) {
        String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=" + placeId +
                "&fields=name,photos,formatted_address,formatted_phone_number,website,rating,user_ratings_total,opening_hours" +
                "&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(detailsUrl, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        if (result == null) return;

                        currentHotelName = result.optString("name", currentHotelName);
                        currentHotelAddress = result.optString("formatted_address", currentHotelAddress);
                        txtName.setText(currentHotelName);
                        txtAddress.setText(currentHotelAddress);

                        if (result.has("photos")) {
                            String photoReference = result.getJSONArray("photos")
                                    .getJSONObject(0)
                                    .getString("photo_reference");

                            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                    "?maxwidth=1200" +
                                    "&photo_reference=" + photoReference +
                                    "&key=" + API_KEY;

                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder) // Use your placeholder
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(imgHotel);
                        }

                        String phone = result.optString("formatted_phone_number", null);
                        if (phone != null) {
                            btnCall.setVisibility(View.VISIBLE);
                            btnCall.setOnClickListener(v ->
                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))));
                        } else {
                            btnCall.setVisibility(View.GONE);
                        }

                        String website = result.optString("website", null);
                        if (website != null) {
                            txtWebsite.setVisibility(View.VISIBLE);
                            txtWebsite.setOnClickListener(v ->
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(website))));
                        } else {
                            txtWebsite.setVisibility(View.GONE);
                        }

                        double rating = result.optDouble("rating", 0.0);
                        int totalReviews = result.optInt("user_ratings_total", 0);
                        if (txtRating != null && rating > 0) {
                            txtRating.setText(rating + " ★ (" + totalReviews + " reviews)");
                        }

                        JSONObject openingHours = result.optJSONObject("opening_hours");
                        if (openingHours != null && txtStatus != null) {
                            boolean isOpenNow = openingHours.optBoolean("open_now");
                            txtStatus.setText(isOpenNow ? "Open Now" : "Closed Now");
                            txtStatus.setTextColor(getResources().getColor(
                                    isOpenNow ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}