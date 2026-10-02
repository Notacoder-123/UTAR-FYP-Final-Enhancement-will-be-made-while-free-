package com.example.final_yp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FoodEntryAdapter extends RecyclerView.Adapter<FoodEntryAdapter.FoodEntryViewHolder> {

    private Context context;
    private List<FoodLogModel.FoodEntry> foodEntries;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public FoodEntryAdapter(Context context, List<FoodLogModel.FoodEntry> foodEntries) {
        this.context = context;
        this.foodEntries = foodEntries;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public FoodEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_entry, parent, false);
        return new FoodEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodEntryViewHolder holder, int position) {
        FoodLogModel.FoodEntry entry = foodEntries.get(position);
        FoodModel food = entry.getFood();

        // Set food name
        holder.nameTextView.setText(food.getName());

        // Set serving info
        String servingText = entry.getServings() + " " + food.getServingSizeUnit() +
                " (" + (food.getServingSize() * entry.getServings()) + "g)";
        holder.servingTextView.setText(servingText);

        // Set calorie info
        NutritionModel nutrition = food.getNutritionForPortion(entry.getServings());
        holder.caloriesTextView.setText(nutrition.getCalories() + " cal");

        // Set macros
        String macrosText = String.format("P: %.1fg • C: %.1fg • F: %.1fg",
                nutrition.getProtein(), nutrition.getCarbs(), nutrition.getFat());
        holder.macrosTextView.setText(macrosText);

        // Load food image if available
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.ic_meal)
                    .error(R.drawable.ic_meal)
                    .centerCrop()
                    .into(holder.foodImageView);
        } else {
            holder.foodImageView.setImageResource(R.drawable.ic_meal);
        }

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodEntries.size();
    }

    public static class FoodEntryViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView nameTextView, servingTextView, caloriesTextView, macrosTextView;
        ImageButton deleteButton;

        public FoodEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.food_image);
            nameTextView = itemView.findViewById(R.id.food_name);
            servingTextView = itemView.findViewById(R.id.food_serving);
            caloriesTextView = itemView.findViewById(R.id.food_calories);
            macrosTextView = itemView.findViewById(R.id.food_macros);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
