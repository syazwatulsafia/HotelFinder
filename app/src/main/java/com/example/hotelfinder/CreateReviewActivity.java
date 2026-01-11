package com.example.hotelfinder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText editReview;
    private Button btnSubmit, btnOpenSearch;
    private ImageView btnBack, imgHotel, img1, img2, img3;
    private TextView txtHotelName, txtHotelAddress;

    private int imageCount = 0;
    private Uri cameraImageUri;
    private String hotelName, hotelAddress;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private FirebaseFirestore db;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        db = FirebaseFirestore.getInstance();

        // 🔹 Initialize Places (Replace with your actual API Key)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBKpSvu4pYLp6Jh1PPbBr6HFPstRUzhnCU");
        }
        placesClient = Places.createClient(this);

        initViews();
        setupLaunchers();

        btnBack.setOnClickListener(v -> finish());
        btnOpenSearch.setOnClickListener(v -> startGoogleSearch());

        // Click logic for the three small image slots
        View.OnClickListener addPhotoClick = v -> checkPermissionAndShowDialog();
        img1.setOnClickListener(addPhotoClick);
        img2.setOnClickListener(addPhotoClick);
        img3.setOnClickListener(addPhotoClick);

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnOpenSearch = findViewById(R.id.btnOpenSearch);
        imgHotel = findViewById(R.id.imgHotel);
        txtHotelName = findViewById(R.id.txtHotelName);
        txtHotelAddress = findViewById(R.id.txtHotelAddress);
        ratingBar = findViewById(R.id.hotelRatingBar);
        editReview = findViewById(R.id.editReviewText);
        btnSubmit = findViewById(R.id.btnSubmitReview);
        img1 = findViewById(R.id.imgSlot1);
        img2 = findViewById(R.id.imgSlot2);
        img3 = findViewById(R.id.imgSlot3);
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) handleImageSelection(uri); });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == RESULT_OK) handleImageSelection(cameraImageUri); });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) showImageSourceDialog();
                    else Toast.makeText(this, "Permission denied. Cannot use camera.", Toast.LENGTH_SHORT).show();
                });

        autocompleteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        hotelName = place.getName();
                        hotelAddress = place.getAddress();

                        // 🔹 1. Set the text
                        txtHotelName.setText(hotelName);
                        txtHotelAddress.setText(hotelAddress);

                        // 🔹 2. Make them VISIBLE
                        imgHotel.setVisibility(View.VISIBLE);
                        txtHotelName.setVisibility(View.VISIBLE);
                        txtHotelAddress.setVisibility(View.VISIBLE);

                        // 🔹 3. Fetch the photo
                        if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                            fetchHotelPhoto(place.getPhotoMetadatas().get(0));
                        } else {
                            // If no photo exists, show a placeholder
                            imgHotel.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                });
    }

    private void startGoogleSearch() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypesFilter(Arrays.asList("establishment"))
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void fetchHotelPhoto(PhotoMetadata photoMetadata) {
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(800)
                .setMaxHeight(500)
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            imgHotel.setImageBitmap(fetchPhotoResponse.getBitmap());
        });
    }

    private void checkPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceDialog() {
        if (imageCount >= 3) {
            Toast.makeText(this, "Maximum 3 photos reached", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else galleryLauncher.launch("image/*");
                }).show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Review Picture");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

    private void handleImageSelection(Uri uri) {
        ImageView[] slots = {img1, img2, img3};
        if (imageCount < 3) {
            slots[imageCount].setImageURI(uri);
            slots[imageCount].setPadding(0, 0, 0, 0);
            slots[imageCount].setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageCount++;
        }
    }

    private void submitReview() {
        if (hotelName == null) {
            Toast.makeText(this, "Please search for a hotel first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        Map<String, Object> review = new HashMap<>();
        review.put("hotelName", hotelName);
        review.put("hotelAddress", hotelAddress);
        review.put("rating", ratingBar.getRating());
        review.put("comment", editReview.getText().toString());
        review.put("timestamp", System.currentTimeMillis());

        db.collection("reviews").add(review)
                .addOnSuccessListener(doc -> {
                    Intent intent = new Intent(this, ReviewSuccessActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                });
    }
}