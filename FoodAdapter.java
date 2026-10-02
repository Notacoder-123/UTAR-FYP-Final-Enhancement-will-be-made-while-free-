package com.example.final_yp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<FoodModel> foodList;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(FoodModel food);
    }

    public FoodAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = foodList.get(position);

        // Set food name
        holder.nameTextView.setText(food.getName());

        // Set category and serving info
        String categoryText = "Category: " + food.getCategory();
        holder.categoryTextView.setText(categoryText);

        String servingText = "Serving: " + food.getServingSize() + food.getServingSizeUnit();
        holder.servingTextView.setText(servingText);

        // Set nutrition info
        NutritionModel nutrition = food.getNutritionPerServing();
        holder.caloriesTextView.setText(nutrition.getCalories() + " cal");

        String macrosText = String.format("P: %.1fg  C: %.1fg  F: %.1fg",
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

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView nameTextView, categoryTextView, servingTextView, caloriesTextView, macrosTextView;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.food_image);
            nameTextView = itemView.findViewById(R.id.food_name);
            categoryTextView = itemView.findViewById(R.id.food_category);
            servingTextView = itemView.findViewById(R.id.food_serving);
            caloriesTextView = itemView.findViewById(R.id.food_calories);
            macrosTextView = itemView.findViewById(R.id.food_macros);
        }
    }
}