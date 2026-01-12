package com.example.hotelfinder;

import android.content.Context;
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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());

        if (review.getImageUri() != null) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            // Using Glide to load the image efficiently
            Glide.with(holder.itemView.getContext())
                    .load(review.getImageUri())
                    .centerCrop()
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return reviewList.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment;
        RatingBar ratingBar;
        ImageView ivPhoto;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComment = itemView.findViewById(R.id.displayComment);
            ratingBar = itemView.findViewById(R.id.displayRating);
            ivPhoto = itemView.findViewById(R.id.displayPhoto); // Add this to item_review.xml
        }
    }
}