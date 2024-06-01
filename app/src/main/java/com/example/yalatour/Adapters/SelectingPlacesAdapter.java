package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;

import java.util.ArrayList;
import java.util.List;

public class SelectingPlacesAdapter extends RecyclerView.Adapter<SelectingPlacesAdapter.ViewHolder> {

    private Context context;
    private List<TourismPlaceClass> placesList;
    private List<TourismPlaceClass> selectedPlacesList;
    private List<TourismPlaceClass> initialSelectedPlacesList;

    public SelectingPlacesAdapter(Context context, List<TourismPlaceClass> placesList, List<TourismPlaceClass> initialSelectedPlacesList) {
        this.context = context;
        this.placesList = placesList;
        this.selectedPlacesList = new ArrayList<>(initialSelectedPlacesList); // Initialize with initial selected places
        this.initialSelectedPlacesList = initialSelectedPlacesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectingplaces_recycleritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourismPlaceClass place = placesList.get(position);
        holder.placeTitle.setText(place.getPlaceName());
        holder.placeCheckbox.setOnCheckedChangeListener(null); // Remove previous listener to avoid callback during initialization
        holder.placeCheckbox.setChecked(selectedPlacesList.contains(place));
        Glide.with(context).load(place.getPlaceImages().get(0)).into(holder.placeImage);

        holder.placeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPlacesList.add(place);
            } else {
                selectedPlacesList.remove(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }



    public List<TourismPlaceClass> getSelectedPlaces() {
        return selectedPlacesList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle;
        CheckBox placeCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeCheckbox = itemView.findViewById(R.id.placeCheckbox);
            placeImage = itemView.findViewById(R.id.placeImage);
        }
    }
}
