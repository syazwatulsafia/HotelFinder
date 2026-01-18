package com.example.hotelfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    public interface OnHotelClickListener {
        void onHotelSelected(Hotel hotel);
        void onHotelFocused(Hotel hotel);
    }

    private final List<Hotel> hotelList;
    private final OnHotelClickListener listener;

    public HotelAdapter(List<Hotel> hotelList, OnHotelClickListener listener) {
        this.hotelList = hotelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hotel_card, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.tvName.setText(hotel.name);

        holder.tvDistance.setText(
                String.format(Locale.getDefault(), "%.2f km", hotel.distance)
        );

        holder.btnSelect.setOnClickListener(v -> listener.onHotelSelected(hotel));

        holder.itemView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        listener.onHotelFocused(hotel)
        );
    }


    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDistance;
        Button btnSelect;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvDistance = itemView.findViewById(R.id.tvHotelDistance);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }


}
