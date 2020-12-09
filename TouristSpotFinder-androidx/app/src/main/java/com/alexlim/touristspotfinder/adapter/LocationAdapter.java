package com.alexlim.touristspotfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexlim.touristspotfinder.R;
import com.alexlim.touristspotfinder.model.LocationItem;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private ArrayList<LocationItem> mLocationList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView mLocationName;
        public TextView mLocationAddress;
        public TextView mLocationDist;

        public LocationViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            mLocationName = itemView.findViewById(R.id.locationName);
            mLocationAddress = itemView.findViewById(R.id.locationAddress);
            mLocationDist = itemView.findViewById(R.id.locationDistance);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public LocationAdapter(ArrayList<LocationItem> locationList) {
        mLocationList = locationList;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        LocationViewHolder lvh = new LocationViewHolder(v, mListener);

        return lvh;
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        LocationItem currentItem = mLocationList.get(position);
        holder.mLocationName.setText(currentItem.getLocationName());
        holder.mLocationAddress.setText(currentItem.getLocationAddress());
        holder.mLocationDist.setText(String.valueOf(currentItem.getDistance()) + "m away");
    }

    @Override
    public int getItemCount() {
        return mLocationList.size();
    }
}
