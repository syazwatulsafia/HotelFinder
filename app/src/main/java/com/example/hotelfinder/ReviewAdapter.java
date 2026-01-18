package com.example.hotelfinder;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewList;
    private OnDeleteClickListener deleteListener;
    private boolean showDelete;

    public ReviewAdapter(List<Review> reviewList,
                         boolean showDelete,
                         OnDeleteClickListener listener) {
        this.reviewList = reviewList;
        this.showDelete = showDelete;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Review review = reviewList.get(position);

        holder.txtUserEmail.setText(review.userEmail);
        holder.txtHotelName.setText(review.hotelName);
        holder.txtHotelAddress.setText(review.hotelAddress);
        holder.txtComment.setText(review.comment);
        holder.ratingBar.setRating(review.rating);

        if (review.imageUri != null && !review.imageUri.isEmpty()) {
            holder.imgReview.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(review.imageUri)
                    .into(holder.imgReview);
        } else {
            holder.imgReview.setVisibility(View.GONE);
        }

        holder.txtHotelName.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), HotelDetailActivity.class);
            intent.putExtra("name", review.hotelName);
            intent.putExtra("address", review.hotelAddress);
            intent.putExtra("lat", review.lat);
            intent.putExtra("lng", review.lng);
            v.getContext().startActivity(intent);
        });

        if (showDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Review")
                        .setMessage("Are you sure you want to delete this review?")
                        .setPositiveButton("Delete", (d, w) -> {
                            if (deleteListener != null) {
                                deleteListener.onDelete(review);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(review.userId)
                .child("photoUri");

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String photoUri = snapshot.getValue(String.class);

                if (photoUri != null && !photoUri.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(photoUri))
                            .circleCrop()
                            .into(holder.imgUserProfile);
                } else {
                    holder.imgUserProfile.setImageResource(
                            R.drawable.profile);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserEmail, txtHotelName, txtHotelAddress, txtComment;
        RatingBar ratingBar;
        ImageView imgReview;
        Button btnDelete;
        ImageView imgUserProfile;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserEmail    = itemView.findViewById(R.id.txtUserEmail);
            imgUserProfile = itemView.findViewById(R.id.imgUserProfile);
            txtHotelName    = itemView.findViewById(R.id.txtHotelName);
            txtHotelAddress = itemView.findViewById(R.id.txtHotelAddress);
            txtComment      = itemView.findViewById(R.id.txtComment);
            ratingBar       = itemView.findViewById(R.id.ratingBar);
            imgReview       = itemView.findViewById(R.id.imgReview);
            btnDelete       = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnDeleteClickListener {
        void onDelete(Review review);
    }
}
