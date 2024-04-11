package com.example.yalatour.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.DetailsActivity.DetailActivity;
import com.example.yalatour.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<CityClass> cityList;

    public MyAdapter(Context context, List<CityClass> cityList) {
        this.context = context;
        this.cityList = cityList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cityList.get(position).getCityImage()).into(holder.recImage);
        holder.recTitle.setText((cityList).get(position).getCityTitle());
        holder.recDesc.setText((cityList).get(position).getCityDesc());
        holder.recCat.setText((cityList).get(position).getCityArea());

        holder.recCard.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, DetailActivity.class);
                intent.putExtra("Image", cityList.get(holder.getAdapterPosition()).getCityImage());
                intent.putExtra("Description", cityList.get(holder.getAdapterPosition()).getCityDesc());
                intent.putExtra("Title", cityList.get(holder.getAdapterPosition()).getCityTitle());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return  cityList.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView recImage;
    TextView recTitle, recDesc, recCat;
    CardView recCard;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recDesc = itemView.findViewById(R.id.recDesc);
        recCat = itemView.findViewById(R.id.recCat);
        recTitle = itemView.findViewById(R.id.recTitle);

    }
}
