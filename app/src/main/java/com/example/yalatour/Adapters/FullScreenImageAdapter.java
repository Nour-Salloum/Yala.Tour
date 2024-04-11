package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;

import java.util.ArrayList;

public class FullScreenImageAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> imageUrls;

    // Constructor to initialize the adapter with context and list of image URLs
    public FullScreenImageAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    // InstantiateItem method to create or retrieve views for the ViewPager
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Inflating the layout for the item
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_full_screen_image, container, false);

        // Finding the ImageView in the inflated layout
        ImageView imageView = view.findViewById(R.id.imageView);

        // Loading the image into the ImageView using Glide
        Glide.with(context).load(imageUrls.get(position)).into(imageView);

        // Adding the inflated view to the ViewPager's container
        container.addView(view);

        // Returning the inflated view
        return view;
    }

    // GetCount method to return the total number of images
    @Override
    public int getCount() {
        return imageUrls.size();
    }

    // IsViewFromObject method to determine if a given view is associated with a given object
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    // DestroyItem method to remove a page from the ViewPager
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
