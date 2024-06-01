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
    private Context context;
    private List<TripClass> Trips;
    private List<TripClass> SelectedTrips;

    public PlaceinTripAdapter(Context context, List<TripClass> trips) {
        this.context = context;
        Trips = trips;
        SelectedTrips = new ArrayList<>();
    }

    @NonNull
    @Override
    public PlaceinTripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.addplacetotrip_recycleritem, parent, false);
        return new PlaceinTripAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceinTripAdapter.ViewHolder holder, int position) {
        TripClass Trip=Trips.get(position);

        holder.TripName.setText(Trip.getTripName());
        holder.TripDate.setText(Trip.getTripDate());
        holder.TripCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SelectedTrips.add(Trip);
            } else {
               SelectedTrips.remove(Trip);
            }
        });

    }


    @Override
    public int getItemCount() {
        return Trips.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView TripName,TripDate;
        CheckBox TripCheckBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            TripName=itemView.findViewById(R.id.TripName);
            TripDate=itemView.findViewById(R.id.TripDate);
            TripCheckBox=itemView.findViewById(R.id.TripCheckBox);
        }
    }
    public List<TripClass> getSelectedTrips(){
        return SelectedTrips;
    }
}
