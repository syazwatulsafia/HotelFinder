package com.example.hotelfinder;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
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

    }

    @Override
    public int getItemCount() {
        return reviewList.size(); // ✅ FIXED
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserEmail, txtHotelName, txtHotelAddress, txtComment;

        RatingBar ratingBar;
        ImageView imgReview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserEmail   = itemView.findViewById(R.id.txtUserEmail);
            txtHotelName   = itemView.findViewById(R.id.txtHotelName);
            txtHotelAddress= itemView.findViewById(R.id.txtHotelAddress);
            txtComment     = itemView.findViewById(R.id.txtComment);
            ratingBar      = itemView.findViewById(R.id.ratingBar);
            imgReview      = itemView.findViewById(R.id.imgReview);

        }
    }
}
