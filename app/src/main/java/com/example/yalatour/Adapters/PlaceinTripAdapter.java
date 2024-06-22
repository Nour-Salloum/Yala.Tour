package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.R;

import java.util.ArrayList;
import java.util.List;

public class PlaceinTripAdapter extends RecyclerView.Adapter<PlaceinTripAdapter.ViewHolder>{

    // Context and data lists
    private Context context;
    private List<TripClass> Trips;
    private List<TripClass> SelectedTrips;

    // Constructor to initialize context and trips list
    public PlaceinTripAdapter(Context context, List<TripClass> trips) {
        this.context = context;
        Trips = trips;
        SelectedTrips = new ArrayList<>(); // Initialize selected trips list
    }

    // Create view holder for recycler view
    @NonNull
    @Override
    public PlaceinTripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each item in recycler view
        View view = LayoutInflater.from(context).inflate(R.layout.addplacetotrip_recycleritem, parent, false);
        return new PlaceinTripAdapter.ViewHolder(view);
    }

    // Bind data to view holder
    @Override
    public void onBindViewHolder(@NonNull PlaceinTripAdapter.ViewHolder holder, int position) {
        TripClass Trip = Trips.get(position);

        // Set trip name and date to respective views
        holder.TripName.setText(Trip.getTripName());
        holder.TripDate.setText(Trip.getTripDate());

        // Handle checkbox state change
        holder.TripCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SelectedTrips.add(Trip); // Add trip to selected trips list if checked
            } else {
                SelectedTrips.remove(Trip); // Remove trip from selected trips list if unchecked
            }
        });
    }

    // Return total number of items in the recycler view
    @Override
    public int getItemCount() {
        return Trips.size();
    }

    // View holder class for recycler view item
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView TripName, TripDate;
        CheckBox TripCheckBox;

        // Constructor to initialize views
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            TripName = itemView.findViewById(R.id.TripName);
            TripDate = itemView.findViewById(R.id.TripDate);
            TripCheckBox = itemView.findViewById(R.id.TripCheckBox);
        }
    }

    // Method to get selected trips
    public List<TripClass> getSelectedTrips() {
        return SelectedTrips;
    }
}
