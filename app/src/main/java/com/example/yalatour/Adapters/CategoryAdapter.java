package com.example.yalatour.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.Category;
import com.example.yalatour.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private List<Category> categoryList;

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitemcategory, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryCheckBox.setText(category.getName());
        holder.categoryCheckBox.setChecked(category.isSelected());
        holder.categoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                category.setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}

class CategoryViewHolder extends RecyclerView.ViewHolder {
    CheckBox categoryCheckBox;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryCheckBox = itemView.findViewById(R.id.categoryCheckBox);
    }
}
