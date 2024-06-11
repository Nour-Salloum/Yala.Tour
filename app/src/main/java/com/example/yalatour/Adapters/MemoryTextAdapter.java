package com.example.yalatour.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.R;

import java.util.List;

public class MemoryTextAdapter extends RecyclerView.Adapter<MemoryTextAdapter.ViewHolder> {
    private Context context;
    private List<String> texts;

    public MemoryTextAdapter(Context context, List<String> texts) {
        this.context = context;
        this.texts = texts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.memory_text_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String text = texts.get(position);
        holder.textView.setText(text);
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.MemoryText);
        }
    }
}
