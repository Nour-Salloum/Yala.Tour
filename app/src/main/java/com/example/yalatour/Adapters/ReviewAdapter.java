package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.PlaceReviewClass;
import com.example.yalatour.R;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private Context context;
    private List<PlaceReviewClass> reviews;

    public ReviewAdapter(Context context, List<PlaceReviewClass> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the review at the specified position
        PlaceReviewClass review = reviews.get(position);

        // Set the data to your ViewHolder views
        holder.Username.setText(review.getUsername());
        holder.Date.setText(review.getReview_Date().toString());
        holder.ReviewText.setText(review.getReview_Description());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    // ViewHolder class
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Username,Date,ReviewText;

        ViewHolder(View itemView) {
            super(itemView);
            Username=itemView.findViewById(R.id.Review_username);
            Date=itemView.findViewById(R.id.Review_date);
            ReviewText=itemView.findViewById(R.id.ReviewText);
        }
    }
}
