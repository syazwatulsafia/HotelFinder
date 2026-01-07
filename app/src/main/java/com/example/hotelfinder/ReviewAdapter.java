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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    List<Review> list;
    Context context;

    public ReviewAdapter(List<Review> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_review, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = list.get(position);
        holder.name.setText(review.username);
        holder.comment.setText(review.comment);
        holder.ratingBar.setRating(review.rating);

        Glide.with(context).load(review.profileUrl).circleCrop().into(holder.profile);
        Glide.with(context).load(review.hotelImageUrl).into(holder.hotelImg);
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile, hotelImg;
        TextView name, comment;
        RatingBar ratingBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.imgUserProfile);
            hotelImg = itemView.findViewById(R.id.imgHotelReview);
            name = itemView.findViewById(R.id.txtUsername);
            comment = itemView.findViewById(R.id.txtComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
