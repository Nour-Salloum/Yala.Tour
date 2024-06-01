package com.example.yalatour.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.TripRequirementsClass;
import com.example.yalatour.R;

import java.util.List;

public class RequirementsAdapter extends RecyclerView.Adapter<RequirementsAdapter.ViewHolder> {
    private Context context;
    private List<TripRequirementsClass> requirements;
    private boolean isAdmin;

    public RequirementsAdapter(Context context, List<TripRequirementsClass> requirements, boolean isAdmin) {
        this.context = context;
        this.requirements = requirements;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public RequirementsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requirements_recycleritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequirementsAdapter.ViewHolder holder, int position) {
        TripRequirementsClass requirement = requirements.get(position);

        // Set the text
        holder.text.setText(requirement.getText());

        // Set the checkbox state and listener
        holder.requirmentsCheckbox.setOnCheckedChangeListener(null);
        holder.requirmentsCheckbox.setChecked(requirement.isSelected());
        holder.requirmentsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            requirement.setSelected(isChecked);
            requirements.set(position, requirement); // Update the list with the changed item
        });


        // Set the visibility and editable state of EditText and CheckBox based on isAdmin flag
        holder.text.setEnabled(isAdmin);
        holder.requirmentsCheckbox.setEnabled(true);


        // Add listener to the EditText to handle "Done" action
        holder.text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Get the text from the EditText
                String newText = holder.text.getText().toString().trim();

                // Update the text of the current requirement
                requirement.setText(newText);

                // Check if the current requirement is not empty before adding a new one
                if (!newText.isEmpty()) {
                    // Add a new requirement below the current one
                    requirements.add(position + 1, new TripRequirementsClass("", false));
                    // Notify adapter that a new item is inserted
                    notifyItemInserted(position + 1);
                }
                // Notify adapter of the data change
                notifyItemChanged(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return requirements.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        EditText text;
        CheckBox requirmentsCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.RequirementsText);
            requirmentsCheckbox = itemView.findViewById(R.id.RequirementsCheckBox);

            // Add text change listener to EditText
            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No implementation needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // No implementation needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        TripRequirementsClass requirement = requirements.get(adapterPosition);
                        requirement.setText(s.toString().trim());
                        requirements.set(adapterPosition, requirement);
                    }
                }
            });
        }
    }
}
