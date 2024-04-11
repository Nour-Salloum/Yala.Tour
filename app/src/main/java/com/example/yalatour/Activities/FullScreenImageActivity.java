package com.example.yalatour.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.example.yalatour.Adapters.FullScreenImageAdapter;
import com.example.yalatour.R;

import java.util.ArrayList;

public class FullScreenImageActivity extends AppCompatActivity {

    ViewPager viewPager;
    ArrayList<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Receive data from the intent
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        int position = getIntent().getIntExtra("position", 0);

        // Find the ViewPager in the layout
        viewPager = findViewById(R.id.viewPager);

        // Create and set adapter for ViewPager
        FullScreenImageAdapter adapter = new FullScreenImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        // Set current item based on the selected position
        viewPager.setCurrentItem(position);
    }
}
